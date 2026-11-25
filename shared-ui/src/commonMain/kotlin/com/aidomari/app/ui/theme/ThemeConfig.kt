package com.aidomari.app.ui.theme

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.StateFlow

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

/**
 * Persistence contract for application theme configuration.
 */
interface ThemeConfigStore {
    val themeConfig: StateFlow<ThemeConfig>
    fun setThemeConfig(config: ThemeConfig)
}
