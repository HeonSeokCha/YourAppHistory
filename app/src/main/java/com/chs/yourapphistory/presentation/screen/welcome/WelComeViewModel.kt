package com.chs.yourapphistory.presentation.screen.welcome

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class WelComeViewModel : ViewModel() {

    private val _state = MutableStateFlow(WelcomeState())
    val state = _state.asStateFlow()

    private val _effect: Channel<WelcomeEffect> = Channel(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun handleIntent(intent: WelcomeIntent) {
        when (intent) {
            WelcomeIntent.ClickFinish -> _effect.trySend(WelcomeEffect.RequestPermission)
            is WelcomeIntent.OnChangeTabIdx -> _state.update { it.copy(tabIdx = intent.idx) }
        }
    }
}