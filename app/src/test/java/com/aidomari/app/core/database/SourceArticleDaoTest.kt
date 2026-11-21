package com.aidomari.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aidomari.app.core.database.dao.SourceArticleDao
import com.aidomari.app.core.database.entity.SourceArticleEntity
import com.aidomari.app.domain.model.article.ArticleStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SourceArticleDaoTest {

    private lateinit var db: AidomariDatabase
    private lateinit var dao: SourceArticleDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AidomariDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.sourceArticleDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetArticleByUrl() = runBlocking {
        val article = SourceArticleEntity(
            id = "art-1",
            url = "https://example.com/tech/1",
            title = "技術記事タイトル",
            domain = "example.com",
            extractedText = "技術記事本文...",
            status = ArticleStatus.PENDING.name,
            createdAt = 1000L,
            generatedCardCount = 0,
        )
        dao.insertOrUpdateArticle(article)

        val retrieved = dao.getArticleByUrl("https://example.com/tech/1")
        assertNotNull(retrieved)
        assertEquals("art-1", retrieved?.id)
        assertEquals("技術記事タイトル", retrieved?.title)
        assertEquals("example.com", retrieved?.domain)
        assertEquals("技術記事本文...", retrieved?.extractedText)
        assertEquals(ArticleStatus.PENDING.name, retrieved?.status)
    }

    @Test
    fun getArticlesByStatus_filtersCorrectly() = runBlocking {
        val pending1 = SourceArticleEntity(
            id = "art-1",
            url = "https://a.com",
            title = "A",
            domain = "a.com",
            extractedText = "content A",
            status = ArticleStatus.PENDING.name,
            createdAt = 1000L,
        )
        val generated1 = SourceArticleEntity(
            id = "art-2",
            url = "https://b.com",
            title = "B",
            domain = "b.com",
            extractedText = "content B",
            status = ArticleStatus.GENERATED.name,
            createdAt = 2000L,
            generatedCardCount = 5,
        )
        dao.insertOrUpdateArticle(pending1)
        dao.insertOrUpdateArticle(generated1)

        val pendingList = dao.getArticlesByStatus(ArticleStatus.PENDING.name).first()
        assertEquals(1, pendingList.size)
        assertEquals("art-1", pendingList[0].id)

        val generatedList = dao.getArticlesByStatusSync(ArticleStatus.GENERATED.name)
        assertEquals(1, generatedList.size)
        assertEquals("art-2", generatedList[0].id)
        assertEquals(5, generatedList[0].generatedCardCount)
    }

    @Test
    fun updateStatus_updatesStateAndCardCount() = runBlocking {
        val article = SourceArticleEntity(
            id = "art-1",
            url = "https://example.com",
            title = "Title",
            domain = "example.com",
            extractedText = "Content",
            status = ArticleStatus.PENDING.name,
        )
        dao.insertOrUpdateArticle(article)

        dao.updateStatus("art-1", ArticleStatus.GENERATED.name, cardCount = 3)

        val updated = dao.getArticleById("art-1")
        assertNotNull(updated)
        assertEquals(ArticleStatus.GENERATED.name, updated?.status)
        assertEquals(3, updated?.generatedCardCount)
    }

    @Test
    fun deleteArticlesOlderThan_removesExpiredEntries() = runBlocking {
        val oldArticle = SourceArticleEntity(
            id = "art-old",
            url = "https://old.com",
            title = "Old",
            domain = "old.com",
            extractedText = "...",
            createdAt = 100L,
        )
        val newArticle = SourceArticleEntity(
            id = "art-new",
            url = "https://new.com",
            title = "New",
            domain = "new.com",
            extractedText = "...",
            createdAt = 500L,
        )
        dao.insertOrUpdateArticle(oldArticle)
        dao.insertOrUpdateArticle(newArticle)

        val deletedCount = dao.deleteArticlesOlderThan(cutoffTimestamp = 300L)
        assertEquals(1, deletedCount)

        assertNull(dao.getArticleById("art-old"))
        assertNotNull(dao.getArticleById("art-new"))
    }
}
