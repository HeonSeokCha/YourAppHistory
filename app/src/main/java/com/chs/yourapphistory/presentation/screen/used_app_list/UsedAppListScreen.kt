package com.chs.yourapphistory.presentation.screen.used_app_list

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chs.yourapphistory.domain.model.AppInfo
import com.chs.yourapphistory.domain.model.SortType
import com.chs.yourapphistory.presentation.screen.common.FilterDialog
import com.chs.yourapphistory.presentation.screen.common.placeholder

@Composable
fun UsedAppListScreenScreenRoot(
    viewModel: UsedAppListViewModel,
    onClickApp: (AppInfo, Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UsedAppEffect.NavigateAppDetail -> onClickApp(effect.appInfo, effect.targetDate)
                is UsedAppEffect.ShowPagingError -> {
                    Toast.makeText(
                        context,
                        "Something wrong load Used Info..",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    UsedAppListScreenScreen(
        state = state,
        onIntent = viewModel::handleIntent
    )
}

@Composable
fun UsedAppListScreenScreen(
    state: UsedAppListState,
    onIntent: (UsedAppIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .placeholder(visible = state.isLoading),
            text = state.displayDate,
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            modifier = Modifier
                .clickable {
                    if (state.isLoading) return@clickable
                    onIntent(UsedAppIntent.OnShowSortDialog(true))
                }
                .placeholder(visible = state.isLoading),
            text = state.sortOption.title,
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading) {
                items(20) {
                    ItemAppInfoSmall(null, null) { }
                }
            } else {
                items(
                    count = state.list.count(),
                    key = { state.list[it].first.packageName }
                ) { idx ->
                    val appInfo = state.list[idx]
                    ItemAppInfoSmall(
                        usedAppInfo = appInfo,
                        icon = appInfo.first.icon,
                        sortOption = state.sortOption
                    ) { appInfo ->
                        onIntent(UsedAppIntent.ClickAppInfo(appInfo = appInfo))
                    }
                }
            }
        }
    }

    if (state.isShowFilterDialog) {
        FilterDialog(
            list = SortType.entries.toList(),
            onDismiss = {
                onIntent(UsedAppIntent.OnShowSortDialog(false))
            }, onClick = { selectSortType ->
                onIntent(UsedAppIntent.OnChangeSort(selectSortType))
            }
        )
    }
}