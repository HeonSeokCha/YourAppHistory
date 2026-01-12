package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.atEndOfDayToMillis
import com.chs.yourapphistory.common.calcDayUsedList
import com.chs.yourapphistory.common.containsWeek
import com.chs.yourapphistory.common.reverseDateUntilWeek
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
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
) : PagingSource<LocalDate, Map<SortType, List<Pair<LocalDate, Int>>>>() {
    override fun getRefreshKey(state: PagingState<LocalDate, Map<SortType, List<Pair<LocalDate, Int>>>>): LocalDate? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minusDays(1) ?: page?.nextKey?.plusDays(1)
        }
    }

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Map<SortType, List<Pair<LocalDate, Int>>>> {
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
                        SortType.UsageEvent to calcDayUsedList(
                            dateRangeList = it,
                            list = it.associateWith { date ->
                                appUsageDao.getDayTotalAppUsedTime(date.toMillis())
                            }
                        )
                    }

                    val appForeground = async(Dispatchers.IO) {
                        SortType.ForegroundUsageEvent to calcDayUsedList(
                            dateRangeList = it,
                            list = it.associateWith { date ->
                                appForegroundDao.getDayTotalForegroundUsedTime(date.toMillis())
                            }
                        )
                    }

                    val appNotify = async(Dispatchers.IO) {
                        SortType.NotifyEvent to it.map { date ->
                            date to appNotifyInfoDao.getDayAppNotify(date.toMillis())
                        }
                    }

                    val appLaunch = async(Dispatchers.IO) {
                        SortType.LaunchEvent to it.map { date ->
                            date to appUsageDao.getDayTotalAppLaunchCount(date.toMillis())
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
