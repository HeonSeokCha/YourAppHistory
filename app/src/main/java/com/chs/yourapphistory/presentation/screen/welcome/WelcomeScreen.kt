package com.chs.yourapphistory.presentation.screen.welcome

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Button
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.chs.yourapphistory.common.getUsagePermission
import com.chs.yourapphistory.presentation.Screen

@Composable
fun WelcomeScreen(
    navController: NavHostController
) {
    val context: Context = LocalContext.current

    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (getUsagePermission(context)) {
            navController.navigate(Screen.ScreenUsedAppList.route)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
//        Image(
//            modifier = Modifier
//                .fillMaxWidth(0.5f)
//                .fillMaxHeight(0.7f),
//            painter = painterResource(id = onBoardingPage.image),
//            contentDescription = "Pager Image"
//        )
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = "I Need Permissions",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .padding(top = 20.dp),
            text = "I Need Permissions",
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 40.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                          requestPermission.launch(
                              Intent(
                                  Settings.ACTION_USAGE_ACCESS_SETTINGS,
                                  Uri.parse("package:${context.packageName}")
                              )
                          )

                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White
                )
            ) {
                Text(text = "Finish")
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun FirstOnBoardingScreenPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        WelcomeScreen(navController = rememberNavController())
    }
}