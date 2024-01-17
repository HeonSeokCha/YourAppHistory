package com.chs.yourapphistory.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.ApplicationInfoSource
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.toAppInfo
import com.chs.yourapphistory.data.toAppUsageInfo
import com.chs.yourapphistory.domain.model.AppBaseUsageInfo.AppUsageInfo
import com.chs.yourapphistory.domain.model.AppInfo
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

class GetDayPagingAppUsedInfo(
    private val appInfoDao: AppInfoDao,
    private val applicationInfoSource: ApplicationInfoSource,
) : PagingSource<LocalDate, Pair<LocalDate, List<Pair<AppInfo, List<AppUsageInfo>>>>>() {
    override fun getRefreshKey(state: PagingState<LocalDate, Pair<LocalDate, List<Pair<AppInfo, List<AppUsageInfo>>>>>): LocalDate? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minusDays(1) ?: page?.nextKey?.plusDays(1)
        }
    }

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Pair<LocalDate, List<Pair<AppInfo, List<AppUsageInfo>>>>> {
        val pageDate: LocalDate = params.key ?: LocalDate.now()
        val installPackageList: List<String> =
            applicationInfoSource.getInstalledLauncherPackageNameList()

//        Log.e("MEASURE1", LocalDateTime.now().toString())
        val data = appInfoDao.getDayUsedAppInfoList(
            beginDate = pageDate.minusDays(Constants.FIRST_COLLECT_DAY).toMillis(),
            endDate = pageDate.toMillis()
        ).map {
            val date = LocalDate.parse(it.key, Constants.SQL_DATE_TIME_FORMAT)

            val list = installPackageList.map { packageName ->
                if (it.value.containsKey(packageName)) {
                    AppInfo(
                        packageName = packageName,
                        label = applicationInfoSource.getApplicationLabel(packageName),
                        icon = applicationInfoSource.getApplicationIcon(packageName)
                    ) to it.value[packageName]!!.map {
                        it.toAppUsageInfo()
                    }
                } else {
                    AppInfo(
                        packageName = packageName,
                        label = applicationInfoSource.getApplicationLabel(packageName),
                        icon = applicationInfoSource.getApplicationIcon(packageName)
                    ) to emptyList()
                }
            }.sortedWith(
                compareBy(
                    { -it.second.sumOf { (it.endUseTime.toMillis() - it.beginUseTime.toMillis()) } },
                    { it.first.label }
                )
            )

            date to list
        }
//        Log.e("MEASURE2", LocalDateTime.now().toString())

        return LoadResult.Page(
            data = data,
            prevKey = if (pageDate >= LocalDate.now()) null else pageDate.plusDays(Constants.FIRST_COLLECT_DAY),
            nextKey = if (data.isEmpty()) null else pageDate.minusDays(Constants.FIRST_COLLECT_DAY + 1)
        )
    }
}