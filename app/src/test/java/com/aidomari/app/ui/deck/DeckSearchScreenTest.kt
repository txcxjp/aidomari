package com.aidomari.app.ui.deck

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeckSearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeTestRepository
    private lateinit var viewModel: DeckViewModel

    @Before
    fun setup() {
        fakeRepository = FakeTestRepository()
        viewModel = DeckViewModel("sub-1", fakeRepository)
    }

    @Test
    fun searchMode_togglesAndShowsResults() {
        val card = Card(
            id = "c1",
            deckId = "d1",
            front = "Enthusiastic",
            back = "熱心な、熱狂的な",
            userSupplement = "Origin: Greek entheos",
        )
        fakeRepository.cardsList.add(card)

        var spokenText = ""

        composeTestRule.setContent {
            DeckListScreen(
                subjectName = "英単語",
                viewModel = viewModel,
                onBack = {},
                onPartClick = { _, _ -> },
                onSpeakText = { spokenText = it },
            )
        }

        // Initially in deck list
        composeTestRule.onNodeWithText("英単語").assertIsDisplayed()

        // Click search action icon
        composeTestRule.onNodeWithContentDescription("単語検索").performClick()

        // Placeholder is displayed
        composeTestRule.onNodeWithText("教科内の単語を検索").assertIsDisplayed()

        // Input search query
        composeTestRule.onNodeWithText("単語・意味・メモを検索...").performTextInput("Enthu")

        // Result is displayed
        composeTestRule.onNodeWithText("Enthusiastic").assertIsDisplayed()
        composeTestRule.onNodeWithText("熱心な、熱狂的な").assertIsDisplayed()
        composeTestRule.onNodeWithText("📝 メモあり").assertIsDisplayed()

        // Click speaker button on search result item
        composeTestRule.onNodeWithContentDescription("発音").performClick()
        assertEquals("Enthusiastic", spokenText)

        // Close search mode
        composeTestRule.onNodeWithContentDescription("検索を終了").performClick()
        composeTestRule.onNodeWithText("英単語").assertIsDisplayed()
    }

    @Test
    fun searchMode_toggleBookmark_updatesBookmark() {
        val card = Card(
            id = "c1",
            deckId = "d1",
            front = "Enthusiastic",
            back = "熱心な",
            isBookmarked = false,
        )
        fakeRepository.cardsList.add(card)

        composeTestRule.setContent {
            DeckListScreen(
                subjectName = "英単語",
                viewModel = viewModel,
                onBack = {},
                onPartClick = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithContentDescription("教科内を検索").performClick()
        composeTestRule.onNodeWithText("単語・意味・メモを検索...").performTextInput("Enthu")

        // Toggle bookmark from search result
        composeTestRule.onNodeWithTag("SearchBookmarkButton_c1").performClick()
        val updated = fakeRepository.cardsList.find { it.id == "c1" }
        assertTrue(updated!!.isBookmarked)
    }

    private class FakeTestRepository : WordsRepository {
        val cardsList = mutableListOf<Card>()
        val decksList = mutableListOf<Deck>()

        override fun getAllSubjects(): Flow<List<Subject>> = MutableStateFlow(emptyList())
        override suspend fun getSubjectById(id: String): Subject? = null
        override suspend fun saveSubject(subject: Subject) {
        }
        override suspend fun deleteSubject(id: String) {
        }

        override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> = MutableStateFlow(decksList)
        override suspend fun getDeckById(deckId: String): Deck? = decksList.find { it.id == deckId }
        override suspend fun getOrCreateBookmarkDeck(subjectId: String): Deck =
            Deck(id = "bookmark_$subjectId", subjectId = subjectId, name = "苦手単語")
        override suspend fun saveDeck(deck: Deck) {
            decksList.add(deck)
        }
        override suspend fun deleteDeck(deckId: String) {
            decksList.removeIf { it.id == deckId }
        }

        override fun getCardsByDeck(deckId: String): Flow<List<Card>> = MutableStateFlow(cardsList)
        override suspend fun getCardsByDeckSync(deckId: String): List<Card> = cardsList
        override suspend fun getCardById(cardId: String): Card? = cardsList.find { it.id == cardId }
        override suspend fun saveCard(card: Card) {
            cardsList.add(card)
        }
        override suspend fun saveCards(newCards: List<Card>) {
            cardsList.addAll(newCards)
        }
        override suspend fun deleteCard(cardId: String) {
            cardsList.removeIf { it.id == cardId }
        }

        override suspend fun setBookmark(cardId: String, isBookmarked: Boolean) {
            val idx = cardsList.indexOfFirst { it.id == cardId }
            if (idx != -1) {
                cardsList[idx] = cardsList[idx].copy(isBookmarked = isBookmarked)
            }
        }
        override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> = MutableStateFlow(emptyList())

        override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> =
            cardsList.filter {
                it.front.contains(query, ignoreCase = true) ||
                    it.back.contains(query, ignoreCase = true) ||
                    it.userSupplement.contains(query, ignoreCase = true)
            }

        override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? =
            cardsList.find { it.front == frontWord }

        override suspend fun getAllCardsInSubject(subjectId: String): List<Card> = cardsList

        override suspend fun getSession(deckId: String): StudySession? = null
        override fun getSessionFlow(deckId: String): Flow<StudySession?> = MutableStateFlow(null)
        override suspend fun saveSession(session: StudySession) {
        }
        override suspend fun deleteSession(deckId: String) {
        }
    }
}
