package com.aidomari.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.aidomari.app.core.database.entity.SourceArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceArticleDao {

    @Query("SELECT * FROM source_articles ORDER BY created_at DESC")
    fun getAllArticles(): Flow<List<SourceArticleEntity>>

    @Query("SELECT * FROM source_articles WHERE status = :status ORDER BY created_at DESC")
    fun getArticlesByStatus(status: String): Flow<List<SourceArticleEntity>>

    @Query("SELECT * FROM source_articles WHERE status = :status ORDER BY created_at DESC")
    suspend fun getArticlesByStatusSync(status: String): List<SourceArticleEntity>

    @Query("SELECT * FROM source_articles WHERE url = :url LIMIT 1")
    suspend fun getArticleByUrl(url: String): SourceArticleEntity?

    @Query("SELECT * FROM source_articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: String): SourceArticleEntity?

    @Upsert
    suspend fun insertOrUpdateArticle(article: SourceArticleEntity)

    @Query("UPDATE source_articles SET status = :status, generated_card_count = :cardCount WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, cardCount: Int = 0)

    @Delete
    suspend fun deleteArticle(article: SourceArticleEntity)

    @Query("DELETE FROM source_articles WHERE id = :id")
    suspend fun deleteArticleById(id: String)

    @Query("DELETE FROM source_articles WHERE created_at < :cutoffTimestamp")
    suspend fun deleteArticlesOlderThan(cutoffTimestamp: Long): Int
}
