package com.chs.yourapphistory.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.presentation.Screen
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailScreen
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppListScreenScreen
import com.chs.yourapphistory.presentation.screen.welcome.WelcomeScreen
import java.time.LocalDate

@Composable
fun MainNavHost(
    navController: NavHostController,
    isGrantPermission: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = if (isGrantPermission) {
            Screen.ScreenUsedAppList.route
        } else {
            Screen.ScreenWelcome.route
        }
    ) {
        composable(Screen.ScreenWelcome.route) {
            WelcomeScreen(navController = navController)

        }

        composable(Screen.ScreenUsedAppList.route) {
            UsedAppListScreenScreen(navController)
        }

        composable(
            route = "${Screen.ScreenAppUsageDetail.route}/{packageName}/{targetDate}",
            arguments = listOf(
                navArgument("packageName") {
                    nullable = false
                    type = NavType.StringType
                },
                navArgument("targetDate") {
                    nullable = false
                    type = NavType.LongType
                },
            )
        ) {
            AppUsageDetailScreen(
                packageName = it.arguments?.getString("packageName")!!,
                date = it.arguments?.getLong("targetDate")?.toLocalDate() ?: LocalDate.now()
            )
        }
    }
}