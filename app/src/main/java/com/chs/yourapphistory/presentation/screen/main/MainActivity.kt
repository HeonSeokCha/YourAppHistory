package com.chs.yourapphistory.presentation.screen.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.chs.yourapphistory.common.Constants
import com.chs.yourapphistory.domain.usecase.InsertInstallAppInfoUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController: NavHostController = rememberNavController()
            MainNavHost(
                navController = navController,
                isGrantPermission = Constants.getUsagePermission(this)
            )
        }
    }
}
