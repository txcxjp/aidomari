package com.aidomari.app.core.lockscreen

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class LockScreenTarget {
    ALL,
    BOOKMARK_ONLY,
}

data class LockScreenConfig(
    val isEnabled: Boolean = false,
    val target: LockScreenTarget = LockScreenTarget.ALL,
)

class LockScreenPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<LockScreenConfig> = _config.asStateFlow()

    private fun loadConfig(): LockScreenConfig {
        val isEnabled = prefs.getBoolean(KEY_ENABLED, false)
        val targetName = prefs.getString(KEY_TARGET, LockScreenTarget.ALL.name)
            ?: LockScreenTarget.ALL.name
        val target = try {
            LockScreenTarget.valueOf(targetName)
        } catch (_: Exception) {
            LockScreenTarget.ALL
        }
        return LockScreenConfig(isEnabled = isEnabled, target = target)
    }

    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        _config.value = _config.value.copy(isEnabled = enabled)
    }

    fun setTarget(target: LockScreenTarget) {
        prefs.edit().putString(KEY_TARGET, target.name).apply()
        _config.value = _config.value.copy(target = target)
    }

    companion object {
        private const val PREFS_NAME = "aidomari_lock_screen_prefs"
        private const val KEY_ENABLED = "lock_screen_enabled"
        private const val KEY_TARGET = "lock_screen_target"
    }
}
