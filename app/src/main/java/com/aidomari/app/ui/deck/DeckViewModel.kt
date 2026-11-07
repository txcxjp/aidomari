package com.aidomari.app.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.PastDayDeckType
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.repository.WordsRepository
import com.aidomari.app.domain.util.CardSnoozeCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class DeckViewModel(
    val subjectId: String,
    private val repository: WordsRepository,
) : ViewModel() {

    private val _decksWithParts = MutableStateFlow<List<DeckWithPartsUiModel>>(emptyList())
    val decksWithParts: StateFlow<List<DeckWithPartsUiModel>> = _decksWithParts.asStateFlow()

    private val _bookmarkDeckUiModel = MutableStateFlow<BookmarkDeckUiModel?>(null)
    val bookmarkDeckUiModel: StateFlow<BookmarkDeckUiModel?> = _bookmarkDeckUiModel.asStateFlow()

    private val _pastDayDecks = MutableStateFlow<List<PastDayDeckUiModel>>(emptyList())
    val pastDayDecks: StateFlow<List<PastDayDeckUiModel>> = _pastDayDecks.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Card>>(emptyList())
    val searchResults: StateFlow<List<Card>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _allSubjectCards = MutableStateFlow<List<Card>>(emptyList())
    val allSubjectCards: StateFlow<List<Card>> = _allSubjectCards.asStateFlow()

    private val _randomQuestionCount = MutableStateFlow(10)
    val randomQuestionCount: StateFlow<Int> = _randomQuestionCount.asStateFlow()

    private val _isSubjectArchived = MutableStateFlow(false)
    val isSubjectArchived: StateFlow<Boolean> = _isSubjectArchived.asStateFlow()

    init {
        loadSubjectInfo()
        loadDecks()
        observeBookmarks()
        observePastDayDecks()
        loadAllSubjectCards()
    }

    fun loadSubjectInfo() {
        viewModelScope.launch {
            val subject = repository.getSubjectById(subjectId)
            _isSubjectArchived.value = subject?.isArchived == true
        }
    }

    fun unarchiveSubject(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val subject = repository.getSubjectById(subjectId) ?: return@launch
            repository.saveSubject(subject.copy(isArchived = false))
            _isSubjectArchived.value = false
            onComplete()
        }
    }

    fun loadAllSubjectCards() {
        viewModelScope.launch {
            _allSubjectCards.value = repository.getAllCardsInSubject(subjectId)
        }
    }

    fun setRandomQuestionCount(count: Int) {
        _randomQuestionCount.value = count
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

    fun updateCardNotes(cardId: String, userQuestion: String, userSupplement: String) {
        val q = userQuestion.trim()
        val s = userSupplement.trim()
        _searchResults.value = _searchResults.value.map {
            if (it.id == cardId) it.copy(userQuestion = q, userSupplement = s) else it
        }
        _allSubjectCards.value = _allSubjectCards.value.map {
            if (it.id == cardId) it.copy(userQuestion = q, userSupplement = s) else it
        }
        viewModelScope.launch {
            val card = repository.getCardById(cardId) ?: return@launch
            repository.saveCard(card.copy(userQuestion = q, userSupplement = s))
        }
    }

    fun snoozeCard(card: Card, onSnoozed: (durationLabel: String) -> Unit = {}) {
        viewModelScope.launch {
            val updated = repository.snoozeCard(card.id) ?: return@launch
            val label = CardSnoozeCalculator.calculateNextSnooze(card.snoozeLevel).durationLabel
            onSnoozed(label)
            _searchResults.value = _searchResults.value.map {
                if (it.id == card.id) updated else it
            }
            _allSubjectCards.value = _allSubjectCards.value.map {
                if (it.id == card.id) updated else it
            }
        }
    }

    fun unsnoozeCard(card: Card, onUnsnoozed: () -> Unit = {}) {
        viewModelScope.launch {
            val updated = repository.unsnoozeCard(card.id) ?: return@launch
            onUnsnoozed()
            _searchResults.value = _searchResults.value.map {
                if (it.id == card.id) updated else it
            }
            _allSubjectCards.value = _allSubjectCards.value.map {
                if (it.id == card.id) updated else it
            }
        }
    }

    fun loadDecks() {
        viewModelScope.launch {
            repository.getDecksBySubject(subjectId).collect { decks ->
                val uiModels = decks.map { deck ->
                    val cards = repository.getCardsByDeckSync(deck.id)
                    val parts = deck.calculateParts(cards.size)
                    val session = repository.getSession(deck.id)
                    val completedParts = session?.completedPartIndices ?: emptySet()

                    val partModels = parts.map { part ->
                        val isPartCompleted = completedParts.contains(part.partIndex) ||
                            (session?.partIndex == part.partIndex && session.isCompleted)
                        val matchingSession = if (session?.partIndex == part.partIndex) {
                            session.copy(isCompleted = isPartCompleted)
                        } else if (isPartCompleted) {
                            StudySession(
                                deckId = deck.id,
                                partIndex = part.partIndex,
                                isCompleted = true,
                                completedPartIndices = completedParts,
                            )
                        } else {
                            null
                        }
                        DeckPartWithSession(part = part, session = matchingSession)
                    }

                    val latestLogs = repository.getLatestLogsForDeckSync(deck.id)
                    val learnedCardCount = latestLogs.count { it.isCorrect }

                    DeckWithPartsUiModel(
                        deck = deck,
                        parts = partModels,
                        totalCardCount = cards.size,
                        learnedCardCount = learnedCardCount,
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
                    val completedParts = session?.completedPartIndices ?: emptySet()
                    val partModels = parts.map { part ->
                        val isPartCompleted = completedParts.contains(part.partIndex) ||
                            (session?.partIndex == part.partIndex && session.isCompleted)
                        val matchingSession = if (session?.partIndex == part.partIndex) {
                            session.copy(isCompleted = isPartCompleted)
                        } else if (isPartCompleted) {
                            StudySession(
                                deckId = bookmarkDeck.id,
                                partIndex = part.partIndex,
                                isCompleted = true,
                                completedPartIndices = completedParts,
                            )
                        } else {
                            null
                        }
                        DeckPartWithSession(part = part, session = matchingSession)
                    }
                    val learnedCardCount = bookmarkedCards.count { card ->
                        val recent = repository.getRecentLogsForCard(card.id, limit = 1)
                        recent.firstOrNull()?.isCorrect == true
                    }
                    _bookmarkDeckUiModel.value = BookmarkDeckUiModel(
                        deck = bookmarkDeck,
                        parts = partModels,
                        totalCount = bookmarkedCards.size,
                        learnedCardCount = learnedCardCount,
                    )
                }
            }
        }
    }

    private fun observePastDayDecks() {
        viewModelScope.launch {
            repository.getPastDayCardsBySubject(subjectId).collect { groups ->
                val order = listOf(
                    PastDayDeckType.YESTERDAY,
                    PastDayDeckType.WEEK,
                    PastDayDeckType.MONTH,
                )
                val uiModels = order.mapNotNull { type ->
                    val group = groups[type] ?: return@mapNotNull null
                    if (group.cards.isEmpty()) return@mapNotNull null

                    val deck = repository.getOrCreatePastDayDeck(subjectId, group.type)
                    val parts = deck.calculateParts(group.cards.size)
                    val session = repository.getSession(deck.id)
                    val completedParts = session?.completedPartIndices ?: emptySet()
                    val partModels = parts.map { part ->
                        val isPartCompleted = completedParts.contains(part.partIndex) ||
                            (session?.partIndex == part.partIndex && session.isCompleted)
                        val matchingSession = if (session?.partIndex == part.partIndex) {
                            session.copy(isCompleted = isPartCompleted)
                        } else if (isPartCompleted) {
                            StudySession(
                                deckId = deck.id,
                                partIndex = part.partIndex,
                                isCompleted = true,
                                completedPartIndices = completedParts,
                            )
                        } else {
                            null
                        }
                        DeckPartWithSession(part = part, session = matchingSession)
                    }
                    val learnedCardCount = group.cards.count { card ->
                        val recent = repository.getRecentLogsForCard(card.id, limit = 1)
                        recent.firstOrNull()?.isCorrect == true
                    }
                    val dateDescription = when (group.type) {
                        PastDayDeckType.YESTERDAY -> "1日前"
                        PastDayDeckType.WEEK -> "${group.daysAgo}日前"
                        PastDayDeckType.MONTH -> "${group.daysAgo}日前"
                    }
                    PastDayDeckUiModel(
                        type = group.type,
                        deck = deck,
                        parts = partModels,
                        totalCount = group.cards.size,
                        learnedCardCount = learnedCardCount,
                        targetDateDescription = dateDescription,
                    )
                }
                _pastDayDecks.value = uiModels
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
