package com.chs.yourapphistory.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.chs.yourapphistory.presentation.ScreenAppUsageDetail
import com.chs.yourapphistory.presentation.ScreenUsedAppList
import com.chs.yourapphistory.presentation.ScreenWelcome
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailScreenRoot
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailViewModel
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppListScreenScreenRoot
import com.chs.yourapphistory.presentation.screen.welcome.WelcomeScreenRoot
import org.koin.core.parameter.parametersOf
import org.koin.androidx.compose.koinViewModel


@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey>,
    selectPackage: (String?) -> Unit
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
            entry<ScreenWelcome> {
                WelcomeScreenRoot(
                    viewModel = koinViewModel(),
                    onNavigateHome = {
                        backStack.removeLastOrNull()
                        backStack.add(ScreenUsedAppList)
                    }
                )
            }

            entry<ScreenUsedAppList> {
                UsedAppListScreenScreenRoot(
                    viewModel = koinViewModel(),
                    onClickApp = { info, date ->
                        selectPackage(info.label)
                        backStack.add(
                            ScreenAppUsageDetail(
                                targetPackageName = info.packageName,
                                targetDate = date
                            )
                        )
                    }
                )
            }

            entry<ScreenAppUsageDetail> { key ->
                val viewModel = koinViewModel<AppUsageDetailViewModel> {
                    parametersOf(key.targetPackageName, key.targetDate)
                }
                AppUsageDetailScreenRoot(viewModel = viewModel)
            }
        }
    )
}