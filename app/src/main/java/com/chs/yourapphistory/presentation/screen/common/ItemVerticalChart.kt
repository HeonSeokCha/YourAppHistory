package com.chs.yourapphistory.presentation.screen.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chs.yourapphistory.common.NiceNumUtil
import com.chs.yourapphistory.common.calculateScale
import com.chs.yourapphistory.common.chsLog
import com.chs.yourapphistory.common.convert24HourString
import com.chs.yourapphistory.common.convertBetweenHourString
import com.chs.yourapphistory.common.convertUsageUnitText
import com.chs.yourapphistory.common.isZero
import com.chs.yourapphistory.domain.model.UsageEventType
import com.chs.yourapphistory.presentation.theme.DashLineColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ItemDailyChart(
    hourUsageList: List<Pair<Int, Int>>,
    usageEventType: UsageEventType,
    clickText: DrawScope.(
        TextMeasurer,
        BarArea,
    ) -> Unit
) {
    val density = LocalDensity.current
    val textSize = with(density) { 10.sp.toPx() }
    val smallPadding = with(density) { 4.dp.toPx() }
    val labelSectionHeight = smallPadding.times(2) + textSize
    val topBasePadding = with(density) { 14.sp.toPx() + 21f }
    val barWidth = with(density) { 8.dp.toPx() }
    val barColor = MaterialTheme.colorScheme.primary

    val textMeasurer = rememberTextMeasurer()

    val style1 = TextStyle(
        fontSize = 10.sp,
        color = Color.Black
    )

    val basePadding = textMeasurer
        .measure(
            text = 0.convert24HourString(true),
            style = style1
        ).size
        .width
        .div(2)
        .toFloat()

    val niceNumber: List<Int> = NiceNumUtil.niceNum(
        value = hourUsageList.maxOf { it.second },
        usageEventType = usageEventType
    )

    val niceNumberMeasure = textMeasurer.measure(
        text = niceNumber.max().convertUsageUnitText(usageEventType),
        style = style1
    )


    val barAreas: MutableList<BarArea> = remember { mutableListOf() }

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
                onCompleted = { scope.launch { selectedPos = it } }
            )
    ) {
        val distance = (size.width - 8.dp.toPx() - niceNumberMeasure.size.width).div(24)
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
            hourUsageList.map { it.second }
        )
        val chartAreaBottom = size.height - labelSectionHeight

        hourUsageList.forEachIndexed { idx, info ->
            barAreas.add(
                BarArea(
                    idx = idx,
                    value = info.second,
                    xStart = distance.times(idx),
                    xEnd = distance.times(idx) + barWidth
                )
            )

            val barHeight = info.second.times(scale).toFloat()
            drawRoundRect(
                color = barColor,
                topLeft = Offset(
                    x = distance.times(idx),
                    y = size.height - barHeight - smallPadding - labelSectionHeight
                ),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            if (idx % 6 == 0 || idx == 23) {
                val time = if (idx == 23) "24(시)" else idx.toString()

                drawText(
                    textMeasurer = textMeasurer,
                    text = time,
                    topLeft = Offset(
                        x = distance.times(idx),
                        y = chartAreaBottom
                    ),
                    style = style1
                )
            }
        }

        if (selectedBar != null) {
            val barHeight = (size.height - selectedBar!!.value.times(scale)
                .toFloat() - smallPadding - labelSectionHeight)

            drawLine(
                color = DashLineColor,
                start = Offset(selectedBar!!.xStart + barWidth.div(2), 50f),
                end = Offset(selectedBar!!.xStart + barWidth.div(2), barHeight),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(6.dp.toPx(), 4.dp.toPx()),
                    phase = 0f
                )
            )

            clickText(
                textMeasurer,
                selectedBar!!,
            )
        }
    }
}

@Composable
fun DailyUsageChart(
    title: String,
    subTitle: String? = null,
    list: List<Pair<Int, Int>>,
    usageEventType: UsageEventType,
    convertText: (Int) -> String
) {
    val barColor = MaterialTheme.colorScheme.onTertiary
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            modifier = Modifier
                .padding(
                    top = 8.dp, start = 8.dp
                ),
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        if (subTitle != null) {

            Text(
                modifier = Modifier
                    .padding(
                        top = 8.dp,
                        start = 8.dp
                    ),
                text = subTitle,
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        ItemDailyChart(
            hourUsageList = list,
            usageEventType = usageEventType
        ) { textMeasurer, selectedBar ->


            val selectValue = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        baselineShift = BaselineShift(+0.1f),
                        fontSize = 12.sp
                    )
                ) {
                    append(selectedBar.idx.convertBetweenHourString() + " ")
                }

                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                ) {
                    append(convertText(selectedBar.value))
                }
            }

            val selectValueMeasurer: TextLayoutResult = textMeasurer.measure(selectValue)

            val textRectPadding =
                selectedBar.xStart - ((selectValueMeasurer.size.width.div(24)) * selectedBar.idx)

            drawRoundRect(
                color = barColor,
                topLeft = Offset(
                    textRectPadding,
                    -21f
                ),
                size = Size(
                    selectValueMeasurer.size.width + 12.dp.toPx(),
                    selectValueMeasurer.size.height.toFloat() + 21f
                ),
                cornerRadius = CornerRadius(50f)
            )

            drawText(
                textMeasurer = textMeasurer,
                text = selectValue,
                topLeft = Offset(
                    textRectPadding + 20f,
                    -9f
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

fun Modifier.tapOrPress(
    onCompleted: (offsetX: Float) -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.pointerInput(interactionSource) {
        awaitEachGesture {
            val tap = awaitFirstDown()
                .also { if (it.pressed != it.previousPressed) it.consume() }
            val up = waitForUpOrCancellation()
            if (up != null) {
                if (up.pressed != up.previousPressed) up.consume()
                onCompleted(tap.position.x)
            }
        }
    }
}

@Preview
@Composable
private fun PreViewUsageChart() {
    val usageMap = object : HashMap<Int, Int>() {
        init {
            for (i in 1..24) {
                put(i, i)
            }
        }
    }.toList()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        DailyUsageChart(
            title = "TEST",
            list = usageMap,
            usageEventType = UsageEventType.UsageEvent,
            convertText = { a -> "$a 개" }
        )
    }
}