package com.example.yourapphistory.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate

@Composable
fun DateHeader(
    onClick: (LocalDate) -> Unit
) {
    var dateMoveCount by remember { mutableIntStateOf(0) }
    val localDate by remember { mutableStateOf(Util.getLocalDateList()) }

    Row {
        IconButton(
            onClick = {
                dateMoveCount += 1
                onClick(localDate[dateMoveCount])
            },
            enabled = dateMoveCount <= localDate.size - 2
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "Previous"
            )
        }

        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            text = localDate[dateMoveCount].toString(),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = {
                dateMoveCount -= 1
                onClick(localDate[dateMoveCount])
            },
            enabled = dateMoveCount > 0L
        ) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Next"
            )
        }
    }
}
