package com.scann.thekami.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Warm, soft "creamy" palette — inspired by paper/ink aesthetics
private val LightColors = lightColorScheme(
    primary = Color(0xFFB5654A),           // warm terracotta
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF3DBCF),
    onPrimaryContainer = Color(0xFF5C2A16),
    secondary = Color(0xFF8A7A6B),
    background = Color(0xFFFAF6F0),        // soft cream
    onBackground = Color(0xFF2B2622),
    surface = Color(0xFFFFFDF9),
    onSurface = Color(0xFF2B2622),
    surfaceVariant = Color(0xFFEDE4D9),
    onSurfaceVariant = Color(0xFF5C5348),
    outline = Color(0xFFD8CCBC)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE0A088),           // soft warm coral
    onPrimary = Color(0xFF3A1B0F),
    primaryContainer = Color(0xFF5C2A16),
    onPrimaryContainer = Color(0xFFF3DBCF),
    secondary = Color(0xFFB6A793),
    background = Color(0xFF1C1917),        // warm near-black
    onBackground = Color(0xFFEDE6DC),
    surface = Color(0xFF25211D),
    onSurface = Color(0xFFEDE6DC),
    surfaceVariant = Color(0xFF352F29),
    onSurfaceVariant = Color(0xFFCBBEAE),
    outline = Color(0xFF4A423A)
)

@Composable
fun ScannTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ScannTypography,
        content = content
    )
}
