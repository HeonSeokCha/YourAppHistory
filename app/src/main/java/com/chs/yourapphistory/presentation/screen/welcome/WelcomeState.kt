package com.chs.yourapphistory.presentation.screen.welcome

import com.chs.yourapphistory.R

data class WelcomeState(
    val tabList: List<Int> = listOf(R.raw.lottie_chart, R.raw.lottie_usage),
    val tabIdx: Int = 0,
)