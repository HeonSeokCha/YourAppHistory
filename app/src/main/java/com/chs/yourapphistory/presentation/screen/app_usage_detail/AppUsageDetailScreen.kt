package com.chs.yourapphistory.presentation.screen.app_usage_detail

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.common.calculateSplitHourUsage
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.presentation.screen.common.ItemVerticalChart
import java.time.LocalDate

@Composable
fun AppUsageDetailScreen(
    packageName: String,
    date: LocalDate,
    viewModel: AppUsageDetailViewModel = hiltViewModel()
) {
    val context: Context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectHourUsageTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(viewModel, context) {
        viewModel.changeDate(date)
    }

    LaunchedEffect(state.targetDate) {
        viewModel.getDayAppUsageList(
            packageName = packageName,
            state.targetDate
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {

        item {
            Text(state.dayUsageList.sumOf { it.second }.convertToRealUsageTime())
        }

        item {
            ItemVerticalChart(hourUsageList = state.dayUsageList) {
                selectHourUsageTime = it
            }
        }
    }
}