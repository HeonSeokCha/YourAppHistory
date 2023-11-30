package com.chs.yourapphistory.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chs.yourapphistory.presentation.Screen
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailScreen
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppListScreenScreen

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

        }

        composable(Screen.ScreenUsedAppList.route) {
            UsedAppListScreenScreen()
        }

        composable(
            route = "${Screen.ScreenAppUsageDetail.route}/{packageName}",
            arguments = listOf(
                navArgument("packageName") {
                    nullable = false
                    type = NavType.StringType
                }
            )
        ) {
            AppUsageDetailScreen(
                packageName = it.arguments?.getString("packageName")!!
            )
        }
    }
}