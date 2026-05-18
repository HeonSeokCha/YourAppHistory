package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.calcHourUsageList
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.reverseDateUntil
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.domain.model.UsageEventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.time.LocalDate

class GetPagingDailyAppInfos(
    private val minDate: LocalDate,
    private val targetDate: LocalDate,
    private val packageName: String,
    private val appUsageDao: AppUsageDao,
    private val appForegroundDao: AppForegroundUsageDao,
    private val appNotifyInfoDao: AppNotifyInfoDao,
) : PagingSource<Int, Pair<LocalDate, Map<UsageEventType, List<Pair<Int, Int>>>>>() {

    override fun getRefreshKey(
        state: PagingState<Int, Pair<LocalDate, Map<UsageEventType, List<Pair<Int, Int>>>>>
    ): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pair<LocalDate, Map<UsageEventType, List<Pair<Int, Int>>>>> {
        val page = params.key ?: 1

        val data = if (targetDate == LocalDate.now()) {
            if (targetDate.minusDays(Constants.PAGING_DAY * page) <= minDate) minDate
            else targetDate.minusDays(Constants.PAGING_DAY * page)
        } else {
            if (targetDate.minusDays(Constants.PAGING_DAY * page) == LocalDate.now()) {
                targetDate.plusDays(1L)
            } else {
                if (targetDate.minusDays(Constants.PAGING_DAY * page) <= minDate) minDate
                else targetDate.minusDays(Constants.PAGING_DAY * page)
            }
        }
            .reverseDateUntil(targetDate.minusDays(Constants.PAGING_DAY * page))
            .map {
                it to withContext(Dispatchers.IO) {
                    val appUsage = async(Dispatchers.IO) {
                        UsageEventType.UsageEvent to calcHourUsageList(
                            list = appUsageDao.getDayPackageUsageInfo(
                                targetDate = it.toMillis(),
                                packageName = packageName
                            ),
                            targetDate = it
                        )
                    }
                    val appForeground = async(Dispatchers.IO) {
                        UsageEventType.ForegroundUsageEvent to calcHourUsageList(
                            list = appForegroundDao.getForegroundUsageInfo(
                                targetDate = it.toMillis(),
                                packageName = packageName
                            ),
                            targetDate = it
                        )

                    }
                    val appNotify = async(Dispatchers.IO) {
                        UsageEventType.NotifyEvent to calcHourUsageList(
                            list = appNotifyInfoDao.getDayNotifyCount(
                                targetDate = it.toMillis(),
                                packageName = packageName
                            )
                        )
                    }
                    val appLaunch = async(Dispatchers.IO) {
                        UsageEventType.LaunchEvent to calcHourUsageList(
                            list = appUsageDao.getDayPackageLaunchInfo(
                                targetDate = it.toMillis(),
                                packageName = packageName
                            )
                        )
                    }

                    awaitAll(appUsage, appForeground, appNotify, appLaunch)
                }.toMap()
            }


        chsLog("$targetDate -> $page -> ${data.count()}")

        return LoadResult.Page(
            data = data,
            prevKey = null,
            nextKey = null
        )
    }
}
