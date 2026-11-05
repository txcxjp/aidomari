package com.aidomari.app.core.lockscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            val prefs = LockScreenPreferences(context)
            if (prefs.config.value.isEnabled) {
                LockScreenStudyService.start(context)
            }
        }
    }
}
