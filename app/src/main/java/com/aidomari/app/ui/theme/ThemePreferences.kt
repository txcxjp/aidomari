package com.aidomari.app.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) : ThemeConfigStore {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeConfig = MutableStateFlow(loadConfig())
    override val themeConfig: StateFlow<ThemeConfig> = _themeConfig.asStateFlow()

    private fun loadConfig(): ThemeConfig {
        val modeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val colorName = prefs.getString(KEY_COLOR_THEME, AppColorTheme.INDIGO.name) ?: AppColorTheme.INDIGO.name

        val mode = try {
            ThemeMode.valueOf(modeName)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }

        val color = try {
            AppColorTheme.valueOf(colorName)
        } catch (_: Exception) {
            AppColorTheme.INDIGO
        }

        return ThemeConfig(mode = mode, colorTheme = color)
    }

    override fun setThemeConfig(config: ThemeConfig) {
        prefs.edit()
            .putString(KEY_THEME_MODE, config.mode.name)
            .putString(KEY_COLOR_THEME, config.colorTheme.name)
            .apply()
        _themeConfig.value = config
    }

    fun setThemeMode(mode: ThemeMode) {
        setThemeConfig(_themeConfig.value.copy(mode = mode))
    }

    fun setColorTheme(colorTheme: AppColorTheme) {
        setThemeConfig(_themeConfig.value.copy(colorTheme = colorTheme))
    }

    companion object {
        private const val PREFS_NAME = "aidomari_theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_COLOR_THEME = "color_theme"
    }
}
