package com.aidomari.app.core.ai

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GeminiPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<GeminiConfig> = _config.asStateFlow()

    private fun loadConfig(): GeminiConfig {
        val key = prefs.getString(KEY_API_KEY, "") ?: ""
        val model = prefs.getString(KEY_MODEL_NAME, GeminiConfig.DEFAULT_MODEL) ?: GeminiConfig.DEFAULT_MODEL
        val temp = prefs.getFloat(KEY_TEMPERATURE, GeminiConfig.DEFAULT_TEMPERATURE)
        return GeminiConfig(apiKey = key, modelName = model, temperature = temp)
    }

    fun updateConfig(config: GeminiConfig) {
        val trimmedKey = config.apiKey.trim()
        prefs.edit()
            .putString(KEY_API_KEY, trimmedKey)
            .putString(KEY_MODEL_NAME, config.modelName)
            .putFloat(KEY_TEMPERATURE, config.temperature)
            .apply()
        _config.value = config.copy(apiKey = trimmedKey)
    }

    fun setApiKey(apiKey: String) {
        updateConfig(_config.value.copy(apiKey = apiKey.trim()))
    }

    fun clearApiKey() {
        setApiKey("")
    }

    companion object {
        private const val PREFS_NAME = "aidomari_gemini_prefs"
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_MODEL_NAME = "gemini_model_name"
        private const val KEY_TEMPERATURE = "gemini_temperature"
    }
}
