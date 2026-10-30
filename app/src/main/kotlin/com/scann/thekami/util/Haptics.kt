package com.scann.thekami.util

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

object Haptics {
    /** Light tick for selections and toggles. */
    fun tick(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    /** Firmer feedback for a completed action, e.g. a successful scan. */
    fun confirm(view: View) {
        view.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.KEYBOARD_TAP
            }
        )
    }
}
