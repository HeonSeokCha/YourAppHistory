package com.chs.yourapphistory.common

import kotlin.math.ceil

object NiceNumUtil {

    private val NICE_INTERVALS_MINUTES = listOf(1, 2, 3, 4, 5, 10, 15, 20, 25, 30, 60, 120)
    fun niceNum(value: Int): List<Int> {
        val maxMinutes = (value / 1000.0 / 60.0).let {
            if (it <= 0) 1.0 else it
        }

        val interval = findNiceInterval(maxMinutes)

        val niceMax = ceil(maxMinutes / interval) * interval

        val ticks = mutableListOf<Int>()
        var current = 0.0
        while (current <= niceMax) {
            ticks.add(current.toInt())
            current += interval
        }

        chsLog(ticks.toString())

        return ticks.map { it * 60 * 1000 }
    }

    private fun findNiceInterval(maxMinutes: Double): Double {
        val roughInterval = maxMinutes / 2
        val niceInterval = NICE_INTERVALS_MINUTES
            .firstOrNull { it >= roughInterval }
            ?: NICE_INTERVALS_MINUTES.last()

        return niceInterval.toDouble()
    }
}