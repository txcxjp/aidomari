package com.aidomari.app.core.lockscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = LockScreenPreferences(context)
            if (prefs.config.value.isEnabled) {
                LockScreenStudyService.start(context)
            }
        }
    }
}
