package com.aidomari.app.core.ai

data class GeminiConfig(
    val apiKey: String = "",
    val modelName: String = DEFAULT_MODEL,
    val temperature: Float = DEFAULT_TEMPERATURE,
) {
    val isConfigured: Boolean get() = apiKey.isNotBlank()

    companion object {
        const val DEFAULT_MODEL = "gemini-1.5-flash"
        const val DEFAULT_TEMPERATURE = 0.2f
        val AVAILABLE_MODELS = listOf(
            "gemini-1.5-flash",
            "gemini-2.0-flash",
            "gemini-1.5-pro",
        )
    }
}
