package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.calcHourUsageList
import com.chs.yourapphistory.common.reverseDateUntil
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import java.time.LocalDate

class GetPagingDailyAppUsedInfo(
    private val minDate: LocalDate,
    private val targetDate: LocalDate,
    private val packageName: String,
    private val dao: AppUsageDao
) : PagingSource<LocalDate, Pair<LocalDate, List<Pair<Int, Int>>>>() {

    override fun getRefreshKey(state: PagingState<LocalDate, Pair<LocalDate, List<Pair<Int, Int>>>>): LocalDate? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minusDays(1) ?: page?.nextKey?.plusDays(1)
        }
    }

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Pair<LocalDate, List<Pair<Int, Int>>>> {
        val pageDate: LocalDate = (params.key ?: LocalDate.now()).run {
            if (params.key == null) {
                targetDate
            } else {
                this
            }
        }

        val data = pageDate.run {
            if (targetDate == LocalDate.now()) {
                if (this.minusDays(Constants.PAGING_DAY) <= minDate) minDate
                else this.minusDays(Constants.PAGING_DAY)
            } else {
                if (this == LocalDate.now()) {
                    targetDate.plusDays(1L)
                } else {
                    if (this.minusDays(Constants.PAGING_DAY) <= minDate) minDate
                    else this.minusDays(Constants.PAGING_DAY)
                }
            }
        }
            .reverseDateUntil(pageDate)
            .map {
                it to calcHourUsageList(
                    list = dao.getDayPackageUsageInfo(
                        targetDate = it.toMillis(),
                        packageName = packageName
                    ),
                    targetDate = it
                )
            }

        return LoadResult.Page(
            data = data,
            prevKey = if (pageDate == LocalDate.now()) {
                null
            } else {
                if (pageDate.plusDays(Constants.PAGING_DAY) >= LocalDate.now()) {
                    LocalDate.now()
                } else pageDate.plusDays(Constants.PAGING_DAY + 1)
            },
            nextKey = if (pageDate.minusDays(Constants.PAGING_DAY + 1) < minDate) {
                null
            } else {
                pageDate.minusDays(Constants.PAGING_DAY + 1)
            }
        )
    }
}