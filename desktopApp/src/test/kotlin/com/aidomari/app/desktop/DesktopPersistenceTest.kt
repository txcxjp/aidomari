package com.aidomari.app.desktop

import app.cash.turbine.test
import com.aidomari.app.core.ai.GeminiConfig
import com.aidomari.app.core.server.LocalIngestServer
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.PastDayDeckType
import com.aidomari.app.domain.model.StudyDirection
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.model.article.ArticleStatus
import com.aidomari.app.ui.theme.AppColorTheme
import com.aidomari.app.ui.theme.ThemeConfig
import com.aidomari.app.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URL
import java.util.UUID
import java.util.prefs.Preferences

class DesktopPersistenceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var dbFile: File
    private lateinit var db: DesktopDatabase
    private lateinit var wordsRepo: DesktopWordsRepository
    private lateinit var articleRepo: DesktopSourceArticleRepository

    @Before
    fun setup() {
        dbFile = File(tempFolder.root, "test_aidomari.db")
        db = DesktopDatabase(databaseFile = dbFile, seedSampleDataIfEmpty = false)
        wordsRepo = DesktopWordsRepository(db)
        articleRepo = DesktopSourceArticleRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun desktopDatabase_seedsStarterSampleDataOnlyWhenEmpty() = runTest {
        val seededFile = File(tempFolder.root, "seeded.db")
        DesktopDatabase(databaseFile = seededFile, seedSampleDataIfEmpty = true).use { seededDb ->
            val repo = DesktopWordsRepository(seededDb)
            val subjects = repo.getAllSubjects().first()
            assertEquals(1, subjects.size)
            val decks = repo.getDecksBySubject(subjects.first().id).first()
            assertEquals(1, decks.size)
            val cards = repo.getCardsByDeckSync(decks.first().id)
            assertEquals(5, cards.size)
        }

        // Reopening existing DB does not duplicate sample rows
        DesktopDatabase(databaseFile = seededFile, seedSampleDataIfEmpty = true).use { reopened ->
            val repo = DesktopWordsRepository(reopened)
            assertEquals(1, repo.getAllSubjects().first().size)
        }
    }

    @Test
    fun wordsRepository_crudAndCascadeDeleteAndStudyLogs() = runTest {
        val subject = Subject(id = "sub-1", name = "英語", ttsLanguage = "en-US", colorTheme = "INDIGO")
        wordsRepo.saveSubject(subject)

        val deck = Deck(
            id = "deck-1",
            subjectId = "sub-1",
            name = "基本単語",
            splitSize = 20,
            studyDirection = StudyDirection.DESCRIPTION_TO_TERM,
        )
        wordsRepo.saveDeck(deck)

        val cards = listOf(
            Card(id = "c-1", deckId = "deck-1", term = "apple", description = "りんご", isBookmarked = true, orderIndex = 0),
            Card(id = "c-2", deckId = "deck-1", term = "banana", description = "バナナ", isBookmarked = false, orderIndex = 1),
        )
        wordsRepo.saveCards(cards)

        // Updating subject must NOT cascade-delete child decks or cards (ON CONFLICT DO UPDATE check)
        wordsRepo.saveSubject(subject.copy(name = "英語・改"))
        assertEquals("英語・改", wordsRepo.getSubjectById("sub-1")?.name)
        assertEquals(StudyDirection.DESCRIPTION_TO_TERM, wordsRepo.getDeckById("deck-1")?.studyDirection)
        assertEquals(2, wordsRepo.getCardsByDeckSync("deck-1").size)

        // Search & bookmark & snooze
        val searchResults = wordsRepo.searchCardsInSubject("sub-1", "りんご")
        assertEquals(1, searchResults.size)
        assertEquals("c-1", searchResults.first().id)

        val snoozed = wordsRepo.snoozeCard("c-2")
        assertNotNull(snoozed?.snoozedUntil)
        assertEquals(1, snoozed?.snoozeLevel)
        val unsnoozed = wordsRepo.unsnoozeCard("c-2")
        assertNull(unsnoozed?.snoozedUntil)
        assertEquals(0, unsnoozed?.snoozeLevel)

        // Session & logs
        val session = StudySession(
            deckId = "deck-1",
            partIndex = 0,
            currentCardIndex = 1,
            queueCardIds = listOf("c-2"),
            completedPartIndices = setOf(0),
        )
        wordsRepo.saveSession(session)
        assertEquals(setOf(0), wordsRepo.getSession("deck-1")?.completedPartIndices)

        wordsRepo.recordStudyLog("c-1", isCorrect = true)
        assertEquals(1, wordsRepo.getLatestLogsForDeckSync("deck-1").size)
        assertEquals(1, wordsRepo.getLearnedCardsByDeckSync("deck-1").size)
        val pastGroups = wordsRepo.getPastDayCardsBySubject("sub-1").first()
        assertFalse(pastGroups[PastDayDeckType.YESTERDAY]?.cards?.isNotEmpty() == true)

        // Deleting subject cascades to decks, cards, sessions, and logs
        wordsRepo.deleteSubject("sub-1")
        assertNull(wordsRepo.getDeckById("deck-1"))
        assertEquals(0, wordsRepo.getCardsByDeckSync("deck-1").size)
        assertNull(wordsRepo.getSession("deck-1"))
        assertEquals(0, wordsRepo.getAllStudyLogsSync().size)
    }

    @Test
    fun sourceArticleRepository_ingestCleanupAndLocalIngestServerIntegration() = runTest {
        // PrivacyFilter rejection
        val excluded = articleRepo.ingestArticle(
            url = "https://www.google.com/search?q=test",
            title = "Search",
            content = "body",
        )
        assertTrue(excluded.isFailure)

        // Normal ingest + LocalIngestServer end-to-end
        val actualPort = ServerSocket(0).use { it.localPort }
        val server = LocalIngestServer(articleRepo, port = actualPort)
        val serverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        server.start(serverScope)
        try {
            withContext(Dispatchers.IO) {
                var attempts = 0
                while (!server.isRunning && attempts < 100) {
                    Thread.sleep(20)
                    attempts++
                }
                assertTrue("Server failed to bind port $actualPort", server.isRunning)
            }

            articleRepo.getPendingArticles().test {
                assertEquals(0, awaitItem().size)

                val responseCode = withContext(Dispatchers.IO) {
                    val conn = (URL("http://127.0.0.1:$actualPort/api/ingest").openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    }
                    val payload = """{"url":"https://example.com/kotlin","title":"Kotlin Notes","content":"Multiplatform shared UI article content."}"""
                    conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
                    conn.responseCode
                }
                assertEquals(200, responseCode)

                val pending = awaitItem()
                assertEquals(1, pending.size)
                assertEquals("Kotlin Notes", pending.first().title)

                val id = pending.first().id
                articleRepo.updateArticleStatus(id, ArticleStatus.GENERATED, cardCount = 4)
                val updatedPending = awaitItem()
                assertEquals(0, updatedPending.size)
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            server.stop()
            serverScope.cancel()
        }
    }

    @Test
    fun configStoresAndAppDirs_workDeterministically() {
        val nodeName = "com/aidomari/test/${UUID.randomUUID()}"
        val prefs = Preferences.userRoot().node(nodeName)
        try {
            val themeStore = DesktopThemeConfigStore(prefs)
            themeStore.setThemeConfig(ThemeConfig(mode = ThemeMode.DARK, colorTheme = AppColorTheme.FOREST))
            val reloadedTheme = DesktopThemeConfigStore(prefs)
            assertEquals(ThemeMode.DARK, reloadedTheme.themeConfig.value.mode)
            assertEquals(AppColorTheme.FOREST, reloadedTheme.themeConfig.value.colorTheme)

            val geminiStore = DesktopGeminiConfigStore(prefs)
            geminiStore.updateConfig(GeminiConfig(apiKey = " test-key ", modelName = "gemini-2.5-flash", temperature = 0.2f))
            val reloadedGemini = DesktopGeminiConfigStore(prefs)
            assertEquals("test-key", reloadedGemini.config.value.apiKey)
            assertEquals("gemini-2.5-flash", reloadedGemini.config.value.modelName)
        } finally {
            prefs.removeNode()
        }

        val winDir = DesktopAppDirs.resolveAppDataDir(
            osName = "Windows 11",
            userHome = tempFolder.root.absolutePath,
            appDataEnv = File(tempFolder.root, "Roaming").absolutePath,
            xdgDataHomeEnv = null,
        )
        assertTrue(winDir.absolutePath.endsWith("${File.separator}Roaming${File.separator}Aidomari"))

        assertEquals("ja-JP", DesktopTtsSpeaker.resolveLanguage("auto", "相泊テスト"))
        assertEquals("en-US", DesktopTtsSpeaker.resolveLanguage("auto", "Hello world"))
    }
}
