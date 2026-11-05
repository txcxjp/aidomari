package com.aidomari.app.ui.lockscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aidomari.app.core.lockscreen.LockScreenPreferences
import com.aidomari.app.core.lockscreen.LockScreenTarget
import com.aidomari.app.core.tts.TtsManager
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LockScreenUiState(
    val isLoading: Boolean = true,
    val card: Card? = null,
    val isFlipped: Boolean = false,
    val isReverse: Boolean = false,
    val speechRate: Float = 1.0f,
    val ttsLanguage: String = "auto",
)

class LockScreenStudyViewModel(
    private val repository: WordsRepository,
    private val preferences: LockScreenPreferences,
    private val ttsManager: TtsManager? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockScreenUiState())
    val uiState: StateFlow<LockScreenUiState> = _uiState.asStateFlow()

    init {
        loadRandomCard()
    }

    fun loadRandomCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isFlipped = false) }
            val bookmarkOnly = preferences.config.value.target == LockScreenTarget.BOOKMARK_ONLY
            val card = repository.getRandomCard(bookmarkOnly)
            var lang = "auto"
            var isReverse = false
            var speechRate = 1.0f
            if (card != null) {
                val deck = repository.getDeckById(card.deckId)
                if (deck != null) {
                    isReverse = deck.reverseMode
                    speechRate = deck.speechRate
                    val subject = repository.getSubjectById(deck.subjectId)
                    if (subject != null) {
                        lang = subject.ttsLanguage
                    }
                }
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    card = card,
                    isFlipped = false,
                    isReverse = isReverse,
                    speechRate = speechRate,
                    ttsLanguage = lang,
                )
            }
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun markDone(onFinish: () -> Unit) {
        viewModelScope.launch {
            // 回答完了処理
            onFinish()
        }
    }

    fun markRetry(onFinish: () -> Unit) {
        viewModelScope.launch {
            // 苦手復習用: 必要なら自動ブックマーク等の処理
            uiState.value.card?.let { card ->
                repository.setBookmark(card.id, true)
            }
            onFinish()
        }
    }

    fun speakText(text: String) {
        ttsManager?.speak(
            text = text,
            languageTag = _uiState.value.ttsLanguage,
            speechRate = _uiState.value.speechRate,
        )
    }
}
