package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
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
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.time.Duration.Companion.hours

class GetPagingAppDetailList(
    private val appUsageDao: AppUsageDao,
    private val appForegroundUsageDao: AppForegroundUsageDao,
    private val appNotifyInfoDao: AppNotifyInfoDao,
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
        val minDate: LocalDate = withContext(Dispatchers.Default) {
            awaitAll(
                async(Dispatchers.IO) { appUsageDao.getFirstCollectTime() },
                async(Dispatchers.IO) { appForegroundUsageDao.getFirstCollectTime() },
                async(Dispatchers.IO) { appNotifyInfoDao.getFirstCollectTime() }
            ).min().toLocalDate()
        }

        val pageDate: LocalDate = (params.key ?: LocalDate.now()).run {
            if (params.key == null) {
                targetDate
            } else {
                this
            }
        }

        val buffetDate = if (pageDate.plusDays(Constants.PAGING_DAY) >= LocalDate.now()) {
            LocalDate.now()
        } else pageDate.plusDays(Constants.PAGING_DAY)

        val data = pageDate.run {
            if (this.minusDays(Constants.PAGING_DAY) <= minDate) minDate
            else this.minusDays(Constants.PAGING_DAY)
        }
            .reverseDateUntil(buffetDate.plusDays(1L))
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
            prevKey = if (buffetDate == LocalDate.now()) {
                null
            } else {
                if (buffetDate.plusDays(Constants.PAGING_DAY) >= LocalDate.now()) {
                    LocalDate.now()
                } else buffetDate.plusDays(Constants.PAGING_DAY + 1)
            },
            nextKey = if (pageDate.minusDays(Constants.PAGING_DAY + 1) < minDate) {
                null
            } else {
                pageDate.minusDays(Constants.PAGING_DAY + 1)
            }
        )
    }

    private fun calcHourUsageList(
        targetDate: LocalDate,
        list: Map<Long, Long>
    ): List<Pair<Int, Int>> {
        val usageMap = object : HashMap<Int, Long>() {
            init {
                for (i in 0..23) {
                    put(i, 0L)
                }
            }
        }

        list.forEach {
            val (begin, end) = it.key.toLocalDateTime() to it.value.toLocalDateTime()

            if (targetDate.dayOfMonth < end.dayOfMonth) {
                val targetDateZeroHour = targetDate.atStartOfDay()
                for (i in begin.hour..23) {
                    usageMap.computeIfPresent(i) { key, value ->
                        val calc = if (i == begin.hour) {
                            targetDateZeroHour.plusHours(i + 1L).toMillis() - begin.toMillis()
                        } else {
                            1.hours.inWholeMilliseconds
                        }

                        value + calc
                    }
                    targetDateZeroHour.minusHours(i.toLong())
                }
                return@forEach
            }

            if (begin.dayOfMonth < targetDate.dayOfMonth) {
                val targetDateZeroHour = targetDate.atStartOfDay()
                for (i in 0..end.hour) {
                    usageMap.computeIfPresent(i) { key, value ->
                        val calc = if (i == end.hour) {
                            end.toMillis() - targetDateZeroHour.plusHours(i.toLong()).toMillis()
                        } else {
                            1.hours.inWholeMilliseconds
                        }
                        value + calc
                    }
                    targetDateZeroHour.minusHours(i.toLong())
                }

                return@forEach
            }

            usageMap.computeIfPresent(begin.hour) { key, value ->
                if (begin.hour < end.hour) {
                    val targetDateZeroHour = targetDate.atStartOfDay()
                    for (i in (begin.hour + 1)..end.hour) {
                        val targetHour = targetDateZeroHour.plusHours(i.toLong())
                        usageMap.computeIfPresent(i) { _, value1 ->
                            if (i == end.hour) {
                                value1 + (end.toMillis() - targetHour.toMillis())
                            } else {
                                1.hours.inWholeMilliseconds
                            }
                        }
                        targetDateZeroHour.minusHours(i.toLong())
                    }
                    val nextHour = targetDate.atStartOfDay().plusHours((begin.hour + 1).toLong())
                    value + (nextHour.toMillis() - begin.toMillis())
                } else {
                    value + (end.toMillis() - begin.toMillis())
                }
            }
        }

        return usageMap.toList().map { it.first to it.second.toInt() }
    }

    private fun calcHourUsageList(list: List<Long>): List<Pair<Int, Int>> {
        val usageMap = object : HashMap<Int, Long>() {
            init {
                for (i in 0..23) {
                    put(i, 0L)
                }
            }
        }

        list.forEach {
            val begin = it.toLocalDateTime()

            usageMap.computeIfPresent(begin.hour) { key, value ->
                value + 1
            }
        }

        return usageMap.toList().map { it.first to it.second.toInt() }
    }
}