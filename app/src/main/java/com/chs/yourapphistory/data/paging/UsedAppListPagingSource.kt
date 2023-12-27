package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.atEndOfDayToMillis
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.data.ApplicationInfoSource
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.data.db.entity.AppInfoEntity
import com.chs.yourapphistory.data.db.entity.AppUsageEntity
import com.chs.yourapphistory.data.toAppInfo
import com.chs.yourapphistory.data.toAppUsageInfo
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.AppUsageInfo
import java.time.LocalDate

class UsedAppListPagingSource(
    private val applicationInfoSource: ApplicationInfoSource,
    private val appInfoDao: AppInfoDao,
    private val appUsageDao: AppUsageDao
) : PagingSource<Long, Pair<LocalDate, Map<AppInfo, List<AppUsageInfo>>>>() {
    override fun getRefreshKey(state: PagingState<Long, Pair<LocalDate, Map<AppInfo, List<AppUsageInfo>>>>): Long? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minus(1L) ?: page?.nextKey?.plus(1L)
        }
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Pair<LocalDate, Map<AppInfo, List<AppUsageInfo>>>> {
        val page: Long = params.key ?: 0L
        val localDate: LocalDate = LocalDate.now()

        val installAppList: List<AppInfoEntity> = appInfoDao.getAllPackage()
        val appUsageList: List<AppUsageEntity> = appUsageDao.getPagingDayUsageInfo(
            beginTime = localDate.minusDays(page + Constants.PAGING_OFFSET).atStartOfDayToMillis(),
            endTime = localDate.minusDays(page).atEndOfDayToMillis()
        )

        val data = localDate.minusDays(page + Constants.PAGING_OFFSET)
            .datesUntil(localDate.plusDays(1L).minusDays(page))
            .map { date ->
                val targetDayAppUsageInfo = appUsageList.filter {
                    it.beginUseTime in date.atStartOfDayToMillis()..date.atEndOfDayToMillis()
                            || it.endUseTime in date.atStartOfDayToMillis()..date.atEndOfDayToMillis()
                }
                date to installAppList.associate { appInfo ->
                    if (targetDayAppUsageInfo.any { it.packageName == appInfo.packageName }) {
                        appInfo.toAppInfo(
                            applicationInfoSource.getApplicationIcon(appInfo.packageName)
                        ) to targetDayAppUsageInfo.filter { it.packageName == appInfo.packageName }
                            .map {
                                it.toAppUsageInfo()
                            }
                    } else {
                        appInfo.toAppInfo(
                            applicationInfoSource.getApplicationIcon(appInfo.packageName)
                        ) to emptyList()
                    }
                }
            }.toList().sortedByDescending { it.first }

        return LoadResult.Page(
            data = data,
            prevKey = if (page == 0L) null else page - Constants.PAGING_OFFSET,
            nextKey = page + Constants.PAGING_OFFSET
        )
    }
}