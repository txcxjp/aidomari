package com.aidomari.app.ui.preview

import com.aidomari.app.core.database.SampleData
import com.aidomari.app.domain.model.DeckPart
import com.aidomari.app.ui.card.CardListUiState
import com.aidomari.app.ui.deck.BookmarkDeckUiModel
import com.aidomari.app.ui.deck.DeckPartWithSession
import com.aidomari.app.ui.deck.DeckWithPartsUiModel
import com.aidomari.app.ui.study.StudyUiState

object PreviewSampleData {
    val sampleSubjects = SampleData.sampleSubjects
    val sampleDeck = SampleData.sampleDeck
    val sampleCards = SampleData.sampleCards
    val sampleCard = sampleCards.first()

    val sampleDeckPart = DeckPart(
        deckId = sampleDeck.id,
        partIndex = 0,
        startIndex = 0,
        endIndex = 5,
        count = 5,
    )

    val sampleDeckPartWithSession = DeckPartWithSession(
        part = sampleDeckPart,
        session = null,
    )

    val sampleDeckWithParts = DeckWithPartsUiModel(
        deck = sampleDeck,
        parts = listOf(sampleDeckPartWithSession),
        totalCardCount = 5,
    )

    val sampleBookmarkDeck = BookmarkDeckUiModel(
        deck = sampleDeck.copy(name = "しおり付きカード"),
        parts = listOf(
            DeckPartWithSession(
                part = DeckPart(
                    deckId = "bookmark-deck",
                    partIndex = 0,
                    startIndex = 0,
                    endIndex = 1,
                    count = 1,
                ),
                session = null,
            ),
        ),
        totalCount = 1,
    )

    val sampleCardListUiState = CardListUiState(
        isLoading = false,
        deck = sampleDeck,
        cards = sampleCards,
        filteredCards = sampleCards,
        searchQuery = "",
        allSubjectCards = sampleCards,
    )

    val sampleStudyUiState = StudyUiState(
        isLoading = false,
        deck = sampleDeck,
        partIndex = 0,
        currentCard = sampleCards[0],
        nextCard = sampleCards[1],
        isFlipped = false,
        queueSize = 5,
        totalInPart = 5,
        completedCount = 2,
        retryCount = 1,
        isPartCompleted = false,
        hasNextPart = false,
        subjectCards = sampleCards,
    )
}
