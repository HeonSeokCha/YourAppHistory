package com.example.yourapphistory.presentation

import android.content.Context
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val context: Context = LocalContext.current
    var openDatePicker by remember { mutableStateOf(false) }
    val usageList by remember { mutableStateOf(Util.getLauncherAppInfoList(context)) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            horizontal = 8.dp,
            vertical = 8.dp
        )
    ) {
        item {
            DateHeader {

            }
        }

        items(usageList) {
            ItemAppInfoSmall(appInfo = it) { packageName ->

            }
        }
    }

    if (openDatePicker) {
        SampleDatePickerView { start, end ->
            viewModel.initStartDate(start)
            viewModel.initEndDate(end)
            openDatePicker = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleDatePickerView(
    selectDateTimes: (Long?, Long?) -> Unit
) {
    val cal = Calendar.getInstance()
    val dateRangePickerState = remember {
        DateRangePickerState(
            initialSelectedStartDateMillis = cal.timeInMillis,
            initialDisplayedMonthMillis = null,
            initialSelectedEndDateMillis = cal.apply {
                this.add(Calendar.DAY_OF_MONTH, 1)
            }.timeInMillis,
            initialDisplayMode = DisplayMode.Picker,
            yearRange = (2023 .. 2024)

        )
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = { coroutineScope.launch {
            if (dateRangePickerState.selectedStartDateMillis != null
                && dateRangePickerState.selectedEndDateMillis != null) {

                selectDateTimes(
                    cal.apply {
                        this.timeInMillis = dateRangePickerState.selectedStartDateMillis!!
                        this.set(Calendar.HOUR_OF_DAY, 0)
                        this.set(Calendar.MINUTE, 0)
                        this.set(Calendar.SECOND, 0)
                        this.set(Calendar.MILLISECOND, 1)
                    }.timeInMillis,
                    cal.apply {
                        this.timeInMillis = dateRangePickerState.selectedEndDateMillis!!
                        this.set(Calendar.HOUR_OF_DAY, 23)
                        this.set(Calendar.MINUTE, 59)
                        this.set(Calendar.SECOND, 59)
                        this.set(Calendar.MILLISECOND, 999)
                    }.timeInMillis,
                )
            } else {
                selectDateTimes(null, null)
            }
            bottomSheetState.hide()
        }},
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
                    .background(Color.White)
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    modifier = Modifier,
                    dateFormatter = DatePickerFormatter("yy MM dd", "yy MM dd", "yy MM dd"),
                    title = {
                        Text(text = "Select date range to assign the chart", modifier = Modifier
                            .padding(16.dp))
                    },
                    headline = {
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)) {
                            Box(Modifier.weight(1f)) {
                                (if(dateRangePickerState.selectedStartDateMillis!=null) dateRangePickerState.selectedStartDateMillis?.toSimpleDateConvert(true) else "Start Date")?.let { Text(text = it) }
                            }
                            Box(Modifier.weight(1f)) {
                                (if(dateRangePickerState.selectedEndDateMillis!=null) dateRangePickerState.selectedEndDateMillis?.toSimpleDateConvert(true) else "End Date")?.let { Text(text = it) }
                            }
                            Box(Modifier.weight(0.2f)) {
                                Icon(imageVector = Icons.Default.Done, contentDescription = "Okk")
                            }

                        }
                    },
                    showModeToggle = true
                )

                Button(
                    onClick = {
                        if (dateRangePickerState.selectedStartDateMillis == null
                            || dateRangePickerState.selectedEndDateMillis == null) {
                            return@Button
                        } else {
                            coroutineScope.launch {
                                selectDateTimes(
                                    cal.apply {
                                        this.timeInMillis = dateRangePickerState.selectedStartDateMillis!!
                                        this.set(Calendar.HOUR_OF_DAY, 0)
                                        this.set(Calendar.MINUTE, 0)
                                        this.set(Calendar.SECOND, 0)
                                        this.set(Calendar.MILLISECOND, 1)
                                    }.timeInMillis,
                                    cal.apply {
                                        this.timeInMillis = dateRangePickerState.selectedEndDateMillis!!
                                        this.set(Calendar.HOUR_OF_DAY, 23)
                                        this.set(Calendar.MINUTE, 59)
                                        this.set(Calendar.SECOND, 59)
                                        this.set(Calendar.MILLISECOND, 999)
                                    }.timeInMillis,
                                )
                                bottomSheetState.hide()
                            }
                        }

                    },
                    colors = ButtonDefaults.buttonColors( contentColor = Color.White),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp)
                ) {
                    Text("Done", color = Color.White)
                }
            }
        }
    )
}

@Composable
fun ItemTime(
    start: Long?,
    end: Long?,
    clickAble: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable {
                clickAble()
            }
    ) {
        DatePickerText(title = "BeginDate", time = start)

        DatePickerText(title = "EndDate", time = end)
    }
}


@Composable
private fun DatePickerText(
    title: String,
    time: Long?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title
        )

        Text(
            text = time?.toSimpleDateConvert() ?: "NULL"
        )
    }
}

@Composable
fun ItemAppUsageInfo(
    appUsageInfo: AppUsageInfo
) {

}

fun Long.toSimpleDateConvert(skipHour: Boolean = false): String {
    return if (skipHour) {
        SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(this)
    } else {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.KOREA).format(this)
    }
}
