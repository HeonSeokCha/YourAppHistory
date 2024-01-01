package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.toAppUsageInfo
import com.chs.yourapphistory.domain.model.AppUsageInfo
import java.time.LocalDate

class GetDayPagingAppUsageInfo(
    private val appUsageDao: AppUsageDao,
    private val targetDate: LocalDate,
    private val packageName: String
) : PagingSource<LocalDate, Pair<LocalDate, List<AppUsageInfo>>>() {
    override fun getRefreshKey(state: PagingState<LocalDate, Pair<LocalDate, List<AppUsageInfo>>>): LocalDate? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minusDays(1) ?: page?.nextKey?.plusDays(1)
        }
    }

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Pair<LocalDate, List<AppUsageInfo>>> {
        val pageDate: LocalDate = params.key ?: LocalDate.now()

        val a = if (targetDate == pageDate) pageDate else targetDate

        val data = appUsageDao.getUsageInfoList(
            beginDate = a.minusDays(Constants.FIRST_COLLECT_DAY).toMillis(),
            endDate = a.toMillis(),
            packageName = packageName
        ).map {
            LocalDate.parse(it.key, Constants.SQL_DATE_TIME_FORMAT) to it.value.map {
                it.toAppUsageInfo()
            }
        }

        return LoadResult.Page(
            data = data,
            prevKey = if (pageDate >= LocalDate.now()) null else pageDate.plusDays(Constants.FIRST_COLLECT_DAY),
            nextKey = pageDate.minusDays(Constants.FIRST_COLLECT_DAY + 1)
        )
    }
}