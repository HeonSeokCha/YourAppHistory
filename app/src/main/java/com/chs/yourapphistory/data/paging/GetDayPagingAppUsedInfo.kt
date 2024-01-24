package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.getUntilDateList
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.ApplicationInfoSource
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.data.toAppInfo
import com.chs.yourapphistory.data.toAppUsageInfo
import com.chs.yourapphistory.domain.model.AppBaseUsageInfo.AppUsageInfo
import com.chs.yourapphistory.domain.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class GetDayPagingAppUsedInfo(
    private val appInfoDao: AppInfoDao,
    private val applicationInfoSource: ApplicationInfoSource
) : PagingSource<LocalDate, Pair<LocalDate, List<Pair<AppInfo, List<AppUsageInfo>>>>>() {
    override fun getRefreshKey(state: PagingState<LocalDate, Pair<LocalDate, List<Pair<AppInfo, List<AppUsageInfo>>>>>): LocalDate? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minusDays(1) ?: page?.nextKey?.plusDays(1)
        }
    }

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Pair<LocalDate, List<Pair<AppInfo, List<AppUsageInfo>>>>> {
        val pageDate: LocalDate = params.key ?: LocalDate.now()

        val data = pageDate.minusDays(Constants.PAGING_DAY).datesUntil(pageDate.plusDays(1L))
            .toList()
            .reversed()
            .map { date ->
                val list = appInfoDao.getDayUsedAppInfoList(date.toMillis()).map {
                    it.key.toAppInfo(applicationInfoSource.getApplicationIcon(it.key.packageName)) to it.value.map {
                        it.toAppUsageInfo()
                    }
                }.sortedWith(
                    compareBy(
                        { -it.second.sumOf { (it.endUseTime.toMillis() - it.beginUseTime.toMillis()) } },
                        { it.first.label }
                    )
                )
                date to list
            }

        return LoadResult.Page(
            data = data,
            prevKey = null,
            nextKey = if (data.isEmpty()) null else pageDate.minusDays(Constants.PAGING_DAY + 1)
        )
    }
}