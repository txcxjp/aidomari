package com.aidomari.app.core.ai

interface CardGenerationNotificationController {
    fun onGenerationStarted(articleTitle: String)
    fun onGenerationProgress(articleTitle: String, message: String, fraction: Float)
    fun onGenerationCompleted(articleTitle: String, cardCount: Int)
    fun onGenerationFailed(articleTitle: String, errorMessage: String)
    fun clearCompletionNotification()
}
