package com.aidomari.app.core.debug

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Developer and debug preferences for Hokkaido Sugoroku travel simulations.
 * Available in debug builds to accelerate travel speed for verification.
 */
class SugorokuDebugPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _speedMultiplier = MutableStateFlow(loadSpeedMultiplier())
    val speedMultiplier: StateFlow<Float> = _speedMultiplier.asStateFlow()

    private fun loadSpeedMultiplier(): Float = prefs.getFloat(KEY_SPEED_MULTIPLIER, 1.0f)

    fun setSpeedMultiplier(multiplier: Float) {
        val safeMultiplier = multiplier.coerceAtLeast(0.1f)
        prefs.edit().putFloat(KEY_SPEED_MULTIPLIER, safeMultiplier).apply()
        _speedMultiplier.value = safeMultiplier
    }

    companion object {
        private const val PREFS_NAME = "aidomari_sugoroku_debug_prefs"
        private const val KEY_SPEED_MULTIPLIER = "sugoroku_speed_multiplier"

        val AVAILABLE_SPEED_MULTIPLIERS = listOf(1.0f, 2.0f, 5.0f, 10.0f, 50.0f, 100.0f)
    }
}
