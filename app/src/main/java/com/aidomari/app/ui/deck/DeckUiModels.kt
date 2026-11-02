package com.aidomari.app.ui.deck

import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.DeckPart
import com.aidomari.app.domain.model.StudySession

data class DeckPartWithSession(
    val part: DeckPart,
    val session: StudySession?,
) {
    val isCompleted: Boolean
        get() = session?.isCompleted == true

    val isInProgress: Boolean
        get() = session != null && !session.isCompleted
}

data class DeckWithPartsUiModel(
    val deck: Deck,
    val parts: List<DeckPartWithSession>,
    val totalCardCount: Int,
) {
    val completedPartsCount: Int
        get() = parts.count { it.isCompleted }

    val totalPartsCount: Int
        get() = parts.size

    val isAllCompleted: Boolean
        get() = parts.isNotEmpty() && completedPartsCount == totalPartsCount

    val learnedCardCount: Int
        get() = parts.filter { it.isCompleted }.sumOf { it.part.count }

    val masteryRate: Float
        get() = if (totalCardCount > 0) {
            (learnedCardCount.toFloat() / totalCardCount).coerceIn(0f, 1f)
        } else {
            0f
        }
}

data class BookmarkDeckUiModel(
    val deck: Deck,
    val parts: List<DeckPartWithSession>,
    val totalCount: Int,
) {
    val completedPartsCount: Int
        get() = parts.count { it.isCompleted }

    val totalPartsCount: Int
        get() = parts.size

    val isAllCompleted: Boolean
        get() = parts.isNotEmpty() && completedPartsCount == totalPartsCount

    val learnedCardCount: Int
        get() = parts.filter { it.isCompleted }.sumOf { it.part.count }

    val masteryRate: Float
        get() = if (totalCount > 0) {
            (learnedCardCount.toFloat() / totalCount).coerceIn(0f, 1f)
        } else {
            0f
        }
}
