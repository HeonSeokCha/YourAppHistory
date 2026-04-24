package com.chs.yourapphistory.common

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

object NiceNumUtil {
    fun niceNum(value: Double, round: Boolean): Double {
        val exponent = floor(log10(value))
        val fraction = value / 10.0.pow(exponent)

        val niceFraction = if (round) {
            when {
                fraction < 1.5 -> 1.0
                fraction < 3.0 -> 2.0
                fraction < 7.0 -> 5.0
                else -> 10.0
            }
        } else {
            when {
                fraction <= 1.0 -> 1.0
                fraction <= 2.0 -> 2.0
                fraction <= 5.0 -> 5.0
                else -> 10.0
            }
        }
        return niceFraction * 10.0.pow(exponent)
    }

    fun calculateTicks(min: Double, max: Double): Int {
        val range = niceNum(max - min, false)
        val tickSpacing = niceNum(range / (3 - 1), true)

        val niceMax = ceil(max / tickSpacing) * tickSpacing

        chsLog("Range: $niceMax with spacing: $tickSpacing")
        return niceMax.toInt()
    }
}