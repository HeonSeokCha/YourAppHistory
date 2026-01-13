package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.calcDailyTotalTime
import com.chs.yourapphistory.common.reverseDateUntilWeek
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.toAppSimpleInfo
import com.chs.yourapphistory.domain.model.AppTotalUsageInfo
import com.chs.yourapphistory.domain.model.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.collections.chunked

class GetPagingWeeklyTotalAppInfo(
    private val minDate: LocalDate,
    private val appUsageDao: AppUsageDao,
    private val appForegroundDao: AppForegroundUsageDao,
    private val appNotifyInfoDao: AppNotifyInfoDao,
) : PagingSource<LocalDate, Map<SortType, List<Pair<LocalDate, List<AppTotalUsageInfo>>>>>() {

    override fun getRefreshKey(state: PagingState<LocalDate, Map<SortType, List<Pair<LocalDate, List<AppTotalUsageInfo>>>>>): LocalDate? = null

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Map<SortType, List<Pair<LocalDate, List<AppTotalUsageInfo>>>>> {
        val pageDate: LocalDate = params.key ?: LocalDate.now()

        val data = pageDate.run {
            if (this.minusWeeks(Constants.PAGING_WEEK) <= minDate) return@run minDate
            this
        }
            .reverseDateUntilWeek(pageDate)
            .chunked(7)
            .map {

                withContext(Dispatchers.IO) {
                    val appUsage = async(Dispatchers.IO) {
                        SortType.UsageEvent to it.map { date ->
                            date to appUsageDao.getDayAppUsedInfo(date.toMillis()).map {
                                it.key.toAppSimpleInfo(
                                    calcDailyTotalTime(
                                        list = it.value,
                                        targetDate = date
                                    )
                                )
                            }.filter { it.totalUsedInfo > 0L }
                        }
                    }

                    val appForeground = async(Dispatchers.IO) {
                        SortType.ForegroundUsageEvent to it.map { date ->
                            date to appForegroundDao.getDayForegroundUsedList(date.toMillis()).map {
                                it.key.toAppSimpleInfo(
                                    calcDailyTotalTime(
                                        list = it.value,
                                        targetDate = date
                                    )
                                )
                            }.filter { it.totalUsedInfo > 0L }
                        }
                    }

                    val appNotify = async(Dispatchers.IO) {
                        SortType.NotifyEvent to it.map { date ->
                            date to appNotifyInfoDao.getDayNotifyList(date.toMillis()).map {
                                it.key.toAppSimpleInfo(totalUsedInfo = it.value.toLong())
                            }.filter { it.totalUsedInfo > 0L }
                        }
                    }

                    val appLaunch = async(Dispatchers.IO) {
                        SortType.LaunchEvent to it.map { date ->
                            date to appUsageDao.getDayAppLaunchInfo(date.toMillis()).map {
                                it.key.toAppSimpleInfo(totalUsedInfo = it.value.toLong())
                            }.filter { it.totalUsedInfo > 0L }
                        }
                    }

                    awaitAll(appUsage, appForeground, appLaunch, appNotify)
                }.toMap()
            }

        return LoadResult.Page(
            prevKey = null,
            nextKey = if (pageDate.minusWeeks(Constants.PAGING_WEEK + 1) < minDate) {
                null
            } else {
                pageDate.minusWeeks(Constants.PAGING_WEEK + 1)
            },
            data = data
        )
    }
}
