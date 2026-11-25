package com.aidomari.app.core.ai

data class GeminiConfig(
    val apiKey: String = "",
    val modelName: String = DEFAULT_MODEL,
    val temperature: Float = DEFAULT_TEMPERATURE,
) {
    val isConfigured: Boolean get() = apiKey.isNotBlank()

    companion object {
        const val DEFAULT_MODEL = "gemini-3.8-flash"
        const val DEFAULT_TEMPERATURE = 0.2f
        val DEPRECATED_MODELS = setOf(
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-1.5-pro",
            "gemini-1.5-flash-8b",
        )
        val AVAILABLE_MODELS = listOf(
            "gemini-3.8-flash",
            "gemini-2.5-flash",
            "gemini-3.5-flash-lite",
            "gemini-3.1-pro",
            "gemini-2.5-pro",
        )
    }
}
