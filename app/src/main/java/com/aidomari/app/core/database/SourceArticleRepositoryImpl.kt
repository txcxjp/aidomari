package com.aidomari.app.core.database

import com.aidomari.app.core.database.dao.SourceArticleDao
import com.aidomari.app.core.database.entity.SourceArticleEntity
import com.aidomari.app.core.privacy.PrivacyFilter
import com.aidomari.app.domain.model.article.ArticleStatus
import com.aidomari.app.domain.model.article.SourceArticle
import com.aidomari.app.domain.repository.CleanupResult
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
        val domain = extractDomain(cleanUrl)

        // プライバシー保護: 検索エンジンやログイン・内部ドメインは拒否
        require(!PrivacyFilter.isExcluded(cleanUrl, domain)) {
            "プライバシー保護のため、除外対象のドメイン/ページです: $domain"
        }

        val cleanTitle = if (title.isNotBlank()) title.trim() else extractFallbackTitle(cleanUrl)

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

    override suspend fun cleanupOldArticles(textRetentionDays: Int, pendingRetentionDays: Int): CleanupResult {
        val now = System.currentTimeMillis()
        val textCutoff = now - (textRetentionDays.toLong() * 24 * 60 * 60 * 1000)
        val pendingCutoff = now - (pendingRetentionDays.toLong() * 24 * 60 * 60 * 1000)

        // 1. 生成済み・破棄された古い記事の本文テキストをパージしてストレージ節約
        val purgedCount = dao.purgeExtractedTextOlderThan(
            cutoffTimestamp = textCutoff,
            status1 = ArticleStatus.GENERATED.name,
            status2 = ArticleStatus.DISCARDED.name,
        )

        // 2. 30日以上放置された未処理（PENDING）記事を削除
        val deletedCount = dao.deleteArticlesOlderThanAndStatus(
            cutoffTimestamp = pendingCutoff,
            status = ArticleStatus.PENDING.name,
        )

        return CleanupResult(purgedTextCount = purgedCount, deletedArticlesCount = deletedCount)
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
