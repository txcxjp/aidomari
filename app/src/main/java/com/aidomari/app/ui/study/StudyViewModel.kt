package com.aidomari.app.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

            val deck = repository.getDeckById(deckId) ?: return@launch
            currentDeck = deck
            currentPartIndex = partIndex

            val cards = repository.getCardsByDeckSync(deckId)
            allCards = cards
            cardsMap = cards.associateBy { it.id }

            val parts = deck.calculateParts(cards.size)
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
            val savedSession = repository.getSession(deckId)
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

            _uiState.update {
                it.copy(
                    isLoading = false,
                    deck = deck,
                    partIndex = partIndex,
                    currentCard = firstCard,
                    isFlipped = false,
                    queueSize = queueCardIds.size,
                    totalInPart = totalInPart,
                    completedCount = completedCount,
                    retryCount = retryCardIds.size,
                    isPartCompleted = firstCard == null,
                    hasNextPart = hasNextPart,
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
        val newBookmarkState = !card.isBookmarked
        val updatedCard = card.copy(isBookmarked = newBookmarkState)

        cardsMap = cardsMap + (card.id to updatedCard)
        _uiState.update { it.copy(currentCard = updatedCard) }

        viewModelScope.launch {
            repository.setBookmark(card.id, newBookmarkState)
        }
    }

    fun restartPart() {
        val deck = currentDeck ?: return
        val parts = deck.calculateParts(allCards.size)
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

        _uiState.update {
            it.copy(
                currentCard = firstCard,
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

        _uiState.update {
            it.copy(
                currentCard = nextCard,
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
