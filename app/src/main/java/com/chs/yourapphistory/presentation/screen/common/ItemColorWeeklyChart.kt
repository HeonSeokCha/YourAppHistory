package com.chs.yourapphistory.presentation.screen.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.toInt
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chs.yourapphistory.common.calculateScale
import com.chs.yourapphistory.common.isZero
import com.chs.yourapphistory.common.toConvertDisplayDay
import com.chs.yourapphistory.domain.model.AppTotalUsageInfo
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours

@Composable
fun ItemColorWeeklyChart(
    list: List<Pair<LocalDate, List<AppTotalUsageInfo>>>,
    onClick: (Int) -> Unit
) {
    val density = LocalDensity.current
    val smallPadding = with(density) { 4.dp.toPx() }
    val topBasePadding = with(density) { 14.sp.toPx() + 21f }
    val barWidth = with(density) { 16.dp.toPx() }
    val distance = with(density) {
        (LocalConfiguration.current.screenWidthDp - 25).div(7).dp.toPx()
    }
    val barColor = MaterialTheme.colorScheme.primary

    val textMeasurer = rememberTextMeasurer()

    val style1 = TextStyle(
        fontSize = 10.sp,
        color = Color.Black
    )

    val basePadding = textMeasurer
        .measure(
            text = DayOfWeek.SUNDAY.getDisplayName(
                java.time.format.TextStyle.SHORT,
                Locale.KOREAN
            ),
            style = style1
        ).size
        .width
        .toFloat()

    val barAreas = list.mapIndexed { idx, pair ->
        BarArea(
            idx = idx,
            value = pair.second.sumOf { it.totalUsedInfo.toInt() },
            xStart = distance.times(idx) + barWidth + smallPadding.times(1),
            xEnd = distance.times(idx) + barWidth + smallPadding.times(1) + barWidth
        )
    }

    var selectedBar: BarArea? by remember { mutableStateOf(null) }
    var selectedPos by remember { mutableFloatStateOf(0f) }


    LaunchedEffect(barAreas) {
        selectedBar = null
    }

    LaunchedEffect(selectedPos) {
        val findBar = barAreas.find { selectedPos in it.xStart..it.xEnd }
        selectedBar = if (findBar?.value.isZero()) {
            null
        } else findBar
    }

    val scope = rememberCoroutineScope()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 4.dp)
            .tapOrPress(
                onStart = { },
                onCancel = { },
                onCompleted = { scope.launch { selectedPos = it } }
            )
    ) {
        if (size.height != 0f) {
            repeat(3) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(basePadding.div(2), ((size.height / 3) * it) + topBasePadding),
                    end = Offset(
                        size.width - basePadding.div(2),
                        ((size.height / 3) * it) + topBasePadding
                    )
                )
            }
        }
        val scale = calculateScale(
            (size.height - smallPadding - topBasePadding).roundToInt(),
            list.map { it.second.sumOf { it.totalUsedInfo.toInt() } }
        )
        val chartAreaBottom = size.height

        val sortedList = barAreas.sortedByDescending { it.value }

        val topTotalPadding = sortedList.take(3).sumOf { it.value }.times(scale).toFloat()
        val colorList = listOf(
            Color.Blue,
            Color.Cyan,
            Color.Green
        )

        sortedList.forEachIndexed { idx, info ->
            val barHeight = info.value.times(scale).toFloat()
            val convertDateFormat = list[idx].first.toConvertDisplayDay()

            repeat(3) {
                drawRoundRect(
                    color = colorList[it],
                    topLeft = Offset(
                        x = distance.times(idx) + barWidth + smallPadding.times(1),
                        y = size.height - barHeight - smallPadding
                    ),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }

            drawRoundRect(
                color = barColor,
                topLeft = Offset(
                    x = distance.times(idx) + barWidth + smallPadding.times(1),
                    y =  barHeight - smallPadding
                ),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            val textResult = textMeasurer.measure(
                text = convertDateFormat,
                style = style1
            )
            val textRectPadding = (distance.times(idx)) + (textResult.size.width) + (smallPadding * 3)
            drawText(
                textMeasurer = textMeasurer,
                text = convertDateFormat,
                topLeft = Offset(
                    x = textRectPadding,
                    y = chartAreaBottom
                ),
                style = style1
            )
        }

        if (selectedBar == null) return@Canvas
        onClick(selectedBar!!.idx)
    }
}

@Composable
fun WeeklyColorUsageChart(
    title: String,
    subTitle: String? = null,
    list: List<Pair<LocalDate, List<AppTotalUsageInfo>>>,
    onClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .padding(
                    top = 8.dp,
                    start = 8.dp
                ),
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (subTitle != null) {
            Text(
                modifier = Modifier
                    .padding(
                        top = 8.dp,
                        start = 8.dp
                    ),
                text = subTitle,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        ItemColorWeeklyChart(
            list = list,
            onClick = onClick
        )
    }
}

@Preview
@Composable
private fun PreViewUsageChart3() {
    val usageMap = object : HashMap<LocalDate, List<AppTotalUsageInfo>>() {
        init {
            repeat(7) {
                put(
                    LocalDate.now().minusDays(it.toLong()),
                    listOf(
                        AppTotalUsageInfo(
                            packageName = "com.chs.123",
                            label = "123",
                            totalUsedInfo = 1.hours.inWholeMilliseconds
                        ),
                        AppTotalUsageInfo(
                            packageName = "com.chs.123",
                            label = "123",
                            totalUsedInfo = 1.hours.inWholeMilliseconds
                        ),
                        AppTotalUsageInfo(
                            packageName = "com.chs.123",
                            label = "123",
                            totalUsedInfo = 1.hours.inWholeMilliseconds
                        ),
                        AppTotalUsageInfo(
                            packageName = "com.chs.123",
                            label = "123",
                            totalUsedInfo = 1.hours.inWholeMilliseconds
                        )
                    )
                )
            }
        }
    }.toList()


    WeeklyColorUsageChart(
        title = "TEST",
        list = usageMap,
        onClick = {}
    )
}
