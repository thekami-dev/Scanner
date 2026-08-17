package com.scann.thekami

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.scann.thekami.model.ScanAction
import com.scann.thekami.model.ScanResult
import com.scann.thekami.ui.screens.HomeScreen
import com.scann.thekami.ui.screens.ResultScreen
import com.scann.thekami.ui.screens.ScannerScreen
import com.scann.thekami.ui.screens.SettingsScreen
import com.scann.thekami.util.ScanActionHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScannApp()
        }
    }
}

private sealed class Screen {
    object Home : Screen()
    object Scanner : Screen()
    object Settings : Screen()
    data class Result(val scanResult: ScanResult) : Screen()
}

@Composable
fun ScannApp() {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        onScanClick = { currentScreen = Screen.Scanner },
                        onSettingsClick = { currentScreen = Screen.Settings }
                    )
                }
                is Screen.Scanner -> {
                    ScannerScreen(
                        onBarcodeScanned = { result ->
                            currentScreen = Screen.Result(result)
                        },
                        onBack = { currentScreen = Screen.Home }
                    )
                }
                is Screen.Settings -> {
                    SettingsScreen(
                        onBack = { currentScreen = Screen.Home }
                    )
                }
                is Screen.Result -> {
                    ResultScreen(
                        scanResult = screen.scanResult,
                        onBack = { currentScreen = Screen.Home },
                        onAction = { action: ScanAction ->
                            ScanActionHandler.handle(context, action)
                        }
                    )
                }
            }
        }
    }
}
