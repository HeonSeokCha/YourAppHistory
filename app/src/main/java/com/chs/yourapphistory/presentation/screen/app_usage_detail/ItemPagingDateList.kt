package com.chs.yourapphistory.presentation.screen.app_usage_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chs.yourapphistory.common.getYearOfWeek
import com.chs.yourapphistory.common.reverseDateUntil
import java.time.LocalDate
import kotlin.collections.reversed

@Composable
fun ItemDateList(
    state: PagerState,
    minDate: LocalDate,
    targetDate: LocalDate,
    item: List<List<LocalDate>>,
    onClick: (Pair<Int, Int>) -> Unit
) {
    HorizontalPager(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        state = state,
        reverseLayout = true,
        key = { item[it].first() }
    ) {
        val item = item[it]
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            item.reversed().forEachIndexed { idx, date ->
                Text(
                    text = if (date.dayOfMonth == 1) {
                        "${date.monthValue} / ${date.dayOfMonth}"
                    } else {
                        date.dayOfMonth.toString()
                    },
                    modifier = Modifier
                        .clickable {
                            if (date < minDate) return@clickable
                            if (date > LocalDate.now()) return@clickable
                            onClick(state.currentPage to item.size - 1 - idx)
                        }
                        .drawBehind {
                            drawRoundRect(
                                color = if (targetDate == date) Color.LightGray else Color.Transparent,
                                cornerRadius = CornerRadius(10.dp.toPx())
                            )
                        }
                        .padding(
                            horizontal =  if (date.dayOfMonth == 1) 8.dp else 12.dp,
                            vertical =  8.dp
                        ),
                    color = if (date >= minDate && date <= LocalDate.now()) Color.Black else Color.LightGray
                )
            }
        }
    }
}

@Composable
fun ItemWeekList(
    state: PagerState,
    targetWeek: List<LocalDate>,
    item: List<List<List<LocalDate>>>,
    onClick: (Pair<Int, Int>) -> Unit
) {
    HorizontalPager(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        state = state,
        reverseLayout = true,
        key = { item[it].first().first() }
    ) {
        val item = item[it]
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            item.reversed().forEachIndexed { idx, dateList ->
                Text(
                    modifier = Modifier
                        .clickable { onClick(state.currentPage to item.size - 1 - idx) }
                        .drawBehind {
                            drawRoundRect(
                                color = if (targetWeek == dateList) Color.LightGray else Color.Transparent,
                                cornerRadius = CornerRadius(10.dp.toPx())
                            )
                        }
                        .padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        ),
                    text = "${dateList.max().getYearOfWeek()}주"
                )
            }
        }
    }
}

@Composable
@Preview
private fun PreviewItemDate() {
    val state = rememberPagerState(initialPage = 0, pageCount = { 1 })
    val date = remember { LocalDate.now().minusDays(16) }
    ItemDateList(
        state = state,
        minDate = date.minusDays(7L),
        targetDate = date.minusDays(3L),
        item = listOf(
            date.minusDays(7L).reverseDateUntil(date)
        ),
        onClick = {}
    )
}