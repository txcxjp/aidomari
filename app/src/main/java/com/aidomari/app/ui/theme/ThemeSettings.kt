package com.aidomari.app.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val label: String) {
    SYSTEM("システム"),
    LIGHT("ライト"),
    DARK("ダーク"),
}

enum class AppColorTheme(
    val displayName: String,
    val previewColor: Color,
) {
    INDIGO("藍 (Indigo)", Color(0xFF2A4B7C)),
    FOREST("常磐 (Forest)", Color(0xFF1E6B45)),
    SAKURA("桜 (Sakura)", Color(0xFF8C4A60)),
    AMBER("琥珀 (Amber)", Color(0xFF835400)),
    SLATE("墨 (Slate)", Color(0xFF3D4753)),
}

data class ThemeConfig(
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val colorTheme: AppColorTheme = AppColorTheme.INDIGO,
)

class ThemePreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeConfig = MutableStateFlow(loadConfig())
    val themeConfig: StateFlow<ThemeConfig> = _themeConfig.asStateFlow()

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

    fun setThemeConfig(config: ThemeConfig) {
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
