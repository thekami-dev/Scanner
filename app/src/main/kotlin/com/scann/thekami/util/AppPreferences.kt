package com.scann.thekami.util

import android.content.Context
import android.os.Build

data class AppSettings(
    val floatingNav: Boolean = false,
    val dynamicColor: Boolean = true,
    val animations: Boolean = true
)

object AppPreferences {
    private const val PREFS_NAME = "scann_prefs"
    private const val KEY_FLOATING_NAV = "floating_nav"
    private const val KEY_DYNAMIC_COLOR = "dynamic_color"
    private const val KEY_ANIMATIONS = "animations"

    val dynamicColorSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun load(context: Context): AppSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaults = AppSettings()
        return AppSettings(
            floatingNav = prefs.getBoolean(KEY_FLOATING_NAV, defaults.floatingNav),
            dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, defaults.dynamicColor),
            animations = prefs.getBoolean(KEY_ANIMATIONS, defaults.animations)
        )
    }

    fun save(context: Context, settings: AppSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_FLOATING_NAV, settings.floatingNav)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .putBoolean(KEY_ANIMATIONS, settings.animations)
            .apply()
    }
}
