package com.aidomari.app.ui.digest

import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.model.ai.CardExtractionMode
import com.aidomari.app.domain.model.article.ArticleStatus
import com.aidomari.app.domain.model.article.SourceArticle
import java.util.UUID

data class CardCandidateItem(
    val id: String = UUID.randomUUID().toString(),
    val term: String,
    val description: String,
    val supplement: String = "",
    val isSelected: Boolean = true,
)

data class BrowsingDigestUiState(
    val articles: List<SourceArticle> = emptyList(),
    val pendingCount: Int = 0,
    val selectedMode: CardExtractionMode = CardExtractionMode.SMART,
    val isGenerating: Boolean = false,
    val generatingArticleId: String? = null,
    val generationProgressMessage: String? = null,
    val generationProgressFraction: Float? = null,
    val reviewArticle: SourceArticle? = null,
    val candidateCards: List<CardCandidateItem> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val selectedSubjectId: String? = null,
    val newDeckName: String = "",
    val isGeminiConfigured: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val filteredArticles: List<SourceArticle>
        get() = articles.filter { it.status == ArticleStatus.PENDING }

    val selectedCandidateCount: Int
        get() = candidateCards.count { it.isSelected }
}
