package com.example.yourapphistory.presentation.screen

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

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
        viewModel.getAppInfoList()
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
            item {
                CircularProgressIndicator()
            }
//            if (state.appInfoList.isEmpty()) {
//                item {
//                    CircularProgressIndicator()
//                }
//            }
            itemsIndexed(state.appInfoList) { idx, list ->
                val expanded: Boolean = expandPos == idx
                ItemAppInfoSmall(
                    appUsagePair = list,
                    expanded = expanded
                ) { packageName ->
                    expandPos = if (expandPos == -1) {
                        idx
                    } else {
                        if (expandPos == idx) {
                            -1
                        } else idx
                    }
                }
            }
        }
    }
}

