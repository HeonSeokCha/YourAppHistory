package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.atEndOfDayToMillis
import com.chs.yourapphistory.common.calcDayUsedList
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.containsWeek
import com.chs.yourapphistory.common.reverseDateUntilWeek
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
import kotlin.collections.chunked

class GetPagingWeeklyAppInfos(
    private val minDate: LocalDate,
    private val targetDate: LocalDate,
    private val packageName: String,
    private val appUsageDao: AppUsageDao,
    private val appForegroundDao: AppForegroundUsageDao,
    private val appNotifyInfoDao: AppNotifyInfoDao,
) : PagingSource<LocalDate, Map<UsageEventType, List<Pair<LocalDate, Int>>>>() {
    override fun getRefreshKey(state: PagingState<LocalDate, Map<UsageEventType, List<Pair<LocalDate, Int>>>>): LocalDate? = null

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Map<UsageEventType, List<Pair<LocalDate, Int>>>> {
        val pageDate: LocalDate = (params.key ?: LocalDate.now()).run {
            if (params.key == null) {
                targetDate
            } else {
                this
            }
        }

        val data = pageDate.run {
            if (this.minusWeeks(Constants.PAGING_WEEK) <= minDate) minDate
            else {
                if (this == LocalDate.now() && targetDate != LocalDate.now()) {
                    LocalDate.now()
                } else {
                    this.minusWeeks(Constants.PAGING_WEEK)
                }
            }
        }
            .reverseDateUntilWeek(pageDate)
            .chunked(7)
            .map {
                withContext(Dispatchers.IO) {
                    val appUsage = async(Dispatchers.IO) {
                        UsageEventType.UsageEvent to calcDayUsedList(
                            dateRangeList = it,
                            list = it.associateWith { date ->
                                appUsageDao.getDayPackageUsageInfo(
                                    targetDate = date.toMillis(),
                                    packageName = packageName
                                )
                            }
                        )
                    }
                    val appForeground = async(Dispatchers.IO) {
                        UsageEventType.ForegroundUsageEvent to calcDayUsedList(
                            dateRangeList = it,
                            list = it.associateWith { date ->
                                appForegroundDao.getForegroundUsageInfo(
                                    targetDate = date.toMillis(),
                                    packageName = packageName
                                )
                            }
                        )
                    }
                    val appNotify = async(Dispatchers.IO) {
                        UsageEventType.NotifyEvent to calcDayUsedList(
                            dateRangeList = it,
                            list = appNotifyInfoDao.getWeeklyNotifyCount(
                                beginDate = it.min().toMillis(),
                                endDate = it.max().atEndOfDayToMillis(),
                                packageName = packageName
                            )
                        )
                    }
                    val appLaunch = async(Dispatchers.IO) {
                        UsageEventType.LaunchEvent to calcDayUsedList(
                            dateRangeList = it,
                            list = appUsageDao.getWeeklyAppLaunchInfo(
                                beginDate = it.min().toMillis(),
                                endDate = it.max().atEndOfDayToMillis(),
                                packageName = packageName
                            )
                        )
                    }

                    awaitAll(appUsage, appForeground, appNotify, appLaunch)
                }.toMap()
            }

        return LoadResult.Page(
            prevKey = if (pageDate.containsWeek(LocalDate.now())) {
                null
            } else {
                if (pageDate.plusWeeks(Constants.PAGING_WEEK) >= LocalDate.now()) {
                    LocalDate.now()
                } else pageDate.plusWeeks(Constants.PAGING_WEEK + 1)
            },
            nextKey = if (pageDate.minusWeeks(Constants.PAGING_WEEK + 1) < minDate) {
                null
            } else {
                pageDate.minusWeeks(Constants.PAGING_WEEK + 1)
            },
            data = data
        )
    }
}
