package com.chs.yourapphistory.presentation.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val context: Context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()
    var expandPos by remember { mutableIntStateOf(-1) }

    LaunchedEffect(state.targetDate) {
        expandPos = -1
        scrollState.scrollToItem(0, 0)
        viewModel.getDayUseAppInfoList(state.targetDate)
    }

    LaunchedEffect(state.appInfoList) {
        expandPos = -1
        scrollState.scrollToItem(0, 0)
    }

    Column {
        if (state.localDateList.isNotEmpty()) {
            DateHeader(state.localDateList) {
                viewModel.changeDate(it)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                horizontal = 8.dp,
                vertical = 8.dp
            )
        ) {
            if (state.isLoading) {
                item {
                    CircularProgressIndicator()
                }
            }

            itemsIndexed(state.appInfoList) { idx, list ->
                val expanded: Boolean = expandPos == idx
                ItemAppInfoSmall(
                    usedAppInfo = list,
                    expanded = expanded,
                    isLoading = state.isLoading,
                    appUsageInfoList = state.appUsageList
                ) { packageName ->
                    expandPos = if (expandPos != idx) {
                        viewModel.getDayAppUsageInfoList(
                            date = state.targetDate,
                            packageName = packageName
                        )
                        idx
                    } else -1
                }
            }
        }
    }
}

