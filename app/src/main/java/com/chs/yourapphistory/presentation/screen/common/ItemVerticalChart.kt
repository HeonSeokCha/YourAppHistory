package com.chs.yourapphistory.presentation.screen.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chs.yourapphistory.common.calculateScale
import com.chs.yourapphistory.common.convert24HourString
import com.chs.yourapphistory.common.convertBetweenHourString
import com.chs.yourapphistory.common.convertToRealUsageMinutes
import com.chs.yourapphistory.common.convertToRealUsageTime
import com.chs.yourapphistory.common.isZero
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ItemVerticalChart(
    hourUsageList: List<Pair<Int, Long>>,
    clickText: DrawScope.(
        TextMeasurer,
        BarArea,
    ) -> Unit
) {
    val density = LocalDensity.current
    val textSize = with(density) { 10.sp.toPx() }
    val smallPadding = with(density) { 4.dp.toPx() }
    val labelSectionHeight = smallPadding.times(2) + textSize
    val barWidth = with(density) { 6.dp.toPx() }
    val distance = with(density) {
        (LocalConfiguration.current.screenWidthDp - 16).div(24).dp.toPx()
    }

    val horizontalPadding = (distance - barWidth)
    val barAreas = hourUsageList.mapIndexed { idx, pair ->
        BarArea(
            idx = idx,
            value = pair.second,
            xStart = horizontalPadding + distance.times(idx) - distance.div(2),
            xEnd = horizontalPadding + distance.times(idx) + distance.div(2)
        )
    }

    var selectedBar: BarArea? by remember { mutableStateOf(null) }
    var selectedPos by remember { mutableFloatStateOf(barAreas.first().xStart.plus(1f)) }

    val textMeasurer = rememberTextMeasurer()

    val style1 = TextStyle(
        fontSize = 10.sp,
        color = Color.Black
    )


    LaunchedEffect(barAreas) {
        selectedBar = null
    }

    LaunchedEffect(selectedPos) {
        val findBar = barAreas.find { selectedPos in it.xStart..it.xEnd }
        selectedBar = if (findBar?.value.isZero()) {
            null
        } else findBar
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(
                start = 8.dp,
                end = 8.dp
            )
    ) {
        val scope = rememberCoroutineScope()
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .tapOrPress(
                    onStart = { position ->
                    },
                    onCancel = { position ->
                    },
                    onCompleted = {
                        scope.launch {
                            selectedPos = it
                        }
                    }

                )
        ) {
            repeat(3) {
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, ((100.dp.toPx() * (it) + 10))),
                    end = Offset(size.width, ((100.dp.toPx() * (it) + 10)))
                )
            }
            val scale = calculateScale(
                (size.height - smallPadding).roundToInt(),
                hourUsageList.map { it.second }
            )
            val chartAreaBottom = size.height - labelSectionHeight

            barAreas.forEachIndexed { idx, info ->
                val barHeight = info.value.times(scale).toFloat()
                drawRoundRect(
                    color = Color.LightGray,
                    topLeft = Offset(
                        x = horizontalPadding + distance.times(idx) - barWidth.div(2),
                        y = size.height - barHeight - smallPadding - labelSectionHeight
                    ),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                if (info.idx % 6 == 0 || info.idx == 23) {
                    val textResult = textMeasurer.measure(info.idx.convert24HourString(true))
                    val textRectPadding = info.xStart - (textResult.size.width.div(48) * info.idx)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = if (info.idx == 23) {
                            (info.idx + 1).convert24HourString(true)
                        } else {
                            info.idx.convert24HourString(true)
                        },
                        topLeft = Offset(
                            x = textRectPadding,
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
                    color = Color.Black,
                    start = Offset(selectedBar!!.xStart + distance.div(2), 50f),
                    end = Offset(selectedBar!!.xStart + distance.div(2), barHeight),
                    strokeWidth = 4f
                )

                clickText(
                    textMeasurer,
                    selectedBar!!,
                )

            }
        }
    }
}

