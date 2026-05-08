package com.chs.yourapphistory.presentation.screen.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chs.yourapphistory.common.NiceNumUtil
import com.chs.yourapphistory.common.calculateScale
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.convertToRealUsageHour
import com.chs.yourapphistory.common.convertUsageUnitText
import com.chs.yourapphistory.common.isZero
import com.chs.yourapphistory.common.toConvertDisplayDay
import com.chs.yourapphistory.domain.model.AppTotalUsageInfo
import com.chs.yourapphistory.domain.model.UsageEventType
import com.chs.yourapphistory.presentation.theme.RankBlue
import com.chs.yourapphistory.presentation.theme.RankGreen
import com.chs.yourapphistory.presentation.theme.RankGreen2
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours

@Composable
fun ItemColorWeeklyChart(
    list: List<Pair<LocalDate, List<AppTotalUsageInfo>>>,
    colorList: List<Color>,
    usageEventType: UsageEventType,
    selectIdx: Int,
    onClick: (Int) -> Unit
) {
    val density = LocalDensity.current
    val textSize = with(density) { 10.sp.toPx() }
    val smallPadding = with(density) { 4.dp.toPx() }
    val labelSectionHeight = smallPadding.times(2) + textSize
    val topBasePadding = with(density) { 14.sp.toPx() + 21f }
    val barWidth = with(density) { 16.dp.toPx() }
    val barColor = Color.Gray

    val textMeasurer = rememberTextMeasurer()

    val style1 = TextStyle(
        fontSize = 11.sp,
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

    val niceNumber: List<Int> = NiceNumUtil.niceNum(
        value = list.maxOf { it.second.sumOf { it.totalUsedInfo } }.toInt(),
        usageEventType = usageEventType
    )

    val niceNumberMeasure = textMeasurer.measure(
        text = niceNumber.max().convertUsageUnitText(usageEventType),
        style = style1
    )

    val barAreas: MutableList<BarAreas> = remember { mutableListOf() }

    var selectedBar: BarAreas? by remember { mutableStateOf(null) }
    var selectedPos by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(barAreas) {
        selectedBar = null
    }

    LaunchedEffect(selectIdx) {
        onClick(selectIdx)
    }

    LaunchedEffect(selectedPos) {
        val findBar = barAreas.find { selectedPos in it.xStart..it.xEnd }
        selectedBar = if (findBar?.values.isNullOrEmpty()) {
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
                onCompleted = { scope.launch { selectedPos = it } }
            )
    ) {
        val distance = (size.width - niceNumberMeasure.size.width).div(7)

        if (size.height != 0f) {
            repeat(3) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(
                        8.dp.toPx(),
                        ((size.height / 3) * it) + topBasePadding
                    ),
                    end = Offset(
                        size.width - 16.dp.toPx() - niceNumberMeasure.size.width,
                        ((size.height / 3) * it) + topBasePadding
                    )
                )

                if (niceNumber.isNotEmpty() && it != 2) {
                    val measure = textMeasurer.measure(
                        text = niceNumber[(2 - it)].convertUsageUnitText(usageEventType),
                        style = style1
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = niceNumber[(2 - it)].convertUsageUnitText(usageEventType),
                        topLeft = Offset(
                            size.width - basePadding.div(2) - measure.size.width,
                            ((size.height / 3) * it) + topBasePadding - (measure.size.height / 2)
                        ),
                        style = style1
                    )
                }
            }
        }

        val scale = calculateScale(
            (size.height - smallPadding - topBasePadding).roundToInt(),
            list.map { it.second.sumOf { it.totalUsedInfo.toInt() } }
        )

        val chartAreaBottom = size.height - labelSectionHeight


        list.forEachIndexed { idx, info ->
            barAreas.add(
                BarAreas(
                    idx = idx,
                    values = info.second.map { it.totalUsedInfo.toInt() },
                    xStart = distance.times(idx) + 16.dp.toPx(),
                    xEnd = distance.times(idx) + 16.dp.toPx() + barWidth
                )
            )
            val convertDateFormat = list[idx].first.toConvertDisplayDay()
            val sortedInfo = barAreas[idx].values.sortedDescending()
            if (barAreas[idx].values.isNotEmpty()) {
                var temp = 0f
                val totalBarHeight = sortedInfo.drop(3).sum().times(scale).toFloat()

                if (barAreas[idx].values.size > 3) {
                    repeat(3) {
                        val barHeight = sortedInfo[it].times(scale).toFloat()
                        drawRoundRect(
                            color = colorList[it],
                            topLeft = Offset(
                                x = barAreas[idx].xStart,
                                y = size.height - barHeight - smallPadding - labelSectionHeight - temp
                            ),
                            size = Size(barWidth, barHeight)
                        )
                        temp += barHeight
                    }
                }

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(
                        x = barAreas[idx].xStart,
                        y = size.height - totalBarHeight - smallPadding - labelSectionHeight - temp
                    ),
                    size = Size(barWidth, totalBarHeight)
                )
            }

            if (idx == selectIdx) {
                drawCircle(
                    center = Offset(
                        x = barAreas[idx].xStart + 8.dp.toPx(),
                        y = size.height - (labelSectionHeight / 2)
                    ),
                    radius = 30f,
                    color = Color.LightGray.copy(alpha = 0.9f)
                )
            }

            drawText(
                textMeasurer = textMeasurer,
                text = convertDateFormat,
                topLeft = Offset(
                    x = barAreas[idx].xStart + 3.dp.toPx(),
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
    title: AnnotatedString,
    subTitle: String? = null,
    list: List<Pair<LocalDate, List<AppTotalUsageInfo>>>,
    usageType: UsageEventType,
    selectIdx: Int,
    onBarClick: (Int) -> Unit,
    onPackageClick: (String, String) -> Unit,
    onButtonClick: () -> Unit
) {
    val colorList = listOf(
        RankBlue,
        RankGreen,
        RankGreen2
    )

    val topValuePackageList =
        list[selectIdx].second.sortedByDescending { it.totalUsedInfo }
            .take(3)
            .map {
                if (usageType == UsageEventType.UsageEvent) {
                    it to it.totalUsedInfo.toInt().convertToRealUsageHour()
                } else {
                    it to "${it.totalUsedInfo}개"
                }
            }

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
        )

        if (subTitle != null) {
            Text(
                modifier = Modifier
                    .padding(
                        top = 8.dp,
                        start = 8.dp
                    ),
                text = subTitle,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        ItemColorWeeklyChart(
            list = list,
            colorList = colorList,
            selectIdx = selectIdx,
            usageEventType = usageType,
            onClick = onBarClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        repeat(topValuePackageList.count()) {
            ItemDailyTotalInfo(
                color = colorList[it],
                label = topValuePackageList[it].first.label,
                value = topValuePackageList[it].second,
                onClick = {
                    onPackageClick(
                        topValuePackageList[it].first.packageName,
                        topValuePackageList[it].first.label,
                    )
                }
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider()

        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            Text(
                text = "모두 보기",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ItemDailyTotalInfo(
    color: Color,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = color,
                    shape = CircleShape
                )
        )

        Text(
            modifier = Modifier
                .weight(0.6f),
            text = label
        )

        Text(
            modifier = Modifier
                .weight(0.3f),
            text = value,
            textAlign = TextAlign.End
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
                            totalUsedInfo = 4.hours.inWholeMilliseconds
                        ),
                        AppTotalUsageInfo(
                            packageName = "com.chs.123",
                            label = "123",
                            totalUsedInfo = 10.hours.inWholeMilliseconds
                        ),
                        AppTotalUsageInfo(
                            packageName = "com.chs.123",
                            label = "123",
                            totalUsedInfo = 3.hours.inWholeMilliseconds
                        ),
                        AppTotalUsageInfo(
                            packageName = "com.chs.123",
                            label = "123",
                            totalUsedInfo = 2.hours.inWholeMilliseconds
                        )
                    )
                )
            }
        }
    }.toList()


    WeeklyColorUsageChart(
        title = buildAnnotatedString { append("TEST") },
        subTitle = "SubTEst",
        list = usageMap,
        usageType = UsageEventType.UsageEvent,
        selectIdx = 0,
        onBarClick = {},
        onPackageClick = { _, _ ->},
        onButtonClick = {}
    )
}
