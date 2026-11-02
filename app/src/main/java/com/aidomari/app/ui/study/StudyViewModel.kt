package com.aidomari.app.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.DeckPart
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StudyViewModel(
    private val repository: WordsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    private var currentDeck: Deck? = null
    private var allCards: List<Card> = emptyList()
    private var cardsMap: Map<String, Card> = emptyMap()

    private val queueCardIds = mutableListOf<String>()
    private val retryCardIds = mutableListOf<String>()
    private var currentPartIndex: Int = 0
    private var totalInPart: Int = 0
    private var completedCount: Int = 0

    fun loadDeckAndPart(deckId: String, partIndex: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val isBookmarkMode = deckId.startsWith("bookmark_")
            val isRandomMode = deckId.startsWith("random_")
            val deck = when {
                isBookmarkMode -> {
                    val subjectId = deckId.removePrefix("bookmark_")
                    repository.getOrCreateBookmarkDeck(subjectId)
                }
                isRandomMode -> {
                    val rest = deckId.removePrefix("random_")
                    val countPart = rest.substringBefore("_")
                    val subjectId = rest.substringAfter("_")
                    val questionCount = countPart.toIntOrNull() ?: 10
                    val baseDeck = repository.getOrCreateRandomDeck(subjectId)
                    val countLabel = if (questionCount <= 0) "全問" else "${questionCount}問"
                    baseDeck.copy(
                        id = deckId,
                        name = "ランダム出題 ($countLabel)",
                        shuffleMode = true,
                    )
                }
                else -> {
                    repository.getDeckById(deckId) ?: return@launch
                }
            }
            currentDeck = deck
            currentPartIndex = partIndex

            val subjectCards = repository.getAllCardsInSubject(deck.subjectId)
            val cards = when {
                isBookmarkMode -> {
                    repository.getBookmarkedCardsBySubject(deck.subjectId).first()
                }
                isRandomMode -> {
                    val rest = deckId.removePrefix("random_")
                    val countPart = rest.substringBefore("_")
                    val questionCount = countPart.toIntOrNull() ?: 10
                    val shuffled = subjectCards.shuffled()
                    if (questionCount > 0 && questionCount < shuffled.size) {
                        shuffled.take(questionCount)
                    } else {
                        shuffled
                    }
                }
                else -> {
                    repository.getCardsByDeckSync(deckId)
                }
            }
            allCards = cards
            cardsMap = cards.associateBy { it.id }

            val parts = if (isRandomMode) {
                listOf(
                    DeckPart(
                        partIndex = 0,
                        partNumber = 1,
                        startIndex = 0,
                        endIndex = cards.size,
                        count = cards.size,
                    ),
                )
            } else {
                deck.calculateParts(cards.size)
            }
            val currentPart = parts.getOrNull(partIndex)
            val hasNextPart = partIndex + 1 < parts.size

            if (currentPart == null || cards.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        deck = deck,
                        partIndex = partIndex,
                        currentCard = null,
                        isPartCompleted = true,
                        hasNextPart = false,
                    )
                }
                return@launch
            }

            totalInPart = currentPart.count
            val partCards = cards.subList(
                currentPart.startIndex.coerceAtMost(cards.size),
                currentPart.endIndex.coerceAtMost(cards.size),
            )

            // Check if there is an existing non-completed session
            val savedSession = if (isRandomMode) null else repository.getSession(deckId)
            val canResume = savedSession != null &&
                savedSession.partIndex == partIndex &&
                !savedSession.isCompleted &&
                (savedSession.queueCardIds.isNotEmpty() || savedSession.retryCardIds.isNotEmpty())

            queueCardIds.clear()
            retryCardIds.clear()

            if (canResume && savedSession != null) {
                queueCardIds.addAll(savedSession.queueCardIds)
                retryCardIds.addAll(savedSession.retryCardIds)
                completedCount = (totalInPart - (queueCardIds.size + retryCardIds.size)).coerceAtLeast(0)
            } else {
                val cardIds = if (deck.shuffleMode) {
                    partCards.map { it.id }.shuffled()
                } else {
                    partCards.map { it.id }
                }
                queueCardIds.addAll(cardIds)
                completedCount = 0
            }

            val firstCardId = queueCardIds.firstOrNull()
            val firstCard = firstCardId?.let { cardsMap[it] }
            val nextCardId = queueCardIds.drop(1).firstOrNull() ?: retryCardIds.firstOrNull()
            val nextCard = nextCardId?.let { cardsMap[it] }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    deck = deck,
                    partIndex = partIndex,
                    currentCard = firstCard,
                    nextCard = nextCard,
                    isFlipped = false,
                    queueSize = queueCardIds.size,
                    totalInPart = totalInPart,
                    completedCount = completedCount,
                    retryCount = retryCardIds.size,
                    isPartCompleted = firstCard == null,
                    hasNextPart = hasNextPart,
                    subjectCards = subjectCards,
                )
            }
        }
    }

    fun flipCard() {
        val state = _uiState.value
        if (state.currentCard == null || state.isPartCompleted) return
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun markDone() {
        val state = _uiState.value
        val currentCard = state.currentCard ?: return
        if (state.isPartCompleted) return

        queueCardIds.remove(currentCard.id)
        completedCount++

        advanceToNextCard()
    }

    fun markRetry() {
        val state = _uiState.value
        val currentCard = state.currentCard ?: return
        if (state.isPartCompleted) return

        queueCardIds.remove(currentCard.id)
        retryCardIds.add(currentCard.id)

        advanceToNextCard()
    }

    fun toggleBookmark() {
        val card = _uiState.value.currentCard ?: return
        toggleCardBookmark(card)
    }

    fun toggleCardBookmark(card: Card) {
        val newBookmarkState = !card.isBookmarked
        val updatedCard = card.copy(isBookmarked = newBookmarkState)

        cardsMap = cardsMap + (card.id to updatedCard)
        _uiState.update { state ->
            val updatedSubjectCards = state.subjectCards.map {
                if (it.id == card.id) updatedCard else it
            }
            state.copy(
                currentCard = if (state.currentCard?.id == card.id) updatedCard else state.currentCard,
                nextCard = if (state.nextCard?.id == card.id) updatedCard else state.nextCard,
                subjectCards = updatedSubjectCards,
            )
        }

        viewModelScope.launch {
            repository.setBookmark(card.id, newBookmarkState)
        }
    }

    fun updateCardNotes(cardId: String, userQuestion: String, userSupplement: String) {
        val card = cardsMap[cardId]
            ?: _uiState.value.subjectCards.find { it.id == cardId }
            ?: _uiState.value.currentCard
            ?: return
        val updatedCard = card.copy(
            userQuestion = userQuestion.trim(),
            userSupplement = userSupplement.trim(),
        )

        cardsMap = cardsMap + (cardId to updatedCard)
        _uiState.update { state ->
            val updatedSubjectCards = state.subjectCards.map {
                if (it.id == cardId) updatedCard else it
            }
            state.copy(
                currentCard = if (state.currentCard?.id == cardId) updatedCard else state.currentCard,
                nextCard = if (state.nextCard?.id == cardId) updatedCard else state.nextCard,
                subjectCards = updatedSubjectCards,
            )
        }

        viewModelScope.launch {
            repository.saveCard(updatedCard)
        }
    }

    fun updateTtsSettings(autoSpeak: Boolean, speechRate: Float) {
        val deck = currentDeck ?: return
        val updatedDeck = deck.copy(
            autoSpeak = autoSpeak,
            speechRate = speechRate,
        )
        currentDeck = updatedDeck
        _uiState.update { it.copy(deck = updatedDeck) }

        viewModelScope.launch {
            repository.saveDeck(updatedDeck)
        }
    }

    fun toggleAutoSpeak() {
        val deck = currentDeck ?: return
        updateTtsSettings(autoSpeak = !deck.autoSpeak, speechRate = deck.speechRate)
    }

    fun searchCards(query: String) {
        val trimmed = query.trim()
        val results = if (trimmed.isBlank()) {
            emptyList()
        } else {
            _uiState.value.subjectCards.filter { card ->
                card.front.contains(trimmed, ignoreCase = true) ||
                    card.back.contains(trimmed, ignoreCase = true) ||
                    card.userQuestion.contains(trimmed, ignoreCase = true) ||
                    card.userSupplement.contains(trimmed, ignoreCase = true)
            }
        }
        _uiState.update {
            it.copy(
                searchQuery = query,
                searchResults = results,
            )
        }
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
            )
        }
    }

    fun deleteCard(cardId: String) {
        val isCurrentCard = _uiState.value.currentCard?.id == cardId
        val wasInQueue = queueCardIds.remove(cardId)
        val wasInRetry = retryCardIds.remove(cardId)

        if (wasInQueue || wasInRetry) {
            totalInPart = (totalInPart - 1).coerceAtLeast(0)
            completedCount = completedCount.coerceAtMost(totalInPart).coerceAtLeast(0)
        }

        allCards = allCards.filter { it.id != cardId }
        cardsMap = cardsMap - cardId

        _uiState.update { state ->
            val updatedSubjectCards = state.subjectCards.filter { it.id != cardId }
            val updatedSearchResults = state.searchResults.filter { it.id != cardId }
            state.copy(
                subjectCards = updatedSubjectCards,
                searchResults = updatedSearchResults,
                totalInPart = totalInPart,
                completedCount = completedCount,
                queueSize = queueCardIds.size,
                retryCount = retryCardIds.size,
            )
        }

        if (isCurrentCard) {
            advanceToNextCard()
        } else {
            val nextCardId = queueCardIds.drop(1).firstOrNull() ?: retryCardIds.firstOrNull()
            val nextCard = nextCardId?.let { cardsMap[it] }
            _uiState.update { it.copy(nextCard = nextCard) }
            saveCurrentSession(isCompleted = _uiState.value.isPartCompleted)
        }

        viewModelScope.launch {
            repository.deleteCard(cardId)
        }
    }

    fun deleteCurrentCard() {
        val card = _uiState.value.currentCard ?: return
        deleteCard(card.id)
    }

    fun restartPart() {
        val deck = currentDeck ?: return
        val parts = if (deck.id.startsWith("random_")) {
            listOf(
                DeckPart(
                    partIndex = 0,
                    partNumber = 1,
                    startIndex = 0,
                    endIndex = allCards.size,
                    count = allCards.size,
                ),
            )
        } else {
            deck.calculateParts(allCards.size)
        }
        val currentPart = parts.getOrNull(currentPartIndex) ?: return

        val partCards = allCards.subList(
            currentPart.startIndex.coerceAtMost(allCards.size),
            currentPart.endIndex.coerceAtMost(allCards.size),
        )

        queueCardIds.clear()
        retryCardIds.clear()

        val cardIds = if (deck.shuffleMode) {
            partCards.map { it.id }.shuffled()
        } else {
            partCards.map { it.id }
        }
        queueCardIds.addAll(cardIds)
        completedCount = 0

        val firstCard = queueCardIds.firstOrNull()?.let { cardsMap[it] }
        val nextCard = (queueCardIds.drop(1).firstOrNull() ?: retryCardIds.firstOrNull())?.let { cardsMap[it] }

        _uiState.update {
            it.copy(
                currentCard = firstCard,
                nextCard = nextCard,
                isFlipped = false,
                queueSize = queueCardIds.size,
                totalInPart = totalInPart,
                completedCount = 0,
                retryCount = 0,
                isPartCompleted = firstCard == null,
            )
        }

        saveCurrentSession(isCompleted = false)
    }

    fun onVolumeKey(isUp: Boolean): Boolean {
        val state = _uiState.value
        if (state.currentCard == null || state.isPartCompleted) return false

        return if (!state.isFlipped) {
            // Front face: Volume Up and Down both flip to back
            flipCard()
            true
        } else {
            // Back face: Volume Down = Done (Right swipe), Volume Up = Retry (Left swipe)
            if (isUp) {
                markRetry()
            } else {
                markDone()
            }
            true
        }
    }

    private fun advanceToNextCard() {
        // If current queue is empty but retry queue has cards, start retry round
        if (queueCardIds.isEmpty() && retryCardIds.isNotEmpty()) {
            queueCardIds.addAll(retryCardIds)
            retryCardIds.clear()
        }

        val isFinished = queueCardIds.isEmpty()
        val nextCardId = queueCardIds.firstOrNull()
        val nextCard = nextCardId?.let { cardsMap[it] }
        val subsequentCardId = queueCardIds.drop(1).firstOrNull() ?: retryCardIds.firstOrNull()
        val subsequentCard = subsequentCardId?.let { cardsMap[it] }

        _uiState.update {
            it.copy(
                currentCard = nextCard,
                nextCard = subsequentCard,
                isFlipped = false,
                queueSize = queueCardIds.size,
                completedCount = completedCount,
                retryCount = retryCardIds.size,
                isPartCompleted = isFinished,
            )
        }

        saveCurrentSession(isCompleted = isFinished)
    }

    private fun saveCurrentSession(isCompleted: Boolean) {
        val deck = currentDeck ?: return
        viewModelScope.launch {
            val session = StudySession(
                deckId = deck.id,
                partIndex = currentPartIndex,
                currentCardIndex = completedCount,
                queueCardIds = queueCardIds.toList(),
                retryCardIds = retryCardIds.toList(),
                isCompleted = isCompleted,
                lastStudiedAt = System.currentTimeMillis(),
            )
            repository.saveSession(session)
        }
    }
}