@Composable
fun UsageTimeZoneChart(
    list: List<Pair<Int, Long>>
) {
    ItemVerticalChart(hourUsageList = list) { textMeasurer, selectedBar ->
        val selectTimZoneValue: String = selectedBar.idx.convertBetweenHourString()
        val selectTimeMeasurer: TextLayoutResult = textMeasurer.measure(selectTimZoneValue)
        val selectValue = selectedBar.value.convertToRealUsageMinutes()
        val selectValueMeasurer: TextLayoutResult = textMeasurer.measure(
            selectValue,
            TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
        )
        val totalWidth: Float = (selectTimeMeasurer.size.width + selectValueMeasurer.size.width).toFloat()

        val textRectPadding = selectedBar.xStart - ((totalWidth.div(24)) * selectedBar.idx)

        drawRoundRect(
            color = Color.LightGray,
            topLeft = Offset(
                textRectPadding,
                0f
            ),
            size = Size(
                totalWidth + 12.dp.toPx(),
                selectValueMeasurer.size.height.toFloat() + 21f
            ),
            cornerRadius = CornerRadius(50f)
        )

        drawText(
            textMeasurer = textMeasurer,
            text = selectTimZoneValue,
            style = TextStyle(
                fontSize = 12.sp,
                color = Color.Black
            ),
            topLeft = Offset(
                textRectPadding + 20f,
                12f
            )
        )

        drawText(
            textMeasurer = textMeasurer,
            text = selectValue,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            topLeft = Offset(
                textRectPadding + 20f + selectTimeMeasurer.size.width.toFloat(),
                6f
            )
        )
    }
}

@Composable
fun UsageLaunchCountChart(
    list: List<Pair<Int, Long>>
) {
    ItemVerticalChart(hourUsageList = list) { textMeasurer, selectedBar ->
        val selectTimZoneValue: String = selectedBar.idx.convertBetweenHourString()
        val selectTimeMeasurer: TextLayoutResult = textMeasurer.measure(selectTimZoneValue)
        val selectValue = "${selectedBar.value}회"
        val selectValueMeasurer: TextLayoutResult = textMeasurer.measure(
            selectValue,
            TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
        )
        val totalWidth: Float = (selectTimeMeasurer.size.width + selectValueMeasurer.size.width).toFloat()

        val textRectPadding = selectedBar.xStart - ((totalWidth.div(24)) * selectedBar.idx)

        drawRoundRect(
            color = Color.LightGray,
            topLeft = Offset(
                textRectPadding,
                0f
            ),
            size = Size(
                totalWidth + 12.dp.toPx(),
                selectValueMeasurer.size.height.toFloat() + 21f
            ),
            cornerRadius = CornerRadius(50f)
        )

        drawText(
            textMeasurer = textMeasurer,
            text = selectTimZoneValue,
            style = TextStyle(
                fontSize = 12.sp,
                color = Color.Black
            ),
            topLeft = Offset(
                textRectPadding + 20f,
                12f
            )
        )

        drawText(
            textMeasurer = textMeasurer,
            text = selectValue,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            topLeft = Offset(
                textRectPadding + 20f + selectTimeMeasurer.size.width.toFloat(),
                6f
            )
        )
    }
}

fun Modifier.tapOrPress(
    onStart: (offsetX: Float) -> Unit,
    onCancel: (offsetX: Float) -> Unit,
    onCompleted: (offsetX: Float) -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.pointerInput(interactionSource) {
        awaitEachGesture {
            val tap = awaitFirstDown()
                .also { if (it.pressed != it.previousPressed) it.consume() }
            onStart(tap.position.x)
            val up = waitForUpOrCancellation()
            if (up == null) {
                onCancel(tap.position.x)
            } else {
                if (up.pressed != up.previousPressed) up.consume()
                onCompleted(tap.position.x)
            }
        }
    }
}