package com.aidomari.app.ui.study

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck

data class StudyUiState(
    val isLoading: Boolean = true,
    val deck: Deck? = null,
    val partIndex: Int = 0,
    val currentCard: Card? = null,
    val nextCard: Card? = null,
    val isFlipped: Boolean = false,
    val queueSize: Int = 0,
    val totalInPart: Int = 0,
    val completedCount: Int = 0,
    val retryCount: Int = 0,
    val isPartCompleted: Boolean = false,
    val hasNextPart: Boolean = false,
    val subjectCards: List<Card> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Card> = emptyList(),
)
