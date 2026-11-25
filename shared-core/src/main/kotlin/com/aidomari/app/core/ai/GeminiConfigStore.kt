package com.aidomari.app.core.ai

import kotlinx.coroutines.flow.StateFlow

/**
 * Persistence contract for Gemini API configuration across platforms.
 */
interface GeminiConfigStore {
    val config: StateFlow<GeminiConfig>
    fun updateConfig(config: GeminiConfig)
}
