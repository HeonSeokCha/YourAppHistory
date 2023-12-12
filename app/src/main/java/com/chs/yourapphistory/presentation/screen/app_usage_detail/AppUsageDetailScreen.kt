package com.chs.yourapphistory.presentation.screen.app_usage_detail

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chs.yourapphistory.common.Constants
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
    var selectHourUsageTime by remember { mutableStateOf("") }

    LaunchedEffect(viewModel, context) {
        viewModel.getDayAppUsageList(
            packageName = packageName,
            date
        )
    }
//
//    LaunchedEffect(state.targetDate) {
//
//    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (state.dayUsageList.isNotEmpty()) {
            item {
                Text(
                    text = if (date == LocalDate.now()) {
                        "오늘"
                    } else {
                       date.format(Constants.DATE_FORMAT)
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = state.dayUsageList.sumOf { it.second }.convertToRealUsageTime(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                ItemVerticalChart(state.dayUsageList) {
                    selectHourUsageTime =
                        "${it.first} ~ ${it.first + 1} : ${it.second.convertToRealUsageTime()}"
                }

                if (selectHourUsageTime.isNotEmpty()) {
                    Text(text = selectHourUsageTime)
                }
            }
        }
    }
}