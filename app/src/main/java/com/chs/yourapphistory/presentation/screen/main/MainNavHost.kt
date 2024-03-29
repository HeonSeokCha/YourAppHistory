package com.chs.yourapphistory.presentation.screen.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.presentation.Screen
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailScreen
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppListScreenScreen
import com.chs.yourapphistory.presentation.screen.welcome.WelcomeScreen
import java.time.LocalDate

@Composable
fun MainNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    isGrantPermission: Boolean,
    selectPackage: (String?) -> Unit
) {
    NavHost(
        modifier = Modifier.padding(paddingValues),
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
            route = "${Screen.ScreenAppUsageDetail.route}/{key_target_package_name}/{key_target_date}",
            arguments = listOf(
                navArgument(Constants.KEY_TARGET_PACKAGE_NAME) {
                    nullable = false
                    type = NavType.StringType
                },
                navArgument(Constants.KEY_TARGET_DATE) {
                    nullable = false
                    type = NavType.LongType
                },
            )
        ) {
            AppUsageDetailScreen(navController = navController) {
                selectPackage(it)
            }
        }
    }
}