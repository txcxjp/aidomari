package com.aidomari.app.domain.repository

import com.aidomari.app.domain.model.article.ArticleStatus
import com.aidomari.app.domain.model.article.SourceArticle
import kotlinx.coroutines.flow.Flow

data class CleanupResult(
    val purgedTextCount: Int = 0,
    val deletedArticlesCount: Int = 0,
)

interface SourceArticleRepository {
    fun getPendingArticles(): Flow<List<SourceArticle>>
    fun getAllArticles(): Flow<List<SourceArticle>>
    suspend fun getPendingArticlesSync(): List<SourceArticle>
    suspend fun getArticleById(id: String): SourceArticle?
    suspend fun ingestArticle(url: String, title: String, content: String): Result<SourceArticle>
    suspend fun updateArticleStatus(id: String, status: ArticleStatus, cardCount: Int = 0)
    suspend fun deleteArticle(id: String)
    suspend fun cleanupOldArticles(textRetentionDays: Int = 7, pendingRetentionDays: Int = 30): CleanupResult
}
