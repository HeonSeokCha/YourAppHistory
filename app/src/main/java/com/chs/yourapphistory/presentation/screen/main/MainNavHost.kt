package com.chs.yourapphistory.presentation.screen.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.chs.yourapphistory.presentation.Screen
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailScreen
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailViewModel
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppListScreenScreen
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppListViewModel
import com.chs.yourapphistory.presentation.screen.welcome.WelcomeScreen

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
            val viewmodel: UsedAppListViewModel = hiltViewModel()
            UsedAppListScreenScreen(
                state = viewmodel.state,
                onEvent = viewmodel::changeSortOption,
                selectPackageLabel = { selectPackage(it) }
            ) {
               navController.navigate(it)
            }
        }

        composable<Screen.ScreenAppUsageDetail> {
            val arg = it.toRoute<Screen.ScreenAppUsageDetail>()
            val parentEntry = remember(it) {
                navController.getBackStackEntry(arg)
            }
            val viewModel: AppUsageDetailViewModel = hiltViewModel(parentEntry)
            AppUsageDetailScreen(
                state = viewModel.state,
                onRefresh = {
                    viewModel.refreshUsageInfo()
                }
            ) {
                selectPackage(null)
                navController.navigateUp()
            }
        }
    }
}