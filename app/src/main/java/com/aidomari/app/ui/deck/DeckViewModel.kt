package com.aidomari.app.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aidomari.app.domain.model.Card
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

    private val _bookmarkDeckUiModel = MutableStateFlow<BookmarkDeckUiModel?>(null)
    val bookmarkDeckUiModel: StateFlow<BookmarkDeckUiModel?> = _bookmarkDeckUiModel.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Card>>(emptyList())
    val searchResults: StateFlow<List<Card>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _allSubjectCards = MutableStateFlow<List<Card>>(emptyList())
    val allSubjectCards: StateFlow<List<Card>> = _allSubjectCards.asStateFlow()

    init {
        loadDecks()
        observeBookmarks()
        loadAllSubjectCards()
    }

    fun loadAllSubjectCards() {
        viewModelScope.launch {
            _allSubjectCards.value = repository.getAllCardsInSubject(subjectId)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            val results = repository.searchCardsInSubject(subjectId, trimmed)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _isSearching.value = false
    }

    fun toggleBookmark(card: Card) {
        val newBookmarkState = !card.isBookmarked
        val updatedCard = card.copy(isBookmarked = newBookmarkState)
        _searchResults.value = _searchResults.value.map {
            if (it.id == card.id) updatedCard else it
        }
        _allSubjectCards.value = _allSubjectCards.value.map {
            if (it.id == card.id) updatedCard else it
        }
        viewModelScope.launch {
            repository.setBookmark(card.id, newBookmarkState)
        }
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

    private fun observeBookmarks() {
        viewModelScope.launch {
            repository.getBookmarkedCardsBySubject(subjectId).collect { bookmarkedCards ->
                if (bookmarkedCards.isEmpty()) {
                    _bookmarkDeckUiModel.value = null
                } else {
                    val bookmarkDeck = repository.getOrCreateBookmarkDeck(subjectId)
                    val parts = bookmarkDeck.calculateParts(bookmarkedCards.size)
                    val session = repository.getSession(bookmarkDeck.id)
                    val partModels = parts.map { part ->
                        val matchingSession = if (session?.partIndex == part.partIndex) session else null
                        DeckPartWithSession(part = part, session = matchingSession)
                    }
                    _bookmarkDeckUiModel.value = BookmarkDeckUiModel(
                        deck = bookmarkDeck,
                        parts = partModels,
                        totalCount = bookmarkedCards.size,
                    )
                }
            }
        }
    }

    fun createDeck(
        name: String,
        splitSize: Int = 50,
        autoContinue: Boolean = false,
        shuffleMode: Boolean = false,
        reverseMode: Boolean = false,
        autoSpeak: Boolean = false,
        speechRate: Float = 1.0f,
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
                reverseMode = reverseMode,
                autoSpeak = autoSpeak,
                speechRate = speechRate,
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

    fun toggleAutoSpeak(deckId: String) {
        val current = _decksWithParts.value.find { it.deck.id == deckId }?.deck ?: return
        updateDeck(current.copy(autoSpeak = !current.autoSpeak))
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            repository.deleteDeck(deckId)
        }
    }
}
