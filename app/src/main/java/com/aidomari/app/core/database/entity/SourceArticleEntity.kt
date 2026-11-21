package com.aidomari.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aidomari.app.domain.model.article.ArticleStatus
import com.aidomari.app.domain.model.article.SourceArticle

@Entity(
    tableName = "source_articles",
    indices = [
        Index("url"),
        Index("status"),
        Index("created_at"),
    ],
)
data class SourceArticleEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val title: String,
    val domain: String,
    @ColumnInfo(name = "extracted_text")
    val extractedText: String,
    val status: String = ArticleStatus.PENDING.name,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "generated_card_count")
    val generatedCardCount: Int = 0,
) {
    fun toDomain(): SourceArticle = SourceArticle(
        id = id,
        url = url,
        title = title,
        domain = domain,
        extractedText = extractedText,
        status = try {
            ArticleStatus.valueOf(status)
        } catch (_: Exception) {
            ArticleStatus.PENDING
        },
        createdAt = createdAt,
        generatedCardCount = generatedCardCount,
    )

    companion object {
        fun fromDomain(domain: SourceArticle): SourceArticleEntity = SourceArticleEntity(
            id = domain.id,
            url = domain.url,
            title = domain.title,
            domain = domain.domain,
            extractedText = domain.extractedText,
            status = domain.status.name,
            createdAt = domain.createdAt,
            generatedCardCount = domain.generatedCardCount,
        )
    }
}
