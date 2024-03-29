package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.toAppInfo
import com.chs.yourapphistory.domain.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class GetDayPagingUsedList(
    private val appInfoDao: AppInfoDao,
    private val appUsageDao: AppUsageDao,
) : PagingSource<LocalDate, Pair<LocalDate, List<Pair<AppInfo, List<Pair<Long, Long>>>>>>() {
    override fun getRefreshKey(state: PagingState<LocalDate, Pair<LocalDate, List<Pair<AppInfo, List<Pair<Long, Long>>>>>>): LocalDate? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minusDays(1) ?: page?.nextKey?.plusDays(1)
        }
    }

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Pair<LocalDate, List<Pair<AppInfo, List<Pair<Long, Long>>>>>> {
        val pageDate: LocalDate = params.key ?: LocalDate.now()
        val limitDate = appUsageDao.getFirstCollectTime().toLocalDate()
        val minDate = if (pageDate.minusDays(Constants.PAGING_DAY) < limitDate) {
            limitDate
        } else pageDate.minusDays(Constants.PAGING_DAY)

        val data = withContext(Dispatchers.IO) {
            minDate.datesUntil(pageDate.plusDays(1L))
                .toList()
                .reversed()
                .map { date ->
                    date to appInfoDao.getDayUsedList(date.toMillis()).map {
                        it.key.toAppInfo() to it.value.map { it.key to it.value }
                    }.sortedWith(
                        compareBy(
                            { -it.second.sumOf { it.second - it.first } },
                            { it.first.label }
                        )
                    )
                }
        }

        return LoadResult.Page(
            data = data,
            prevKey = null,
            nextKey = if (minDate == limitDate) null else pageDate.minusDays(Constants.PAGING_DAY + 1)
        )
    }
}