package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.calcHourUsageList
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.reverseDateUntil
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.common.toLocalDateTime
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.domain.model.AppDetailInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.time.Duration.Companion.hours

class GetPagingAppDetailList(
    private val appUsageDao: AppUsageDao,
    private val appForegroundUsageDao: AppForegroundUsageDao,
    private val appNotifyInfoDao: AppNotifyInfoDao,
    private val minDate: LocalDate,
    private val targetDate: LocalDate,
    private val targetPackageName: String
) : PagingSource<LocalDate, Pair<LocalDate, AppDetailInfo>>() {

    override fun getRefreshKey(state: PagingState<LocalDate, Pair<LocalDate, AppDetailInfo>>): LocalDate? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minusDays(1) ?: page?.nextKey?.plusDays(1)
        }
    }

    override suspend fun load(params: LoadParams<LocalDate>): LoadResult<LocalDate, Pair<LocalDate, AppDetailInfo>> {
        val pageDate: LocalDate = (params.key ?: LocalDate.now()).run {
            if (params.key == null) {
                targetDate
            } else {
                this
            }
        }

        val data = pageDate.run {
            if (this.minusDays(Constants.PAGING_DAY) <= minDate) minDate
            else this.minusDays(Constants.PAGING_DAY)
        }
            .reverseDateUntil(pageDate.plusDays(1L))
            .map {
                it to withContext(Dispatchers.Default) {
                    val usageInfo = calcHourUsageList(
                        list = async(Dispatchers.IO) {
                            appUsageDao.getDayPackageUsageInfo(
                                targetDate = it.toMillis(),
                                packageName = targetPackageName
                            )
                        }.await(),
                        targetDate = it
                    )

                    val foregroundInfo = calcHourUsageList(
                        list = async(Dispatchers.IO) {
                            appForegroundUsageDao.getForegroundUsageInfo(
                                targetDate = it.toMillis(),
                                packageName = targetPackageName
                            )
                        }.await(),
                        targetDate = it
                    )

                    val notifyInfo = calcHourUsageList(
                        list = async(Dispatchers.IO) {
                            appNotifyInfoDao.getDayNotifyCount(
                                targetDate = it.toMillis(),
                                packageName = targetPackageName
                            )
                        }.await()
                    )

                    val launchInfo = calcHourUsageList(
                        list = async(Dispatchers.IO) {
                            appUsageDao.getDayPackageLaunchInfo(
                                targetDate = it.toMillis(),
                                packageName = targetPackageName
                            )
                        }.await()
                    )

                    AppDetailInfo(
                        packageName = targetPackageName,
                        foregroundUsageInfo = foregroundInfo,
                        usageInfo = usageInfo,
                        notifyInfo = notifyInfo,
                        launchCountInfo = launchInfo
                    )
                }
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