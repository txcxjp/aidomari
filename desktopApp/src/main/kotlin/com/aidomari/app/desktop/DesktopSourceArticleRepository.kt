package com.aidomari.app.desktop

import com.aidomari.app.core.privacy.PrivacyFilter
import com.aidomari.app.domain.model.article.ArticleStatus
import com.aidomari.app.domain.model.article.SourceArticle
import com.aidomari.app.domain.repository.CleanupResult
import com.aidomari.app.domain.repository.SourceArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.net.URI
import java.sql.ResultSet
import java.util.UUID

/**
 * SQLite-backed [SourceArticleRepository] implementation for Compose Desktop.
 */
class DesktopSourceArticleRepository(
    private val db: DesktopDatabase,
) : SourceArticleRepository {

    override fun getPendingArticles(): Flow<List<SourceArticle>> =
        db.changeVersion
            .map { getPendingArticlesSync() }
            .distinctUntilChanged()

    override fun getAllArticles(): Flow<List<SourceArticle>> =
        db.changeVersion
            .map {
                db.read { conn ->
                    conn.prepareStatement(
                        "SELECT * FROM source_articles ORDER BY created_at DESC",
                    ).use { ps ->
                        ps.executeQuery().use { rs ->
                            buildList {
                                while (rs.next()) add(rs.toSourceArticle())
                            }
                        }
                    }
                }
            }
            .distinctUntilChanged()

    override suspend fun getPendingArticlesSync(): List<SourceArticle> = db.read { conn ->
        conn.prepareStatement(
            "SELECT * FROM source_articles WHERE status = ? ORDER BY created_at DESC",
        ).use { ps ->
            ps.setString(1, ArticleStatus.PENDING.name)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(rs.toSourceArticle())
                }
            }
        }
    }

    override suspend fun getArticleById(id: String): SourceArticle? = db.read { conn ->
        conn.prepareStatement("SELECT * FROM source_articles WHERE id = ?").use { ps ->
            ps.setString(1, id)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.toSourceArticle() else null
            }
        }
    }

    override suspend fun ingestArticle(
        url: String,
        title: String,
        content: String,
    ): Result<SourceArticle> = runCatching {
        require(url.isNotBlank()) { "URL cannot be blank" }
        require(content.isNotBlank()) { "Article content cannot be blank" }

        val cleanUrl = url.trim()
        val domain = extractDomain(cleanUrl)

        require(!PrivacyFilter.isExcluded(cleanUrl, domain)) {
            "プライバシー保護のため、除外対象のドメイン/ページです: $domain"
        }

        val cleanTitle = if (title.isNotBlank()) title.trim() else extractFallbackTitle(cleanUrl)
        val now = System.currentTimeMillis()
        val trimmedContent = content.trim()

        db.write { conn ->
            val existing = conn.prepareStatement(
                "SELECT * FROM source_articles WHERE url = ? LIMIT 1",
            ).use { ps ->
                ps.setString(1, cleanUrl)
                ps.executeQuery().use { rs ->
                    if (rs.next()) rs.toSourceArticle() else null
                }
            }

            val articleToSave = if (existing != null) {
                existing.copy(
                    title = cleanTitle,
                    domain = domain,
                    extractedText = trimmedContent,
                    createdAt = now,
                    status = ArticleStatus.PENDING,
                )
            } else {
                SourceArticle(
                    id = UUID.randomUUID().toString(),
                    url = cleanUrl,
                    title = cleanTitle,
                    domain = domain,
                    extractedText = trimmedContent,
                    status = ArticleStatus.PENDING,
                    createdAt = now,
                    generatedCardCount = 0,
                )
            }

            conn.prepareStatement(
                """
                INSERT INTO source_articles (
                    id, url, title, domain, extracted_text, status, created_at, generated_card_count
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    url = excluded.url,
                    title = excluded.title,
                    domain = excluded.domain,
                    extracted_text = excluded.extracted_text,
                    status = excluded.status,
                    created_at = excluded.created_at,
                    generated_card_count = excluded.generated_card_count
                """.trimIndent(),
            ).use { ps ->
                ps.setString(1, articleToSave.id)
                ps.setString(2, articleToSave.url)
                ps.setString(3, articleToSave.title)
                ps.setString(4, articleToSave.domain)
                ps.setString(5, articleToSave.extractedText)
                ps.setString(6, articleToSave.status.name)
                ps.setLong(7, articleToSave.createdAt)
                ps.setInt(8, articleToSave.generatedCardCount)
                ps.executeUpdate()
            }

            articleToSave
        }
    }

    override suspend fun updateArticleStatus(id: String, status: ArticleStatus, cardCount: Int) {
        db.write { conn ->
            conn.prepareStatement(
                "UPDATE source_articles SET status = ?, generated_card_count = ? WHERE id = ?",
            ).use { ps ->
                ps.setString(1, status.name)
                ps.setInt(2, cardCount)
                ps.setString(3, id)
                ps.executeUpdate()
            }
        }
    }

    override suspend fun deleteArticle(id: String) {
        db.write { conn ->
            conn.prepareStatement("DELETE FROM source_articles WHERE id = ?").use { ps ->
                ps.setString(1, id)
                ps.executeUpdate()
            }
        }
    }

    override suspend fun cleanupOldArticles(
        textRetentionDays: Int,
        pendingRetentionDays: Int,
    ): CleanupResult {
        val now = System.currentTimeMillis()
        val textCutoff = now - (textRetentionDays.toLong() * 24 * 60 * 60 * 1000)
        val pendingCutoff = now - (pendingRetentionDays.toLong() * 24 * 60 * 60 * 1000)

        return db.write { conn ->
            val purgedCount = conn.prepareStatement(
                """
                UPDATE source_articles
                SET extracted_text = ''
                WHERE created_at < ?
                  AND status IN (?, ?)
                  AND extracted_text != ''
                """.trimIndent(),
            ).use { ps ->
                ps.setLong(1, textCutoff)
                ps.setString(2, ArticleStatus.GENERATED.name)
                ps.setString(3, ArticleStatus.DISCARDED.name)
                ps.executeUpdate()
            }

            val deletedCount = conn.prepareStatement(
                "DELETE FROM source_articles WHERE created_at < ? AND status = ?",
            ).use { ps ->
                ps.setLong(1, pendingCutoff)
                ps.setString(2, ArticleStatus.PENDING.name)
                ps.executeUpdate()
            }

            CleanupResult(purgedTextCount = purgedCount, deletedArticlesCount = deletedCount)
        }
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

    private fun ResultSet.toSourceArticle(): SourceArticle = SourceArticle(
        id = getString("id"),
        url = getString("url"),
        title = getString("title"),
        domain = getString("domain"),
        extractedText = getString("extracted_text"),
        status = runCatching { ArticleStatus.valueOf(getString("status")) }
            .getOrDefault(ArticleStatus.PENDING),
        createdAt = getLong("created_at"),
        generatedCardCount = getInt("generated_card_count"),
    )
}
