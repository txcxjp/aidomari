package com.aidomari.app.ui.digest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aidomari.app.core.ai.CardGenerationNotificationController
import com.aidomari.app.core.ai.GeminiApiClient
import com.aidomari.app.core.ai.GeminiPreferences
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.model.ai.CardExtractionMode
import com.aidomari.app.domain.model.article.ArticleStatus
import com.aidomari.app.domain.model.article.SourceArticle
import com.aidomari.app.domain.repository.SourceArticleRepository
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class BrowsingDigestViewModel(
    private val sourceArticleRepository: SourceArticleRepository,
    private val wordsRepository: WordsRepository,
    private val geminiApiClient: GeminiApiClient,
    private val geminiPreferences: GeminiPreferences,
    private val notificationController: CardGenerationNotificationController? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowsingDigestUiState())
    val uiState: StateFlow<BrowsingDigestUiState> = _uiState.asStateFlow()

    init {
        // 記事一覧と科目を監視
        viewModelScope.launch {
            combine(
                sourceArticleRepository.getAllArticles(),
                wordsRepository.getAllSubjects(),
                geminiPreferences.config,
            ) { articles, subjects, geminiConfig ->
                Triple(articles, subjects, geminiConfig.isConfigured)
            }.collect { (articles, subjects, isConfigured) ->
                _uiState.update { current ->
                    current.copy(
                        articles = articles,
                        pendingCount = articles.count { it.status == ArticleStatus.PENDING },
                        subjects = subjects,
                        isGeminiConfigured = isConfigured,
                        selectedSubjectId = current.selectedSubjectId ?: subjects.firstOrNull()?.id,
                    )
                }
            }
        }
    }

    fun selectSubject(subjectId: String) {
        _uiState.update { it.copy(selectedSubjectId = subjectId) }
    }

    fun selectMode(mode: CardExtractionMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun updateNewDeckName(name: String) {
        _uiState.update { it.copy(newDeckName = name) }
    }

    fun generateCards(article: SourceArticle, mode: CardExtractionMode = _uiState.value.selectedMode) {
        val config = geminiPreferences.config.value
        if (!config.isConfigured) {
            _uiState.update {
                it.copy(errorMessage = "Gemini APIキーが設定されていません。右上の設定から登録してください。")
            }
            return
        }

        _uiState.update {
            it.copy(
                isGenerating = true,
                generatingArticleId = article.id,
                generationProgressMessage = "生成準備中...",
                generationProgressFraction = 0.05f,
                selectedMode = mode,
                errorMessage = null,
            )
        }
        notificationController?.onGenerationStarted(article.title)

        viewModelScope.launch {
            val result = geminiApiClient.generateCardsFromArticle(
                title = article.title,
                url = article.url,
                content = article.extractedText,
                apiKey = config.apiKey,
                model = config.modelName,
                temperature = config.temperature,
                mode = mode,
                onProgress = { progress ->
                    val (message, fraction) = when (progress) {
                        is com.aidomari.app.core.ai.GenerationProgress.AnalyzingThemes -> Pair(progress.message, 0.15f)
                        is com.aidomari.app.core.ai.GenerationProgress.GeneratingThemeCards -> {
                            val base = 0.15f
                            val step = 0.75f / progress.totalThemes.coerceAtLeast(1)
                            val frac = (base + step * progress.currentThemeIndex).coerceIn(0.15f, 0.90f)
                            Pair(progress.displayMessage, frac)
                        }
                        is com.aidomari.app.core.ai.GenerationProgress.Deduplicating -> Pair(progress.displayMessage, 0.95f)
                    }
                    _uiState.update {
                        it.copy(
                            generationProgressMessage = message,
                            generationProgressFraction = fraction,
                        )
                    }
                    notificationController?.onGenerationProgress(article.title, message, fraction)
                },
            )

            result.fold(
                onSuccess = { proposals ->
                    val candidates = proposals.map { proposal ->
                        CardCandidateItem(
                            front = proposal.front,
                            back = proposal.back,
                            supplement = proposal.userSupplement.ifBlank {
                                "出典: ${proposal.sourceTitle}\n${proposal.sourceUrl}"
                            },
                            isSelected = true,
                        )
                    }

                    val defaultDeckName = article.title.take(24).trim().ifBlank {
                        "閲覧記事デッキ"
                    }

                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generatingArticleId = null,
                            generationProgressMessage = null,
                            generationProgressFraction = null,
                            reviewArticle = article,
                            candidateCards = candidates,
                            newDeckName = defaultDeckName,
                            errorMessage = if (candidates.isEmpty()) "カード候補が生成されませんでした。本文が短すぎる可能性があります。" else null,
                        )
                    }
                    if (candidates.isNotEmpty()) {
                        notificationController?.onGenerationCompleted(article.title, candidates.size)
                    } else {
                        notificationController?.onGenerationFailed(article.title, "カード候補が生成されませんでした")
                    }
                },
                onFailure = { error ->
                    val errorMsg = error.localizedMessage ?: "不明なエラー"
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generatingArticleId = null,
                            generationProgressMessage = null,
                            generationProgressFraction = null,
                            errorMessage = "カード生成に失敗しました: $errorMsg",
                        )
                    }
                    notificationController?.onGenerationFailed(article.title, errorMsg)
                },
            )
        }
    }

    fun toggleCardSelection(candidateId: String) {
        _uiState.update { state ->
            val updated = state.candidateCards.map {
                if (it.id == candidateId) it.copy(isSelected = !it.isSelected) else it
            }
            state.copy(candidateCards = updated)
        }
    }

    fun selectAllCandidates(select: Boolean) {
        _uiState.update { state ->
            val updated = state.candidateCards.map { it.copy(isSelected = select) }
            state.copy(candidateCards = updated)
        }
    }

    fun updateCandidate(candidateId: String, front: String, back: String, supplement: String) {
        _uiState.update { state ->
            val updated = state.candidateCards.map {
                if (it.id == candidateId) it.copy(front = front, back = back, supplement = supplement) else it
            }
            state.copy(candidateCards = updated)
        }
    }

    fun closeReview() {
        notificationController?.clearCompletionNotification()
        _uiState.update {
            it.copy(
                reviewArticle = null,
                candidateCards = emptyList(),
            )
        }
    }

    fun saveSelectedCards(createNewSubjectIfNone: Boolean = true) {
        val state = _uiState.value
        val article = state.reviewArticle ?: return
        val selectedCandidates = state.candidateCards.filter { it.isSelected && it.front.isNotBlank() && it.back.isNotBlank() }

        if (selectedCandidates.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "登録するカードが選択されていません") }
            return
        }

        viewModelScope.launch {
            try {
                // 1. 対象の Subject を特定または新規作成
                val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                var targetSubjectId = state.selectedSubjectId

                if (targetSubjectId == null && createNewSubjectIfNone) {
                    val newSubject = Subject(
                        id = UUID.randomUUID().toString(),
                        name = "Web閲覧学習 ($todayStr)",
                        ttsLanguage = "ja",
                    )
                    wordsRepository.saveSubject(newSubject)
                    targetSubjectId = newSubject.id
                }

                if (targetSubjectId == null) {
                    _uiState.update { it.copy(errorMessage = "登録先の科目がありません") }
                    return@launch
                }

                // 2. Deck を作成または特定
                val deckName = state.newDeckName.ifBlank {
                    article.title.take(24).ifBlank { "閲覧デッキ ($todayStr)" }
                }
                val deckId = UUID.randomUUID().toString()
                val deck = Deck(
                    id = deckId,
                    subjectId = targetSubjectId,
                    name = deckName,
                    splitSize = 50,
                    autoContinue = true,
                )
                wordsRepository.saveDeck(deck)

                // 3. Card 群を作成して保存
                val now = System.currentTimeMillis()
                val cards = selectedCandidates.mapIndexed { index, candidate ->
                    Card(
                        id = UUID.randomUUID().toString(),
                        deckId = deckId,
                        front = candidate.front.trim(),
                        back = candidate.back.trim(),
                        userSupplement = candidate.supplement.trim(),
                        orderIndex = index,
                        createdAt = now,
                    )
                }
                wordsRepository.saveCards(cards)

                // 4. SourceArticle のステータスを GENERATED に更新
                sourceArticleRepository.updateArticleStatus(
                    id = article.id,
                    status = ArticleStatus.GENERATED,
                    cardCount = cards.size,
                )

                // 5. 状態更新
                notificationController?.clearCompletionNotification()
                _uiState.update {
                    it.copy(
                        reviewArticle = null,
                        candidateCards = emptyList(),
                        successMessage = "「$deckName」に ${cards.size} 枚のカードを登録しました！",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "カードの保存に失敗しました: ${e.localizedMessage ?: "不明なエラー"}")
                }
            }
        }
    }

    fun deleteArticle(articleId: String) {
        viewModelScope.launch {
            sourceArticleRepository.deleteArticle(articleId)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
