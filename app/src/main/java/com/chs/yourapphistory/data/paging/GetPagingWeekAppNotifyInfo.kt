package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.atEndOfDayToMillis
import com.chs.yourapphistory.common.calcDayUsedList
import com.chs.yourapphistory.common.containsWeek
import com.chs.yourapphistory.common.reverseDateUntilWeek
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import java.time.LocalDate

class GetPagingWeekAppNotifyInfo(
    private val minDate: LocalDate,
    private val targetDate: LocalDate,
    private val packageName: String,
    private val dao: AppNotifyInfoDao
) : PagingSource<LocalDate, Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>() {
    override fun getRefreshKey(state: PagingState<LocalDate, Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>>): LocalDate? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minusDays(1) ?: page?.nextKey?.plusDays(1)
        }
    }

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Pair<List<LocalDate>, List<Pair<LocalDate, Int>>>> {
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
                val dateRangeList = it
                dateRangeList to calcDayUsedList(
                    dateRangeList = dateRangeList,
                    list = dao.getWeeklyNotifyCount(
                        beginDate = dateRangeList.min().toMillis(),
                        endDate = dateRangeList.max().atEndOfDayToMillis(),
                        packageName = packageName
                    )
                )
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
