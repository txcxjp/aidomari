package com.aidomari.app.ui.deck

import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.DeckPart
import com.aidomari.app.domain.model.StudySession

data class DeckPartWithSession(
    val part: DeckPart,
    val session: StudySession?,
)

data class DeckWithPartsUiModel(
    val deck: Deck,
    val parts: List<DeckPartWithSession>,
    val totalCardCount: Int,
)

data class BookmarkDeckUiModel(
    val deck: Deck,
    val parts: List<DeckPartWithSession>,
    val totalCount: Int,
)
