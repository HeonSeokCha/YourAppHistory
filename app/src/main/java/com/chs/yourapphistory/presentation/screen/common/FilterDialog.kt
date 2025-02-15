package com.chs.yourapphistory.presentation.screen.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chs.yourapphistory.presentation.screen.used_app_list.UsedAppEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    list: List<UsedAppEvent.GetUsageEvent>,
    onClick: (UsedAppEvent.GetUsageEvent) -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .wrapContentSize()
            .padding(vertical = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .padding(16.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(list.size) { idx ->
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onClick(list[idx])
                            onDismiss()
                        },
                    text = list[idx].name,
                    fontSize = 16.sp
                )
            }
        }
    }
}