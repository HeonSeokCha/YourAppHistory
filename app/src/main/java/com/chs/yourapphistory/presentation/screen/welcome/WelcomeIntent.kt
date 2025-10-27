package com.chs.yourapphistory.presentation.screen.welcome

sealed interface WelcomeIntent {
    data object ClickFinish : WelcomeIntent
    data class OnChangeTabIdx(val idx: Int) : WelcomeIntent
}