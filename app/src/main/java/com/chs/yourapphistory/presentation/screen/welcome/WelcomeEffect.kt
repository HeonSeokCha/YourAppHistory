package com.chs.yourapphistory.presentation.screen.welcome

sealed interface WelcomeEffect {
    data object RequestPermission : WelcomeEffect
}