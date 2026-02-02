package com.chs.yourapphistory.presentation.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent

val NavigationTransitionSpec = NavDisplay.transitionSpec {
    slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(targetOffsetX = { -it })
} + NavDisplay.popTransitionSpec {
    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
} + NavDisplay.predictivePopTransitionSpec {
    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
}