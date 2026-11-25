package com.aidomari.app.desktop

import com.aidomari.app.core.ai.GeminiConfig
import com.aidomari.app.core.ai.GeminiConfigStore
import com.aidomari.app.ui.theme.AppColorTheme
import com.aidomari.app.ui.theme.ThemeConfig
import com.aidomari.app.ui.theme.ThemeConfigStore
import com.aidomari.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.prefs.Preferences

/**
 * Java Preferences backed [ThemeConfigStore] for desktop targets.
 */
class DesktopThemeConfigStore(
    private val prefs: Preferences = Preferences.userRoot().node("com/aidomari/desktop/theme"),
) : ThemeConfigStore {

    private val _themeConfig = MutableStateFlow(loadConfig())
    override val themeConfig: StateFlow<ThemeConfig> = _themeConfig.asStateFlow()

    private fun loadConfig(): ThemeConfig {
        val modeName = prefs.get(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        val colorName = prefs.get(KEY_COLOR_THEME, AppColorTheme.INDIGO.name)
        val mode = runCatching { ThemeMode.valueOf(modeName) }.getOrDefault(ThemeMode.SYSTEM)
        val color = runCatching { AppColorTheme.valueOf(colorName) }.getOrDefault(AppColorTheme.INDIGO)
        return ThemeConfig(mode = mode, colorTheme = color)
    }

    override fun setThemeConfig(config: ThemeConfig) {
        prefs.put(KEY_THEME_MODE, config.mode.name)
        prefs.put(KEY_COLOR_THEME, config.colorTheme.name)
        runCatching { prefs.flush() }
        _themeConfig.value = config
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_COLOR_THEME = "color_theme"
    }
}

/**
 * Java Preferences backed [GeminiConfigStore] for desktop targets.
 */
class DesktopGeminiConfigStore(
    private val prefs: Preferences = Preferences.userRoot().node("com/aidomari/desktop/gemini"),
) : GeminiConfigStore {

    private val _config = MutableStateFlow(loadConfig())
    override val config: StateFlow<GeminiConfig> = _config.asStateFlow()

    private fun loadConfig(): GeminiConfig {
        val key = prefs.get(KEY_API_KEY, "")
        val savedModel = prefs.get(KEY_MODEL_NAME, GeminiConfig.DEFAULT_MODEL)
        val model = if (savedModel in GeminiConfig.DEPRECATED_MODELS) GeminiConfig.DEFAULT_MODEL else savedModel
        val temp = prefs.getFloat(KEY_TEMPERATURE, GeminiConfig.DEFAULT_TEMPERATURE)
        return GeminiConfig(apiKey = key, modelName = model, temperature = temp)
    }

    override fun updateConfig(config: GeminiConfig) {
        val trimmedKey = config.apiKey.trim()
        val effectiveModel = config.modelName.trim().ifBlank { GeminiConfig.DEFAULT_MODEL }
        prefs.put(KEY_API_KEY, trimmedKey)
        prefs.put(KEY_MODEL_NAME, effectiveModel)
        prefs.putFloat(KEY_TEMPERATURE, config.temperature)
        runCatching { prefs.flush() }
        _config.value = config.copy(apiKey = trimmedKey, modelName = effectiveModel)
    }

    companion object {
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_MODEL_NAME = "gemini_model_name"
        private const val KEY_TEMPERATURE = "gemini_temperature"
    }
}
