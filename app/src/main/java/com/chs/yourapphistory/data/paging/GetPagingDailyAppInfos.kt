package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.calcHourUsageList
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.containsWeek
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
) : PagingSource<LocalDate, Pair<LocalDate, Map<UsageEventType, List<Pair<Int, Int>>>>>() {

    private var isFirstLoad = true

    override fun getRefreshKey(
        state: PagingState<LocalDate, Pair<LocalDate, Map<UsageEventType, List<Pair<Int, Int>>>>>
    ): LocalDate? = null

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Pair<LocalDate, Map<UsageEventType, List<Pair<Int, Int>>>>> {
        val pageDate: LocalDate = params.key ?: targetDate

        val prevKey = if (isFirstLoad) {
            isFirstLoad = false
            null
        } else {
            if (params.key == targetDate) {
                null
            } else {
                if (pageDate >= LocalDate.now()) {
                    null
                } else {
                    if (pageDate.plusDays(Constants.PAGING_DAY) >= LocalDate.now()) {
                        null
                    } else pageDate.plusDays(Constants.PAGING_DAY + 1)
                }
            }
        }

        val data = if (pageDate == targetDate) {
            if (targetDate.plusDays(Constants.PAGING_DAY) >= LocalDate.now()) {
                LocalDate.now()
            } else {
                targetDate.plusDays(Constants.PAGING_DAY)
            }.run {
                val a = if (pageDate.minusDays(Constants.PAGING_DAY) <= minDate) {
                    minDate
                } else {
                    targetDate.minusDays(Constants.PAGING_DAY)
                }
                a.reverseDateUntil(this)
            }
        } else {
            if (targetDate >= pageDate) {
                if (pageDate.minusDays(Constants.PAGING_DAY) <= minDate) {
                    minDate
                } else {
                    if (pageDate == LocalDate.now() && targetDate != LocalDate.now()) {
                        LocalDate.now()
                    } else {
                        pageDate.minusDays(Constants.PAGING_DAY)
                    }
                }.run {
                    this.reverseDateUntil(pageDate)
                }
            } else {
                if (pageDate.plusDays(Constants.PAGING_DAY) >= LocalDate.now()) {
                    LocalDate.now()
                } else {
                    pageDate.plusDays(Constants.PAGING_DAY)
                }.run {
                    pageDate.reverseDateUntil(this)
                }
            }
        }.map {
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


        chsLog("$targetDate -> $pageDate -> ${data.map { it.first }}")

        return LoadResult.Page(
            data = data,
            prevKey = prevKey,
            nextKey = if (pageDate.minusDays(Constants.PAGING_DAY + 1) < minDate) {
                null
            } else {
                pageDate.minusDays(Constants.PAGING_DAY + 1)
            }
        )
    }
}
