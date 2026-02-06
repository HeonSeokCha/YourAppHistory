package com.chs.yourapphistory.presentation.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation3.runtime.NavKey
import com.chs.yourapphistory.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppbar(
    screen: NavKey,
    onBack: () -> Unit,
    onSearch: (String) -> Unit
) {
    when (screen) {
        MainScreens.ScreenWelcome -> Unit

        is MainScreens.ScreenTotalSummary -> {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }

        is MainScreens.ScreenAppUsageDetail -> {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }

        else -> {
            AppSearchBar(
                onSearch = onSearch,
                onBack = onBack
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppSearchBar(
    onSearch: (String) -> Unit,
    onBack: () -> Unit
) {
    var searchMode by remember { mutableStateOf(false) }
    val textState = rememberTextFieldState()

    TopAppBar(
        title = {
            if (searchMode) {
                BasicTextField(
                    state = textState,
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 1),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Text
                    ),
                    onKeyboardAction = {
                        searchMode = false
                        onSearch(textState.text.toString())
                    }
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            }
        },
        actions = {
            IconButton(onClick = {
                if (searchMode) {
                    onSearch(textState.text.toString())
                    searchMode = false
                } else {
                    searchMode = true
                }
            }) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}