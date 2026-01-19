package com.chs.yourapphistory.presentation.screen.common

data class BarArea(
    val idx: Int,
    val value: Int,
    val xStart: Float,
    val xEnd: Float
)

data class BarAreas(
    val idx: Int,
    val values: List<Int>,
    val xStart: Float,
    val xEnd: Float
)
