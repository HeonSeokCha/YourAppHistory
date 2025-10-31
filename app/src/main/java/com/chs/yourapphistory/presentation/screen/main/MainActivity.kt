package com.chs.yourapphistory.presentation.screen.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chs.yourapphistory.R
import com.chs.yourapphistory.common.getUsagePermission
import com.chs.yourapphistory.presentation.screen.NavigationRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val selectPackageLabel: String? by viewModel.selectPackageLabel.collectAsStateWithLifecycle()

            Scaffold(
                topBar = {
                    if (selectPackageLabel == null) {
                        TopAppBar(
                            title = {
                                Text(text = getString(R.string.app_name))
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    } else {
                        TopAppBar(
                            title = {
                                Text(
                                    text = selectPackageLabel!!,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        viewModel.changeSelectPackageName(null)
                                    }
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
                },
                modifier = Modifier
                    .fillMaxSize()
            ) {
                NavigationRoot(
                    modifier = Modifier
                        .padding(it),
                    isGrantPermission = getUsagePermission(this),
                    selectPackage = { viewModel.changeSelectPackageName(it) }
                )
            }
        }
    }
}
