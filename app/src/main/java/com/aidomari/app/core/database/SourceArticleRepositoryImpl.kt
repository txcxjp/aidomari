package com.aidomari.app.core.database

import com.aidomari.app.core.database.dao.SourceArticleDao
import com.aidomari.app.core.database.entity.SourceArticleEntity
import com.aidomari.app.domain.model.article.ArticleStatus
import com.aidomari.app.domain.model.article.SourceArticle
import com.aidomari.app.domain.repository.SourceArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.URI
import java.util.UUID

class SourceArticleRepositoryImpl(
    private val dao: SourceArticleDao,
) : SourceArticleRepository {

    override fun getPendingArticles(): Flow<List<SourceArticle>> = dao.getArticlesByStatus(ArticleStatus.PENDING.name).map { list ->
        list.map { it.toDomain() }
    }

    override fun getAllArticles(): Flow<List<SourceArticle>> = dao.getAllArticles().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun getPendingArticlesSync(): List<SourceArticle> = dao.getArticlesByStatusSync(ArticleStatus.PENDING.name).map { it.toDomain() }

    override suspend fun getArticleById(id: String): SourceArticle? = dao.getArticleById(id)?.toDomain()

    override suspend fun ingestArticle(
        url: String,
        title: String,
        content: String,
    ): Result<SourceArticle> = runCatching {
        require(url.isNotBlank()) { "URL cannot be blank" }
        require(content.isNotBlank()) { "Article content cannot be blank" }

        val cleanUrl = url.trim()
        val cleanTitle = if (title.isNotBlank()) title.trim() else extractFallbackTitle(cleanUrl)
        val domain = extractDomain(cleanUrl)

        val existing = dao.getArticleByUrl(cleanUrl)
        val articleToSave = if (existing != null) {
            existing.copy(
                title = cleanTitle,
                domain = domain,
                extractedText = content.trim(),
                createdAt = System.currentTimeMillis(),
                status = ArticleStatus.PENDING.name,
            )
        } else {
            SourceArticleEntity(
                id = UUID.randomUUID().toString(),
                url = cleanUrl,
                title = cleanTitle,
                domain = domain,
                extractedText = content.trim(),
                status = ArticleStatus.PENDING.name,
                createdAt = System.currentTimeMillis(),
                generatedCardCount = 0,
            )
        }

        dao.insertOrUpdateArticle(articleToSave)
        articleToSave.toDomain()
    }

    override suspend fun updateArticleStatus(id: String, status: ArticleStatus, cardCount: Int) {
        dao.updateStatus(id, status.name, cardCount)
    }

    override suspend fun deleteArticle(id: String) {
        dao.deleteArticleById(id)
    }

    private fun extractDomain(urlString: String): String = try {
        val uri = URI(urlString)
        val host = uri.host ?: urlString
        host.removePrefix("www.")
    } catch (_: Exception) {
        urlString.take(30)
    }

    private fun extractFallbackTitle(url: String): String = try {
        val uri = URI(url)
        val path = uri.path?.trim('/') ?: ""
        if (path.isNotEmpty()) {
            path.substringAfterLast('/')
        } else {
            uri.host ?: url
        }
    } catch (_: Exception) {
        url
    }
}
