package com.example.yourapphistory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun UsageChart(
    usageInfoList: List<AppUsageInfo> = emptyList(),
    modifier: Modifier = Modifier,
    graphColor: Color = Color.Cyan
) {

    val spacing = 100f

    val transparentGraphColor = remember {
        graphColor.copy(alpha = 0.4f)
    }

    usageInfoList.forEach {
        Log.e("Usage", it.toString())
    }

    val upperValue = remember {
        (usageInfoList.maxOfOrNull { ((it.endTime - it.beginTime) / 1000) / 60 }?.plus(1)) ?: 0
    }

    val lowerValue = remember {
       usageInfoList.minOfOrNull { ((it.endTime - it.beginTime) / 1000) / 60 } ?: 0
    }

    Log.e("upper", upperValue.toString())
    Log.e("low", lowerValue.toString())

    val dayUsageMap: HashMap<Int, Int> = hashMapOf<Int, Int>().apply {
        (0..23).forEach {
            this[it] = 0
        }
    }

    val density = LocalDensity.current

    val textPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.BLACK
            textAlign = Paint.Align.CENTER
            textSize = density.run { 12.sp.toPx() }
        }
    }

    Canvas(
        modifier = modifier,
    ) {
        val spacePerHour = 35

        for (i in 0 .. 24 step 6) {
            drawContext.canvas.nativeCanvas.apply {
                val calendar = Calendar.getInstance().apply {
                    this.set(Calendar.HOUR_OF_DAY, i)
                }.timeInMillis

                drawText(
                    SimpleDateFormat("a h", Locale.KOREA).format(calendar),
                    spacing + i * spacePerHour,
                    size.height - 5,
                    textPaint
                )
            }
        }

        val usageStep = (upperValue - lowerValue) / 3f
        (1..3).forEach { i ->
            Log.e("123_1", "$lowerValue + $usageStep * $i")
            Log.e("123_2", (lowerValue + usageStep * i).roundToInt().toString())
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    (lowerValue + usageStep * i).roundToInt().toString(),
                    size.width - 60,
                    size.height - spacing - i * size.height / 6f,
                    textPaint
                )
            }
        }

        usageInfoList.forEach {
            val usageTime = (it.endTime - it.beginTime)
            val calendar = Calendar.getInstance().apply {
                this.timeInMillis = it.beginTime
            }
            dayUsageMap[calendar[Calendar.HOUR_OF_DAY]] = usageTime.toInt()
        }
    }
}