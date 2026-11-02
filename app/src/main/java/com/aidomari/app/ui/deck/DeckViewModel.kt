package com.aidomari.app.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class DeckViewModel(
    private val subjectId: String,
    private val repository: WordsRepository,
) : ViewModel() {

    private val _decksWithParts = MutableStateFlow<List<DeckWithPartsUiModel>>(emptyList())
    val decksWithParts: StateFlow<List<DeckWithPartsUiModel>> = _decksWithParts.asStateFlow()

    init {
        loadDecks()
    }

    fun loadDecks() {
        viewModelScope.launch {
            repository.getDecksBySubject(subjectId).collect { decks ->
                val uiModels = decks.map { deck ->
                    val cards = repository.getCardsByDeckSync(deck.id)
                    val parts = deck.calculateParts(cards.size)
                    val session = repository.getSession(deck.id)

                    val partModels = parts.map { part ->
                        val matchingSession = if (session?.partIndex == part.partIndex) session else null
                        DeckPartWithSession(part = part, session = matchingSession)
                    }

                    DeckWithPartsUiModel(
                        deck = deck,
                        parts = partModels,
                        totalCardCount = cards.size,
                    )
                }
                _decksWithParts.value = uiModels
            }
        }
    }

    fun createDeck(
        name: String,
        splitSize: Int = 50,
        autoContinue: Boolean = false,
        shuffleMode: Boolean = false,
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newDeck = Deck(
                id = UUID.randomUUID().toString(),
                subjectId = subjectId,
                name = name.trim(),
                splitSize = splitSize,
                autoContinue = autoContinue,
                shuffleMode = shuffleMode,
                sortOrder = _decksWithParts.value.size,
            )
            repository.saveDeck(newDeck)
        }
    }

    fun updateDeck(deck: Deck) {
        viewModelScope.launch {
            repository.saveDeck(deck)
        }
    }

    fun toggleShuffle(deckId: String) {
        val current = _decksWithParts.value.find { it.deck.id == deckId }?.deck ?: return
        updateDeck(current.copy(shuffleMode = !current.shuffleMode))
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            repository.deleteDeck(deckId)
        }
    }
}
