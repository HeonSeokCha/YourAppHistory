package com.example.yourapphistory.presentation

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val context: Context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.targetDate) {
        if (state.targetDate != null) {
            viewModel.getAppInfoList(state.targetDate!!)
        }
    }

    Column {
        DateHeader(state.localDateList) {
            viewModel.changeDate(it)
            coroutineScope.launch {
                scrollState.scrollToItem(0, 0)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                horizontal = 8.dp,
                vertical = 8.dp
            )
        ) {
            items(state.appInfoList) {
                ItemAppInfoSmall(appInfo = it) { packageName ->

                }
            }
        }
    }

}

fun Long.toSimpleDateConvert(skipHour: Boolean = false): String {
    return if (skipHour) {
        SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(this)
    } else {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.KOREA).format(this)
    }
}
