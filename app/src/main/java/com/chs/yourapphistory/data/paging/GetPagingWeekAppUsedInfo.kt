package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.atEndOfDayToMillis
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.common.reverseDateUntilWeek
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

class GetPagingWeekAppUsedInfo(
    private val minDate: LocalDate,
    private val targetDate: LocalDate,
    private val packageName: String,
    private val dao: AppUsageDao
) : PagingSource<LocalDate, Pair<Int, List<Pair<String, Int>>>>() {
    override fun getRefreshKey(state: PagingState<LocalDate, Pair<Int, List<Pair<String, Int>>>>): LocalDate? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minusDays(1) ?: page?.nextKey?.plusDays(1)
        }
    }

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Pair<Int, List<Pair<String, Int>>>> {
        val pageDate: LocalDate = (params.key ?: LocalDate.now()).run {
            if (params.key == null) {
                targetDate
            } else {
                this
            }
        }

        val data = pageDate.run {
            if (this.minusWeeks(Constants.PAGING_WEEK) <= minDate) minDate
            else this.minusWeeks(Constants.PAGING_WEEK)
        }
            .reverseDateUntilWeek(pageDate)
            .chunked(7)
            .map {
                it.first().get(WeekFields.ISO.weekOfYear()) to
                        dao.getWeeklyAppUsedInfo(
                            beginDate = it.min().atStartOfDayToMillis(),
                            endDate = it.max().atEndOfDayToMillis(),
                            packageName = packageName
                        ).map {
                            it.key.toLocalDate().dayOfWeek
                                .getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.KOREAN
                                ) to it.value.toList().sumOf { it.second - it.first }.toInt()
                        }
            }

        return LoadResult.Page(
            prevKey = if (pageDate == LocalDate.now()) {
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