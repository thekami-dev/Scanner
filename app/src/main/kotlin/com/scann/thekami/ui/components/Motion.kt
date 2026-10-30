package com.scann.thekami.ui.components

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/** App-wide switch for non-essential animations (user setting + system "remove animations"). */
val LocalAnimationsEnabled = staticCompositionLocalOf { true }

@Composable
fun rememberSystemAnimationsDisabled(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
}
