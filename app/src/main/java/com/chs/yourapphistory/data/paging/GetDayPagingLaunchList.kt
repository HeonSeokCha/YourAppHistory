package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.reverseDateUntil
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.toAppInfo
import com.chs.yourapphistory.domain.model.AppInfo
import java.time.LocalDate

class GetDayPagingLaunchList(
    private val appInfoDao: AppInfoDao,
) : PagingSource<LocalDate, Pair<LocalDate, List<Pair<AppInfo, Int>>>>() {
    override fun getRefreshKey(state: PagingState<LocalDate, Pair<LocalDate, List<Pair<AppInfo, Int>>>>): LocalDate? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minusDays(1) ?: page?.nextKey?.plusDays(1)
        }
    }

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Pair<LocalDate, List<Pair<AppInfo, Int>>>> {
        val pageDate: LocalDate = params.key ?: LocalDate.now()

        val data = pageDate.run { this.minusDays(Constants.PAGING_DAY) }
            .reverseDateUntil(pageDate.plusDays(1L))
            .map { date ->
                date to appInfoDao.getDayLaunchList(date.toMillis()).map {
                    it.key.toAppInfo() to it.value
                }
            }

        return LoadResult.Page(
            data = data,
            prevKey = null,
            nextKey = if (data.isEmpty()) {
                null
            } else pageDate.minusDays(Constants.PAGING_DAY + 1)
        )
    }
}
