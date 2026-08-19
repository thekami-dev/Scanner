package com.scann.thekami

import android.content.Intent
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

    private var pendingDeepLinkTab by mutableStateOf<NavTab?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingDeepLinkTab = resolveDeepLinkTab(intent)
        setContent {
            ScannApp(
                initialTab = pendingDeepLinkTab,
                onDeepLinkConsumed = { pendingDeepLinkTab = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLinkTab = resolveDeepLinkTab(intent)
    }

    private fun resolveDeepLinkTab(intent: Intent?): NavTab? {
        val uri = intent?.data ?: return null
        return when (uri.host) {
            "scan" -> NavTab.SCAN
            "create" -> NavTab.CREATE
            else -> null
        }
    }
}

private sealed class Overlay {
    object None : Overlay()
    object About : Overlay()
    data class Result(val scanResult: ScanResult) : Overlay()
}

@Composable
fun ScannApp(
    initialTab: NavTab? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(initialTab ?: NavTab.SCAN) }
    var overlay by remember { mutableStateOf<Overlay>(Overlay.None) }

    LaunchedEffect(initialTab) {
        if (initialTab != null) {
            selectedTab = initialTab
            overlay = Overlay.None
            onDeepLinkConsumed()
        }
    }

    ScannTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            BackHandler(enabled = overlay !is Overlay.None) {
                overlay = Overlay.None
            }

            Box(modifier = Modifier.fillMaxSize()) {
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
