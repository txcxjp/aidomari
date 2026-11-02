package com.aidomari.app.ui.card

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck

data class CardListUiState(
    val isLoading: Boolean = true,
    val deck: Deck? = null,
    val cards: List<Card> = emptyList(),
    val filteredCards: List<Card> = emptyList(),
    val searchQuery: String = "",
    val allSubjectCards: List<Card> = emptyList(),
)
