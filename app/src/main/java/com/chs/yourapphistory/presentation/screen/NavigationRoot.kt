package com.chs.yourapphistory.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailScreenRoot
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailViewModel
import com.chs.yourapphistory.presentation.screen.total_summary.TotalSummaryScreenRoot
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppListScreenScreenRoot
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

            entry<MainScreens.ScreenUsedAppList> {
                UsedAppListScreenScreenRoot(
                    viewModel = koinViewModel(),
                    onClickApp = { info, date ->
                        backStack.add(
                            MainScreens.ScreenAppUsageDetail(
                                targetPackageName = info.packageName,
                                targetLabelName = info.label,
                                targetDate = date
                            )
                        )
                    }
                )
            }

            entry<MainScreens.ScreenAppUsageDetail> { key ->
                val viewModel = koinViewModel<AppUsageDetailViewModel> {
                    parametersOf(
                        (key.targetPackageName to key.targetLabelName),
                        key.targetDate
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
                                targetDate = targetDate
                            )
                        )
                    },
                    onNavigateUsedAppList = { targetDate, type ->
                        backStack.add(
                            MainScreens.ScreenUsedAppList(type)
                        )
                    }
                )
            }
        }
    )
}