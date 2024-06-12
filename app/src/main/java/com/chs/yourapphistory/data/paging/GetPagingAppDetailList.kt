package com.chs.yourapphistory.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.atStartOfDayToMillis
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.reverseDateUntil
import com.chs.yourapphistory.common.toLocalDateTime
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.data.db.dao.AppForegroundUsageDao
import com.chs.yourapphistory.data.db.dao.AppInfoDao
import com.chs.yourapphistory.data.db.dao.AppNotifyInfoDao
import com.chs.yourapphistory.data.db.dao.AppUsageDao
import com.chs.yourapphistory.domain.model.AppDetailInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.time.Duration.Companion.hours
import kotlin.time.measureTime

class GetPagingAppDetailList(
    private val appUsageDao: AppUsageDao,
    private val appForegroundUsageDao: AppForegroundUsageDao,
    private val appNotifyInfoDao: AppNotifyInfoDao,
    private val targetDate: LocalDate,
    private val targetPackageName: String
) : PagingSource<Long, Pair<LocalDate, AppDetailInfo>>() {

    override fun getRefreshKey(state: PagingState<Long, Pair<LocalDate, AppDetailInfo>>): Long? {
        return state.anchorPosition?.let { position ->
            val page = state.closestPageToPosition(position)
            page?.prevKey?.minus(1) ?: page?.nextKey?.plus(1)
        }
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Pair<LocalDate, AppDetailInfo>> {
        val pageKey: Long = params.key ?: 1L

        chsLog(targetDate.minusDays(pageKey).toString())

        val bufferDate = if (pageKey == 1L) {
            if (targetDate == LocalDate.now()) {
                targetDate.plusDays(1L)
            } else if (targetDate > LocalDate.now().minusDays(Constants.PAGING_DAY)) {
                LocalDate.now().plusDays(1L)
            } else {
                targetDate.plusDays(3L)
            }
        } else {
            targetDate.minusDays(pageKey + 1)
        }


        val data = targetDate.run { this.minusDays(Constants.PAGING_DAY + pageKey + 1) }
            .reverseDateUntil(bufferDate)
            .map {
                it to withContext(Dispatchers.IO) {
                    val usageInfo = calcHourUsageList(
                        list = async {
                            appUsageDao.getDayUsageInfoList(
                                targetDate = it.toMillis(),
                                packageName = targetPackageName
                            )
                        }.await(),
                        targetDate = it
                    )

                    val foregroundInfo = calcHourUsageList(
                        list = async {
                            appForegroundUsageDao.getForegroundUsageInfo(
                                targetDate = it.toMillis(),
                                packageName = targetPackageName
                            )
                        }.await(),
                        targetDate = it
                    )

                    val notifyInfo = calcHourUsageList(
                        list = async {
                            appNotifyInfoDao.getDayNotifyCount(
                                targetDate = it.toMillis(),
                                packageName = targetPackageName
                            )
                        }.await()
                    )

                    val launchInfo = calcHourUsageList(
                        list = async {
                            appUsageDao.getDayUsageBeginInfoList(
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
            prevKey = if (pageKey - Constants.PAGING_DAY <= 0) null else pageKey - Constants.PAGING_DAY,
            nextKey = if (data.isEmpty()) null else Constants.PAGING_DAY + pageKey
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
                            begin.toMillis() - targetDateZeroHour.plusHours(i.toLong()).toMillis()
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