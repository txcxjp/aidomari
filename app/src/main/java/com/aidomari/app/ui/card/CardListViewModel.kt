package com.aidomari.app.ui.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class CardListViewModel(
    private val deckId: String,
    private val repository: WordsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardListUiState())
    val uiState: StateFlow<CardListUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val deck = repository.getDeckById(deckId)
            val cards = repository.getCardsByDeckSync(deckId)
            val allSubjectCards = if (deck != null) {
                repository.getAllCardsInSubject(deck.subjectId)
            } else {
                emptyList()
            }
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    deck = deck,
                    cards = cards,
                    filteredCards = filterCards(cards, state.searchQuery),
                    allSubjectCards = allSubjectCards,
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredCards = filterCards(state.cards, query),
            )
        }
    }

    private fun filterCards(cards: List<Card>, query: String): List<Card> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return cards
        return cards.filter { card ->
            card.front.lowercase().contains(trimmed) ||
                card.back.lowercase().contains(trimmed) ||
                card.userQuestion.lowercase().contains(trimmed) ||
                card.userSupplement.lowercase().contains(trimmed)
        }
    }

    fun toggleBookmark(card: Card) {
        val newBookmarkState = !card.isBookmarked
        val updatedCard = card.copy(isBookmarked = newBookmarkState)
        updateCardInState(updatedCard)
        viewModelScope.launch {
            repository.setBookmark(card.id, newBookmarkState)
        }
    }

    fun saveCard(
        cardId: String?,
        front: String,
        back: String,
        userQuestion: String = "",
        userSupplement: String = "",
    ) {
        val trimmedFront = front.trim()
        val trimmedBack = back.trim()
        val trimmedQuestion = userQuestion.trim()
        val trimmedSupplement = userSupplement.trim()

        if (trimmedFront.isEmpty() || trimmedBack.isEmpty()) return

        val existingCard = _uiState.value.cards.find { it.id == cardId }
        val cardToSave = if (existingCard != null) {
            existingCard.copy(
                front = trimmedFront,
                back = trimmedBack,
                userQuestion = trimmedQuestion,
                userSupplement = trimmedSupplement,
            )
        } else {
            Card(
                id = cardId ?: UUID.randomUUID().toString(),
                deckId = deckId,
                front = trimmedFront,
                back = trimmedBack,
                userQuestion = trimmedQuestion,
                userSupplement = trimmedSupplement,
                orderIndex = _uiState.value.cards.size,
                createdAt = System.currentTimeMillis(),
            )
        }

        val updatedCards = if (existingCard != null) {
            _uiState.value.cards.map { if (it.id == cardToSave.id) cardToSave else it }
        } else {
            _uiState.value.cards + cardToSave
        }

        _uiState.update { state ->
            state.copy(
                cards = updatedCards,
                filteredCards = filterCards(updatedCards, state.searchQuery),
            )
        }

        viewModelScope.launch {
            repository.saveCard(cardToSave)
        }
    }

    fun updateCardNotes(cardId: String, userQuestion: String, userSupplement: String) {
        val targetCard = _uiState.value.allSubjectCards.find { it.id == cardId }
            ?: _uiState.value.cards.find { it.id == cardId }
            ?: return
        val updatedCard = targetCard.copy(
            userQuestion = userQuestion.trim(),
            userSupplement = userSupplement.trim(),
        )
        updateCardInState(updatedCard)
        viewModelScope.launch {
            repository.saveCard(updatedCard)
        }
    }

    fun deleteCard(cardId: String) {
        val updatedCards = _uiState.value.cards.filterNot { it.id == cardId }
        val updatedAllSubject = _uiState.value.allSubjectCards.filterNot { it.id == cardId }
        _uiState.update { state ->
            state.copy(
                cards = updatedCards,
                filteredCards = filterCards(updatedCards, state.searchQuery),
                allSubjectCards = updatedAllSubject,
            )
        }
        viewModelScope.launch {
            repository.deleteCard(cardId)
        }
    }

    private fun updateCardInState(updatedCard: Card) {
        _uiState.update { state ->
            val updatedCards = state.cards.map {
                if (it.id == updatedCard.id) updatedCard else it
            }
            state.copy(
                cards = updatedCards,
                filteredCards = filterCards(updatedCards, state.searchQuery),
            )
        }
    }
}
