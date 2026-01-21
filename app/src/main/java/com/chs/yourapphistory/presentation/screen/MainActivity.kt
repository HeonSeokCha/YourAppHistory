package com.chs.yourapphistory.presentation.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation3.runtime.rememberNavBackStack
import com.chs.yourapphistory.R
import com.chs.yourapphistory.common.getUsagePermission
import com.chs.yourapphistory.domain.model.SortType

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val backstack = rememberNavBackStack().apply {
                this.clear()
                if (getUsagePermission(this@MainActivity)) this.add(MainScreens.ScreenTotalSummary)
//                if (getUsagePermission(this@MainActivity)) this.add(MainScreens.ScreenUsedAppList(SortType.UsageEvent))
                else this.add(MainScreens.ScreenWelcome)
            }

            Scaffold(
                topBar = {
                    when (backstack.last()) {
                        MainScreens.ScreenWelcome -> Unit

                        is MainScreens.ScreenUsedAppList -> {
                            TopAppBar(
                                title = {
                                    Text(text = getString(R.string.app_name))
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.primary,
                                )
                            )
                        }

                        is MainScreens.ScreenAppUsageDetail -> {
                            val a = backstack.last() as MainScreens.ScreenAppUsageDetail
                            TopAppBar(
                                title = {
                                    Text(
                                        text = a.targetLabelName,
                                        maxLines = 1,
                                        overflow = TextOverflow.Companion.Ellipsis
                                    )
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { backstack.removeLastOrNull() }
                                    ) {
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
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
            ) {
                NavigationRoot(
                    modifier = Modifier.padding(it),
                    backStack = backstack
                )
            }
        }
    }
}