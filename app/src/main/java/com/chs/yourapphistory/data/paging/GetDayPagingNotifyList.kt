package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.toAppInfo
import com.chs.yourapphistory.domain.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class GetDayPagingNotifyList(
    private val appInfoDao: AppInfoDao,
    private val appNotifyInfoDao: AppNotifyInfoDao
) : PagingSource<LocalDate, Pair<LocalDate, List<Pair<AppInfo, Int>>>>() {
    override fun getRefreshKey(state: PagingState<LocalDate, Pair<LocalDate, List<Pair<AppInfo, Int>>>>): LocalDate? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minusDays(1) ?: page?.nextKey?.plusDays(1)
        }
    }

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Pair<LocalDate, List<Pair<AppInfo, Int>>>> {
        val pageDate: LocalDate = params.key ?: LocalDate.now()
        val limitDate = appNotifyInfoDao.getFirstCollectTime().toLocalDate()
        val minDate = if (pageDate.minusDays(Constants.PAGING_DAY) < limitDate) {
            limitDate
        } else pageDate.minusDays(Constants.PAGING_DAY)

        val data = withContext(Dispatchers.IO) {
            minDate.datesUntil(pageDate.plusDays(1L))
                .toList()
                .reversed()
                .map { date ->
                    date to appInfoDao.getDayNotifyList(date.toMillis()).map {
                        it.key.toAppInfo() to it.value.size
                    }.sortedWith(
                        compareBy(
                            { -it.second },
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