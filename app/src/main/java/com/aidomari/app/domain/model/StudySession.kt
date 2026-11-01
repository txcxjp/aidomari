package com.aidomari.app.domain.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class StudySession(
    val deckId: String,
    val partIndex: Int = 0,
    val currentCardIndex: Int = 0,
    val queueCardIds: List<String> = emptyList(),
    val retryCardIds: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val lastStudiedAt: Long = System.currentTimeMillis(),
) {
    fun queueCardIdsJson(): String = Json.encodeToString(queueCardIds)
    fun retryCardIdsJson(): String = Json.encodeToString(retryCardIds)

    companion object {
        fun parseCardIds(json: String): List<String> =
            try {
                Json.decodeFromString(json)
            } catch (_: Exception) {
                emptyList()
            }
    }
}
