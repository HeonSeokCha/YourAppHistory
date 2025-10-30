package com.chs.yourapphistory.presentation.screen.welcome

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.chs.yourapphistory.common.getUsagePermission
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun WelcomeScreenRoot(
    viewModel: WelComeViewModel,
    onNavigateHome: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (getUsagePermission(context)) {
            onNavigateHome()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                WelcomeEffect.RequestPermission -> {
                    try {
                        requestPermission.launch(
                            Intent(
                                Settings.ACTION_USAGE_ACCESS_SETTINGS,
                                "package:${context.packageName}".toUri()
                            )
                        )
                    } catch (e: ActivityNotFoundException) {
                        requestPermission.launch(
                            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        )
                    }
                }
            }
        }
    }

    WelcomeScreen(
        state = state,
        onIntent = viewModel::handleIntent
    )
}

@Composable
fun WelcomeScreen(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { state.tabList.count() })
    LaunchedEffect(state.tabIdx) {
        pagerState.animateScrollToPage(state.tabIdx)
    }

    LaunchedEffect(pagerState.currentPage) {
        onIntent(WelcomeIntent.OnChangeTabIdx(pagerState.currentPage))
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        HorizontalPager(
            state = pagerState,
            key = { it }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                ContentItem(lottieRawId = state.tabList[it])
            }
        }

        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        AnimatedVisibility(visible = state.tabIdx + 1 == state.tabList.count()) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                onClick = { onIntent(WelcomeIntent.ClickFinish) },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White
                )
            ) {
                Text(text = "Finish")
            }
        }
    }
}


@Composable
fun ContentItem(@RawRes lottieRawId: Int) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRawId))
    val progress by animateLottieCompositionAsState(
        composition,
        isPlaying = true,
        speed = 1f,
        iterations = 1
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(vertical = 8.dp)
    )
}

@Composable
@Preview(showBackground = true)
fun FirstOnBoardingScreenPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        WelcomeScreen(state = WelcomeState()) {

        }
    }
}