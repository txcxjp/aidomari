package com.aidomari.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aidomari.app.domain.model.article.ArticleStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SourceArticleRepositoryTest {

    private lateinit var db: AidomariDatabase
    private lateinit var repository: SourceArticleRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AidomariDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SourceArticleRepositoryImpl(db.sourceArticleDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun ingestArticle_extractsDomainAndPersists() = runBlocking {
        val result = repository.ingestArticle(
            url = "https://zenn.dev/user/articles/abc12345",
            title = "Kotlin入門",
            content = "Kotlinの基本的な文法...",
        )

        assertTrue(result.isSuccess)
        val article = result.getOrThrow()
        assertEquals("Kotlin入門", article.title)
        assertEquals("zenn.dev", article.domain)
        assertEquals("Kotlinの基本的な文法...", article.extractedText)
        assertEquals(ArticleStatus.PENDING, article.status)

        val pending = repository.getPendingArticles().first()
        assertEquals(1, pending.size)
        assertEquals(article.id, pending[0].id)
    }

    @Test
    fun ingestArticle_withDuplicateUrl_updatesExistingPending() = runBlocking {
        val first = repository.ingestArticle(
            url = "https://qiita.com/item/1",
            title = "First Title",
            content = "Old Content",
        ).getOrThrow()

        val second = repository.ingestArticle(
            url = "https://qiita.com/item/1",
            title = "Updated Title",
            content = "New Content",
        ).getOrThrow()

        assertEquals(first.id, second.id)
        assertEquals("Updated Title", second.title)
        assertEquals("New Content", second.extractedText)

        val all = repository.getAllArticles().first()
        assertEquals(1, all.size)
    }

    @Test
    fun updateArticleStatus_and_deleteArticle() = runBlocking {
        val article = repository.ingestArticle(
            url = "https://example.com",
            title = "Example",
            content = "Content",
        ).getOrThrow()

        repository.updateArticleStatus(article.id, ArticleStatus.GENERATED, cardCount = 4)
        val retrieved = repository.getArticleById(article.id)
        assertNotNull(retrieved)
        assertEquals(ArticleStatus.GENERATED, retrieved?.status)
        assertEquals(4, retrieved?.generatedCardCount)

        repository.deleteArticle(article.id)
        val deleted = repository.getArticleById(article.id)
        assertEquals(null, deleted)
    }
}
