package com.scann.thekami.util

import android.content.Context

object AppPreferences {
    private const val PREFS_NAME = "scann_prefs"
    private const val KEY_FLOATING_NAV = "floating_nav"

    fun isFloatingNav(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_FLOATING_NAV, false)

    fun setFloatingNav(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_FLOATING_NAV, enabled)
            .apply()
    }
}
