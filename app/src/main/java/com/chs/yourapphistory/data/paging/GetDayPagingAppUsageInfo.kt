package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toLocalDate
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
        val pageDate: LocalDate = params.key ?: targetDate
        val endDate = appUsageDao.getOldestCollectTime().toLocalDate()

        val data = appUsageDao.getUsageInfoList(
            beginDate = pageDate.minusDays(Constants.FIRST_COLLECT_DAY).toMillis(),
            endDate = pageDate.toMillis(),
            packageName = packageName
        ).map {
            LocalDate.parse(it.key, Constants.SQL_DATE_TIME_FORMAT) to it.value.map {
                it.toAppUsageInfo()
            }
        }

        val dateList = pageDate.minusDays(Constants.FIRST_COLLECT_DAY).datesUntil(
            pageDate.plusDays(1L)
        ).map { date ->
            if (data.any { it.first == date }) {
                date to data.find { it.first == date }!!.second
            } else {
                date to emptyList()
            }
        }.toList().reversed()

        return LoadResult.Page(
            data = dateList,
            prevKey = if (pageDate >= LocalDate.now()) null else pageDate.plusDays(Constants.FIRST_COLLECT_DAY),
            nextKey = if (pageDate <= endDate) null else pageDate.minusDays(Constants.FIRST_COLLECT_DAY + 1)
        )
    }
}