package com.aidomari.app.domain.model.article

data class SourceArticle(
    val id: String,
    val url: String,
    val title: String,
    val domain: String,
    val extractedText: String,
    val status: ArticleStatus = ArticleStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val generatedCardCount: Int = 0,
)
