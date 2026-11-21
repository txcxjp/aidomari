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
            url = "https://example.com/guide",
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

    @Test
    fun ingestArticle_rejectsExcludedSearchAndAuthDomains() = runBlocking {
        val searchResult = repository.ingestArticle(
            url = "https://www.google.com/search?q=secret",
            title = "Google検索",
            content = "検索結果",
        )
        assertTrue("Google search must be rejected", searchResult.isFailure)

        val authResult = repository.ingestArticle(
            url = "https://accounts.google.com/signin",
            title = "ログイン",
            content = "認証情報",
        )
        assertTrue("Auth page must be rejected", authResult.isFailure)

        val localResult = repository.ingestArticle(
            url = "http://localhost:8080/admin",
            title = "Local",
            content = "ローカル",
        )
        assertTrue("Localhost must be rejected", localResult.isFailure)
    }

    @Test
    fun cleanupOldArticles_purgesOldGeneratedTextAndDeletesOldPending() = runBlocking {
        val now = System.currentTimeMillis()
        val eightDaysAgo = now - (8L * 24 * 60 * 60 * 1000)
        val twoDaysAgo = now - (2L * 24 * 60 * 60 * 1000)
        val thirtyFiveDaysAgo = now - (35L * 24 * 60 * 60 * 1000)

        // 1. 8日前の生成済み記事
        val oldGenerated = repository.ingestArticle(
            url = "https://example.com/old-generated",
            title = "Old Generated",
            content = "長い記事本文...",
        ).getOrThrow()
        repository.updateArticleStatus(oldGenerated.id, ArticleStatus.GENERATED, cardCount = 5)
        // created_at を直接8日前に改変
        db.sourceArticleDao().insertOrUpdateArticle(
            db.sourceArticleDao().getArticleById(oldGenerated.id)!!.copy(createdAt = eightDaysAgo),
        )

        // 2. 2日前の生成済み記事
        val recentGenerated = repository.ingestArticle(
            url = "https://example.com/recent-generated",
            title = "Recent Generated",
            content = "最近の記事本文...",
        ).getOrThrow()
        repository.updateArticleStatus(recentGenerated.id, ArticleStatus.GENERATED, cardCount = 3)
        db.sourceArticleDao().insertOrUpdateArticle(
            db.sourceArticleDao().getArticleById(recentGenerated.id)!!.copy(createdAt = twoDaysAgo),
        )

        // 3. 35日前の未処理記事
        val oldPending = repository.ingestArticle(
            url = "https://example.com/old-pending",
            title = "Old Pending",
            content = "放置された記事...",
        ).getOrThrow()
        db.sourceArticleDao().insertOrUpdateArticle(
            db.sourceArticleDao().getArticleById(oldPending.id)!!.copy(createdAt = thirtyFiveDaysAgo),
        )

        // クリーンアップ実行 (7日以上経過した生成済み本文パージ、30日以上経過した未処理記事削除)
        val cleanupResult = repository.cleanupOldArticles(textRetentionDays = 7, pendingRetentionDays = 30)
        assertEquals(1, cleanupResult.purgedTextCount)
        assertEquals(1, cleanupResult.deletedArticlesCount)

        // 検証
        val updatedOldGen = repository.getArticleById(oldGenerated.id)
        assertNotNull(updatedOldGen)
        assertEquals("", updatedOldGen!!.extractedText) // パージ済み
        assertEquals(ArticleStatus.GENERATED, updatedOldGen.status)

        val updatedRecentGen = repository.getArticleById(recentGenerated.id)
        assertNotNull(updatedRecentGen)
        assertEquals("最近の記事本文...", updatedRecentGen!!.extractedText) // 保持

        val updatedOldPending = repository.getArticleById(oldPending.id)
        assertEquals(null, updatedOldPending) // 削除済み
    }
}
