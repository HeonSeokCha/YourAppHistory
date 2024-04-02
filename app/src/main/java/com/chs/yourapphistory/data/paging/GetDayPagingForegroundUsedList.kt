package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.toAppInfo
import com.chs.yourapphistory.domain.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class GetDayPagingForegroundUsedList(
    private val appInfoDao: AppInfoDao,
) : PagingSource<LocalDate, Pair<LocalDate, List<Pair<AppInfo, List<Pair<Long, Long>>>>>>() {
    override fun getRefreshKey(state: PagingState<LocalDate, Pair<LocalDate, List<Pair<AppInfo, List<Pair<Long, Long>>>>>>): LocalDate? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minusDays(1) ?: page?.nextKey?.plusDays(1)
        }
    }

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Pair<LocalDate, List<Pair<AppInfo, List<Pair<Long, Long>>>>>> {
        val pageDate: LocalDate = params.key ?: LocalDate.now()

        val data = pageDate.run { this.minusDays(Constants.PAGING_DAY) }
            .datesUntil(pageDate.plusDays(1L))
            .toList()
            .reversed()
            .map { date ->
                date to appInfoDao.getDayForegroundUsedList(date.toMillis()).map {
                    it.key.toAppInfo() to it.value.map { it.key to it.value }
                }.sortedWith(
                    compareBy(
                        { -it.second.sumOf { it.second - it.first } },
                        { it.first.label }
                    )
                )
            }

        return LoadResult.Page(
            data = data,
            prevKey = null,
            nextKey = if (data.isEmpty()) null else pageDate.minusDays(Constants.PAGING_DAY + 1)
        )
    }
}