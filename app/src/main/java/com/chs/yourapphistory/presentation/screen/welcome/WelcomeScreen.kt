package com.chs.yourapphistory.presentation.screen.welcome

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RawRes
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.chs.yourapphistory.R
import com.chs.yourapphistory.common.getUsagePermission
import com.chs.yourapphistory.presentation.Screen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    navController: NavHostController
) {
    val context: Context = LocalContext.current

    val lottieList = listOf(R.raw.lottie_chart, R.raw.lottie_usage)

    val pagerState = rememberPagerState(pageCount = { lottieList.size })

    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (getUsagePermission(context)) {
            navController.navigate(Screen.ScreenUsedAppList) {
                popUpTo(Screen.ScreenWelcome) {
                    inclusive = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        HorizontalPager(
            state = pagerState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                ContentItem(lottieRawId = lottieList[it])
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
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(16.dp)
                )
            }
        }



        if (pagerState.currentPage == lottieList.size - 1) {
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    requestPermission.launch(
                        Intent(
                            Settings.ACTION_USAGE_ACCESS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                },
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

//    Text(text = "")

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
        WelcomeScreen(navController = rememberNavController())
    }
}