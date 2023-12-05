package com.chs.yourapphistory.presentation.screen.used_app_list

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.chs.yourapphistory.common.toMillis
import com.chs.yourapphistory.presentation.Screen
import com.chs.yourapphistory.presentation.screen.common.DateHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
fun UsedAppListScreenScreen(
    navController: NavHostController,
    viewModel: UsedAppListViewModel = hiltViewModel()
) {
    val context: Context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberLazyListState()

    LaunchedEffect(context, viewModel) {
        withContext(Dispatchers.IO) {
            viewModel.insertInfo()
        }
        viewModel.getDayUseAppInfoList(state.targetDate)
    }

    LaunchedEffect(state.targetDate) {
        scrollState.scrollToItem(0, 0)
        viewModel.getDayUseAppInfoList(state.targetDate)
    }

    Column {
        if (state.localDateList.isNotEmpty()) {
            DateHeader(state.localDateList) {
                viewModel.changeDate(it)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                horizontal = 8.dp,
                vertical = 8.dp
            )
        ) {
            if (state.isLoading) {
                item {
                    CircularProgressIndicator()
                }
            }

            items(state.appInfoList) { appInfo ->
                ItemAppInfoSmall(
                    usedAppInfo = appInfo,
                ) { packageName ->
                    navController.navigate(
                        "${Screen.ScreenAppUsageDetail.route}/${packageName}/${state.targetDate.toMillis()}"
                    )
                }
            }
        }
    }
}

