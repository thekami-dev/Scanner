package com.scann.thekami

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.scann.thekami.model.ScanAction
import com.scann.thekami.model.ScanResult
import com.scann.thekami.ui.components.FloatingBottomNav
import com.scann.thekami.ui.components.NavTab
import com.scann.thekami.ui.screens.AboutScreen
import com.scann.thekami.ui.screens.GeneratorScreen
import com.scann.thekami.ui.screens.ResultScreen
import com.scann.thekami.ui.screens.ScannerScreen
import com.scann.thekami.ui.screens.SettingsScreen
import com.scann.thekami.ui.theme.ScannTheme
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

private sealed class Overlay {
    object None : Overlay()
    object About : Overlay()
    data class Result(val scanResult: ScanResult) : Overlay()
}

@Composable
fun ScannApp() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(NavTab.SCAN) }
    var overlay by remember { mutableStateOf<Overlay>(Overlay.None) }

    ScannTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BackHandler(enabled = overlay !is Overlay.None) {
                overlay = if (overlay is Overlay.About) Overlay.None else Overlay.None
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Base tab content
                when (selectedTab) {
                    NavTab.SCAN -> {
                        ScannerScreen(
                            onBarcodeScanned = { result ->
                                overlay = Overlay.Result(result)
                            }
                        )
                    }
                    NavTab.CREATE -> {
                        GeneratorScreen()
                    }
                    NavTab.SETTINGS -> {
                        SettingsScreen(
                            onAboutClick = { overlay = Overlay.About }
                        )
                    }
                }

                // Floating bottom nav — hidden while an overlay (Result/About) is showing
                if (overlay is Overlay.None) {
                    FloatingBottomNav(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                            .navigationBarsPadding()
                    )
                }

                // Overlays
                when (val current = overlay) {
                    is Overlay.Result -> {
                        ResultScreen(
                            scanResult = current.scanResult,
                            onBack = { overlay = Overlay.None },
                            onAction = { action: ScanAction ->
                                ScanActionHandler.handle(context, action)
                            }
                        )
                    }
                    is Overlay.About -> {
                        AboutScreen(
                            onBack = { overlay = Overlay.None }
                        )
                    }
                    is Overlay.None -> {}
                }
            }
        }
    }
}
