package com.aidomari.app.ui.digest

import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.model.ai.CardExtractionMode
import com.aidomari.app.domain.model.article.ArticleStatus
import com.aidomari.app.domain.model.article.SourceArticle
import java.util.UUID

enum class ArticleFilterTab(val label: String) {
    PENDING("未処理"),
    GENERATED("生成済み"),
    ALL("すべて"),
}

data class CardCandidateItem(
    val id: String = UUID.randomUUID().toString(),
    val front: String,
    val back: String,
    val supplement: String = "",
    val isSelected: Boolean = true,
)

data class BrowsingDigestUiState(
    val articles: List<SourceArticle> = emptyList(),
    val pendingCount: Int = 0,
    val selectedTab: ArticleFilterTab = ArticleFilterTab.PENDING,
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
        get() = when (selectedTab) {
            ArticleFilterTab.PENDING -> articles.filter { it.status == ArticleStatus.PENDING }
            ArticleFilterTab.GENERATED -> articles.filter { it.status == ArticleStatus.GENERATED }
            ArticleFilterTab.ALL -> articles
        }

    val selectedCandidateCount: Int
        get() = candidateCards.count { it.isSelected }
}
