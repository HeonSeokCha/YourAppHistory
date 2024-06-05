package com.chs.yourapphistory.presentation.screen.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.presentation.Screen
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailScreen
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailViewModel
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppListScreenScreen
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppListViewModel
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
            Screen.ScreenUsedAppList
        } else {
            Screen.ScreenWelcome
        }
    ) {
        composable<Screen.ScreenWelcome> {
            WelcomeScreen {
                navController.navigate(it) {
                    popUpTo(Screen.ScreenWelcome) {
                        inclusive = true
                    }
                }
            }
        }

        composable<Screen.ScreenUsedAppList> {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(Screen.ScreenUsedAppList)
            }
            val viewmodel: UsedAppListViewModel = hiltViewModel(parentEntry)
            UsedAppListScreenScreen(
                state = viewmodel.state,
                onEvent = viewmodel::changeSortOption
            ) {
                selectPackage(it.packageName)
               navController.navigate(it)
            }
        }

        composable<Screen.ScreenAppUsageDetail> {
            val arg = it.toRoute<Screen.ScreenAppUsageDetail>()
            val parentEntry = remember(it) {
                navController.getBackStackEntry(arg)
            }
            val viewModel: AppUsageDetailViewModel = hiltViewModel(parentEntry)
            AppUsageDetailScreen(state = viewModel.state) {
                selectPackage(null)
                navController.navigateUp()
            }
        }
    }
}