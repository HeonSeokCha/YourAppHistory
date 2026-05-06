package com.chs.yourapphistory.common

import com.chs.yourapphistory.domain.model.UsageEventType
import kotlin.math.ceil

object NiceNumUtil {

    private val NICE_INTERVALS = listOf(0, 1, 2, 3, 4, 5, 10, 15, 20, 25, 30, 60, 100, 120, 180, 240, 300, 360)
    fun niceNum(
        value: Int,
        usageEventType: UsageEventType
    ): List<Int> {
        val maxMinutes = when (usageEventType) {
            UsageEventType.UsageEvent, UsageEventType.ForegroundUsageEvent -> {
                (value / 1000.0 / 60.0).let {
                    if (it <= 0) 1.0 else it
                }
            }

            UsageEventType.LaunchEvent, UsageEventType.NotifyEvent -> {
                value.toDouble()
            }
        }

        val interval = findNiceInterval(maxMinutes, usageEventType)

        val niceMax = (ceil(maxMinutes / interval) * interval).run {
            if (this == 0.0) return@run 1.0
            return@run this
        }

        val ticks = mutableListOf<Int>()
        var current = 0.0
        while (current <= niceMax) {
            ticks.add(current.toInt())
            current += interval
        }

        if (ticks.size == 2) {
            ticks.add(ticks.last() * 2)
        }

        chsLog(ticks.toString())

        return when (usageEventType) {
            UsageEventType.UsageEvent, UsageEventType.ForegroundUsageEvent -> {
                ticks.map { it * 60 * 1000 }
            }

            UsageEventType.LaunchEvent, UsageEventType.NotifyEvent -> {
                ticks
            }
        }
    }

    private fun findNiceInterval(
        maxMinutes: Double,
        usageEventType: UsageEventType
    ): Int {
        val roughInterval = maxMinutes / 2
        val niceInterval = NICE_INTERVALS
            .firstOrNull {
                when (usageEventType) {
                    UsageEventType.UsageEvent, UsageEventType.ForegroundUsageEvent -> {
                        it >= roughInterval
                    }
                    UsageEventType.LaunchEvent, UsageEventType.NotifyEvent -> {
                        it > roughInterval
                    }
                }
            }
            ?: NICE_INTERVALS.last()

        return niceInterval
    }
}