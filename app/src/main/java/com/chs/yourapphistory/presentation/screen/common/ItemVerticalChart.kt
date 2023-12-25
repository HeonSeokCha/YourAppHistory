package com.chs.yourapphistory.presentation.screen.common

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chs.yourapphistory.common.calculateScale
import com.chs.yourapphistory.common.convert24HourString
import com.chs.yourapphistory.common.convertBetweenHourString
import com.chs.yourapphistory.common.isZero
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ItemVerticalChart(
    hourUsageList: List<Pair<Int, Long>>,
    selectHour: (Pair<Int, Long>?) -> Unit
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
    val style = TextStyle(
        fontSize = 12.sp,
        color = Color.Black
    )

    LaunchedEffect(barAreas) {
        selectedBar = null
        selectHour(null)
    }

    LaunchedEffect(selectedPos) {
        val findBar = barAreas.find { selectedPos in it.xStart .. it.xEnd }
        selectedBar = if (findBar?.value.isZero()) {
            null
        } else findBar
    }

    LaunchedEffect(selectedBar) {
        if (selectedBar != null) {
            selectHour(selectedBar!!.idx to selectedBar!!.value)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(
                start = 8.dp,
                end = 8.dp
            )
    ) {

        val scope = rememberCoroutineScope()
        val animatable = remember { Animatable(1f) }
        val tempAnimatable = remember { Animatable(0f) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .tapOrPress(
                    onStart = { position ->
//                        scope.launch {
//                            selectedBar?.let { selected ->
//                                if (position in selected.xStart..selected.xEnd) {
//                                    // click in selected area - do nothing
//                                } else {
//                                    tempPosition = position
//                                    scope.launch {
//                                        tempAnimatable.snapTo(0f)
//                                        tempAnimatable.animateTo(1f, animationSpec = tween(300))
//                                    }
//                                }
//
//                            }
//                        }
                    },
                    onCancel = { position ->
//                        tempPosition = -Int.MAX_VALUE.toFloat()
//                        scope.launch {
//                            tempAnimatable.animateTo(0f)
//                        }
                    },
                    onCompleted = {
//                        val currentSelected = selectedBar
                        scope.launch {
                            selectedPos = it
//                            animatable.snapTo(tempAnimatable.value)
//                            async {
//                                animatable.animateTo(
//                                    1f,
//                                    animationSpec = tween(
//                                        300
//                                            .times(1f - tempAnimatable.value)
//                                            .roundToInt()
//                                    )
//                                )
//                            }
//
//                            async {
//                                tempAnimatable.snapTo(0f)
//                                currentSelected?.let {
//                                    tempPosition = currentSelected.xStart.plus(1f)
//                                    tempAnimatable.snapTo(1f)
//                                    tempAnimatable.animateTo(0f, tween(300))
//                                }
//                            }
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
                val barHeight = (size.height - selectedBar!!.value.times(scale).toFloat() - smallPadding - labelSectionHeight)
                val textResult = textMeasurer.measure(selectedBar!!.idx.convertBetweenHourString())
                val textRectPadding = selectedBar!!.xStart - (textResult.size.width.div(24) * selectedBar!!.idx)

                drawLine(
                    color = Color.Black,
                    start = Offset(selectedBar!!.xStart + distance.div(2), 50f),
                    end = Offset(selectedBar!!.xStart + distance.div(2), barHeight),
                    strokeWidth = 4f
                )

                drawRoundRect(
                    color = Color.LightGray,
                    topLeft = Offset(
                        textRectPadding - 0f,
                        0f
                    ),
                    size = Size(
                        textResult.size.width.toFloat(),
                        textResult.size.height.toFloat()
                    ),
                    cornerRadius = CornerRadius(50f)
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = selectedBar!!.idx.convertBetweenHourString(),
                    style = style,
                    topLeft = Offset(
                        textRectPadding + 20f,
                        3f
                    )
                )
            }
        }
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