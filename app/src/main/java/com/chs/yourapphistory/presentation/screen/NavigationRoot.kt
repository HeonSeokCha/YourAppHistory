package com.chs.yourapphistory.presentation.screen

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.defaultTransitionSpec
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailScreenRoot
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailViewModel
import com.chs.yourapphistory.presentation.screen.total_summary.TotalSummaryScreenRoot
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppListScreenScreenRoot
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppListViewModel
import com.chs.yourapphistory.presentation.screen.welcome.WelcomeScreenRoot
import org.koin.core.parameter.parametersOf
import org.koin.androidx.compose.koinViewModel


@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey>,
) {
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(
                targetOffsetX = { -it })
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(
                targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(
                targetOffsetX = { it })
        },
        entryProvider = entryProvider {
            entry<MainScreens.ScreenWelcome> {
                WelcomeScreenRoot(
                    viewModel = koinViewModel(),
                    onNavigateHome = {
                        backStack.removeLastOrNull()
                        backStack.add(MainScreens.ScreenTotalSummary)
                    }
                )
            }

            entry<MainScreens.ScreenUsedAppList> { key ->
                UsedAppListScreenScreenRoot(
                    viewModel = koinViewModel<UsedAppListViewModel> {
                        parametersOf(
                            key.sortType,
                            key.targetDateMilli
                        )
                    },
                    onClickApp = { info, date ->
                        backStack.add(
                            MainScreens.ScreenAppUsageDetail(
                                targetPackageName = info.packageName,
                                targetLabelName = info.label,
                                targetDateMilli = date
                            )
                        )
                    }
                )
            }

            entry<MainScreens.ScreenAppUsageDetail> { key ->
                val viewModel = koinViewModel<AppUsageDetailViewModel> {
                    parametersOf(
                        (key.targetPackageName to key.targetLabelName),
                        key.targetDateMilli
                    )
                }
                AppUsageDetailScreenRoot(viewModel = viewModel)
            }

            entry<MainScreens.ScreenTotalSummary> {
                TotalSummaryScreenRoot(
                    viewModel = koinViewModel(),
                    onNavigateUsageDetail = { packageName, label, targetDate ->
                        backStack.add(
                            MainScreens.ScreenAppUsageDetail(
                                targetPackageName = packageName,
                                targetLabelName = label,
                                targetDateMilli = targetDate
                            )
                        )
                    },
                    onNavigateUsedAppList = { targetDateMilli, type ->
                        backStack.add(
                            MainScreens.ScreenUsedAppList(
                                sortType = type,
                                targetDateMilli = targetDateMilli
                            )
                        )
                    }
                )
            }
        }
    )
}