package com.chs.yourapphistory.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.toLocalDate
import com.chs.yourapphistory.presentation.ScreenAppUsageDetail
import com.chs.yourapphistory.presentation.ScreenUsedAppList
import com.chs.yourapphistory.presentation.ScreenWelcome
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailScreenRoot
import com.chs.yourapphistory.presentation.screen.app_usage_detail.AppUsageDetailViewModel
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppListScreenScreenRoot
import com.chs.yourapphistory.presentation.screen.welcome.WelcomeScreenRoot
import java.time.LocalDate


@Composable
fun NavigationRoot(
    modifier: Modifier = Modifier,
    isGrantPermission: Boolean,
    selectPackage: (String?) -> Unit
) {
    val backstack = rememberNavBackStack().apply {
        this.clear()
        if (isGrantPermission)
            this.add(ScreenUsedAppList)
        else
            this.add(ScreenWelcome)
    }

    NavDisplay(
        modifier = modifier,
        backStack = backstack,
        onBack = { backstack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<ScreenWelcome> {
                WelcomeScreenRoot(
                    viewModel = hiltViewModel(),
                    onNavigateHome = {
                        backstack.removeLastOrNull()
                        backstack.add(ScreenUsedAppList)
                    }
                )
            }

            entry<ScreenUsedAppList> {
                UsedAppListScreenScreenRoot(
                    viewModel = hiltViewModel(),
                    onClickApp = { info, date ->
                        selectPackage(info.label)
                        backstack.add(
                            ScreenAppUsageDetail(
                                targetPackageName = info.packageName,
                                targetDate = date
                            )
                        )
                    }
                )
            }

            entry<ScreenAppUsageDetail> { key ->
                val viewModel = hiltViewModel<AppUsageDetailViewModel, AppUsageDetailViewModel.Factory>(
                    creationCallback = { factory ->
                        factory.create(key.targetPackageName, key.targetDate.toLocalDate())
                    }
                )
                AppUsageDetailScreenRoot(
                    viewModel = viewModel,
                    onBack = { backstack.removeLastOrNull() }
                )
            }
        }
    )
}