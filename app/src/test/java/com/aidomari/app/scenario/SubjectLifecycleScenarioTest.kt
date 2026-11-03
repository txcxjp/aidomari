package com.aidomari.app.scenario

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aidomari.app.core.database.AidomariDao
import com.aidomari.app.core.database.AidomariDatabase
import com.aidomari.app.core.database.WordsRepositoryImpl
import com.aidomari.app.core.database.entity.CardEntity
import com.aidomari.app.core.database.entity.DeckEntity
import com.aidomari.app.core.database.entity.SubjectEntity
import com.aidomari.app.core.tts.TtsManager
import com.aidomari.app.ui.navigation.AidomariNavHost
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SubjectLifecycleScenarioTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var db: AidomariDatabase
    private lateinit var repository: WordsRepositoryImpl
    private lateinit var ttsManager: TtsManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AidomariDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WordsRepositoryImpl(db.aidomariDao())
        ttsManager = TtsManager(context = null, initTts = false)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun scenario_subjectArchiveAndUnarchive_preservesAllDecksAndCards() = runBlocking {
        // 1. Seed Real Database with Subject, Deck, and Cards
        val dao = db.aidomariDao()
        val subject = SubjectEntity(id = "sub-1", name = "実戦英語", ttsLanguage = "en-US")
        val deck = DeckEntity(id = "deck-1", subjectId = "sub-1", name = "基礎英単語500", splitSize = 50)
        val card1 = CardEntity(id = "card-1", deckId = "deck-1", front = "apple", back = "りんご")
        val card2 = CardEntity(id = "card-2", deckId = "deck-1", front = "banana", back = "バナナ")

        dao.insertSubject(subject)
        dao.insertDeck(deck)
        dao.insertCards(listOf(card1, card2))

        // 2. Launch Full Compose UI with Real Repository
        composeTestRule.setContent {
            AidomariNavHost(
                repository = repository,
                ttsManager = ttsManager,
            )
        }
        composeTestRule.waitForIdle()

        // 3. Verify Subject appears in Active Subject List
        composeTestRule.onNodeWithText("相泊").assertIsDisplayed()
        composeTestRule.onNodeWithText("実戦英語").assertIsDisplayed()

        // 4. Tap Subject to navigate to DeckListScreen and verify Deck is present
        composeTestRule.onNodeWithText("実戦英語").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("基礎英単語500").assertIsDisplayed()

        // 5. Navigate back to SubjectListScreen
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("相泊").assertIsDisplayed()

        // 6. Open Subject 3-dot menu and Archive Subject
        composeTestRule.onNodeWithTag("SubjectMoreOptionsButton_sub-1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("教科をアーカイブ").performClick()

        // 7. Wait for DB update to reflect in UI: Active list shows "アクティブな教科がありません"
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("ArchivedEmptyViewButton").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("アクティブな教科がありません").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ArchivedEmptyViewButton").assertIsDisplayed()

        // 8. Navigate to Archived View
        composeTestRule.onNodeWithTag("ArchivedEmptyViewButton").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("アーカイブ済み教科").assertIsDisplayed()
        composeTestRule.onNodeWithText("実戦英語").assertIsDisplayed()

        // 9. Open Archived Subject -> CRITICAL TEST: Deck must NOT be deleted by CASCADE!
        composeTestRule.onNodeWithText("実戦英語").performClick()
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("ArchivedSubjectBanner").fetchSemanticsNodes().isNotEmpty()
        }

        // Verify Archived banner is shown
        composeTestRule.onNodeWithTag("ArchivedSubjectBanner").assertIsDisplayed()
        composeTestRule.onNodeWithText("アーカイブされた教科").assertIsDisplayed()

        // Verify Deck is still intact (NOT deleted)
        composeTestRule.onNodeWithText("基礎英単語500").assertIsDisplayed()

        // 10. Unarchive from the banner button
        composeTestRule.onNodeWithText("元に戻す").performClick()
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("ArchivedSubjectBanner").fetchSemanticsNodes().isEmpty()
        }

        // 11. Navigate back to Home and verify Subject is back in active view
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText("実戦英語").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("相泊").assertIsDisplayed()
        composeTestRule.onNodeWithText("実戦英語").assertIsDisplayed()

        // 12. Final database integrity check
        val finalSubject = dao.getSubjectById("sub-1")
        val finalDecks = dao.getDecksBySubjectSync("sub-1")
        val finalCards = dao.getCardsByDeckSync("deck-1")

        assertNotNull(finalSubject)
        assertEquals(false, finalSubject?.isArchived)
        assertEquals(1, finalDecks.size)
        assertEquals(2, finalCards.size)
    }
}

// Helper synchronous query for final verification in test
private suspend fun AidomariDao.getDecksBySubjectSync(subjectId: String): List<DeckEntity> = getDecksBySubject(subjectId).first()
