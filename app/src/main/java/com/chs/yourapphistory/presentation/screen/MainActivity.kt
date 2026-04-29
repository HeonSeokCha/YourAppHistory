package com.chs.yourapphistory.presentation.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import com.chs.yourapphistory.common.getUsagePermission

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
                    MainAppbar(
                        screen = backstack.last(),
                        onBack = { backstack.removeLastOrNull() },
                    )
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