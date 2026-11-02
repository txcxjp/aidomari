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
    val completedPartIndices: Set<Int> = emptySet(),
) {
    fun queueCardIdsJson(): String = Json.encodeToString(queueCardIds)
    fun retryCardIdsJson(): String = Json.encodeToString(retryCardIds)
    fun completedPartIndicesJson(): String = Json.encodeToString(completedPartIndices.toList())

    companion object {
        fun parseCardIds(json: String): List<String> =
            try {
                Json.decodeFromString(json)
            } catch (_: Exception) {
                emptyList()
            }

        fun parsePartIndices(json: String): Set<Int> =
            try {
                Json.decodeFromString<List<Int>>(json).toSet()
            } catch (_: Exception) {
                emptySet()
            }
    }
}
