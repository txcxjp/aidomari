package com.aidomari.app.ui.card

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CardListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeTestCardRepository
    private lateinit var viewModel: CardListViewModel

    private val deck = Deck(
        id = "deck-1",
        subjectId = "sub-1",
        name = "英検準1級",
        splitSize = 50,
    )

    private val initialCard = Card(
        id = "card-1",
        deckId = "deck-1",
        front = "Abundant",
        back = "豊富な",
        userQuestion = "同義語は？",
        userSupplement = "plentiful",
        isBookmarked = false,
    )

    @Before
    fun setup() {
        fakeRepository = FakeTestCardRepository(deck, mutableListOf(initialCard))
        viewModel = CardListViewModel("deck-1", fakeRepository)
    }

    @Test
    fun displaysDeckNameAndCardContent() {
        composeTestRule.setContent {
            CardListScreen(
                viewModel = viewModel,
                onBack = {},
            )
        }

        composeTestRule.onNodeWithText("カード管理").assertIsDisplayed()
        composeTestRule.onNodeWithText("英検準1級 (1枚)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Abundant").assertIsDisplayed()
        composeTestRule.onNodeWithText("豊富な").assertIsDisplayed()
        composeTestRule.onNodeWithText("Q: 同義語は？").assertIsDisplayed()
        composeTestRule.onNodeWithText("メモ: plentiful").assertIsDisplayed()
    }

    @Test
    fun addCard_dialogOpensAndSavesNewCard() {
        composeTestRule.setContent {
            CardListScreen(
                viewModel = viewModel,
                onBack = {},
            )
        }

        // Tap Add FAB
        composeTestRule.onNodeWithTag("AddCardFab").performClick()

        // Verify Dialog
        composeTestRule.onNodeWithTag("CardEditDialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("新規単語を追加").assertIsDisplayed()

        // Enter front and back
        composeTestRule.onNodeWithTag("CardEditFrontTextField").performTextInput("Diligent")
        composeTestRule.onNodeWithTag("CardEditBackTextField").performTextInput("勤勉な")
        composeTestRule.onNodeWithTag("CardEditQuestionTextField").performTextInput("反対語は？")
        composeTestRule.onNodeWithTag("CardEditSupplementTextField").performTextInput("lazy")

        // Save
        composeTestRule.onNodeWithTag("CardEditSaveButton").performClick()

        // Verify new card is displayed
        composeTestRule.onNodeWithText("Diligent").assertIsDisplayed()
        composeTestRule.onNodeWithText("勤勉な").assertIsDisplayed()
        assertEquals(2, fakeRepository.cards.size)
    }

    @Test
    fun editCard_dialogOpensAndUpdatesCard() {
        composeTestRule.setContent {
            CardListScreen(
                viewModel = viewModel,
                onBack = {},
            )
        }

        // Tap Edit button on card-1
        composeTestRule.onNodeWithTag("EditCardButton_card-1").performClick()

        // Verify Dialog is prefilled
        composeTestRule.onNodeWithText("単語カードを編集").assertIsDisplayed()

        // Update back text
        composeTestRule.onNodeWithTag("CardEditBackTextField").performTextClearance()
        composeTestRule.onNodeWithTag("CardEditBackTextField").performTextInput("ありあまるほどの")

        // Save
        composeTestRule.onNodeWithTag("CardEditSaveButton").performClick()

        // Verify updated text in list
        composeTestRule.onNodeWithText("ありあまるほどの").assertIsDisplayed()
        val updated = fakeRepository.cards.find { it.id == "card-1" }
        assertEquals("ありあまるほどの", updated?.back)
    }

    @Test
    fun deleteCard_viaKeywordBottomSheet_deletesCardSuccessfully() {
        composeTestRule.setContent {
            CardListScreen(
                viewModel = viewModel,
                onBack = {},
            )
        }

        // Direct delete button must NOT exist on card list row
        composeTestRule.onNodeWithTag("DeleteCardButton_card-1").assertDoesNotExist()

        // Tap card row to open KeywordBottomSheet
        composeTestRule.onNodeWithText("Abundant").performClick()
        composeTestRule.waitForIdle()

        // Open More Options in KeywordBottomSheet and click "カードを削除"
        composeTestRule.onNodeWithContentDescription("More Options").performClick()
        composeTestRule.onNodeWithText("カードを削除").performClick()

        // Confirm Delete inside KeywordBottomSheet's confirmation dialog
        composeTestRule.onNodeWithText("削除").performClick()
        composeTestRule.waitForIdle()

        // Verify card is removed and empty message displayed
        composeTestRule.onNodeWithText("カードがまだ登録されていません").assertIsDisplayed()
        assertTrue(fakeRepository.cards.isEmpty())
    }

    @Test
    fun toggleBookmark_updatesBookmarkState() {
        composeTestRule.setContent {
            CardListScreen(
                viewModel = viewModel,
                onBack = {},
            )
        }

        assertFalse(initialCard.isBookmarked)

        // Tap Bookmark button
        composeTestRule.onNodeWithTag("BookmarkButton_card-1").performClick()

        val updated = fakeRepository.cards.find { it.id == "card-1" }
        assertTrue(updated!!.isBookmarked)
    }

    @Test
    fun speakButton_invokesOnSpeakCallback() {
        var spokenText = ""
        composeTestRule.setContent {
            CardListScreen(
                viewModel = viewModel,
                onBack = {},
                onSpeakText = { spokenText = it },
            )
        }

        composeTestRule.onNodeWithContentDescription("発音").performClick()
        assertEquals("Abundant", spokenText)
    }

    private class FakeTestCardRepository(
        private val deck: Deck,
        val cards: MutableList<Card> = mutableListOf(),
    ) : WordsRepository {
        override fun getAllSubjects(): Flow<List<Subject>> = flowOf(emptyList())
        override suspend fun getSubjectById(subjectId: String): Subject? = null
        override suspend fun saveSubject(subject: Subject) {
        }
        override suspend fun deleteSubject(subjectId: String) {
        }
        override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> = flowOf(listOf(deck))
        override suspend fun getDeckById(deckId: String): Deck? = if (deckId == deck.id) deck else null
        override suspend fun saveDeck(deck: Deck) {
        }
        override suspend fun deleteDeck(deckId: String) {
        }
        override fun getCardsByDeck(deckId: String): Flow<List<Card>> = flowOf(cards)
        override suspend fun getCardsByDeckSync(deckId: String): List<Card> = cards.toList()
        override suspend fun getCardById(cardId: String): Card? = cards.find { it.id == cardId }
        override suspend fun saveCard(card: Card) {
            cards.removeIf { it.id == card.id }
            cards.add(card)
        }
        override suspend fun saveCards(cards: List<Card>) {
            this.cards.addAll(cards)
        }
        override suspend fun deleteCard(cardId: String) {
            cards.removeIf { it.id == cardId }
        }
        override suspend fun setBookmark(cardId: String, isBookmarked: Boolean) {
            val idx = cards.indexOfFirst { it.id == cardId }
            if (idx != -1) {
                cards[idx] = cards[idx].copy(isBookmarked = isBookmarked)
            }
        }
        override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> =
            flowOf(cards.filter { it.isBookmarked })
        override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> =
            cards.filter { it.front.contains(query) }
        override suspend fun getAllCardsInSubject(subjectId: String): List<Card> = cards.toList()
        override suspend fun getSession(deckId: String): StudySession? = null
        override suspend fun saveSession(session: StudySession) {
        }
        override suspend fun deleteSession(deckId: String) {
        }
        override suspend fun getOrCreateBookmarkDeck(subjectId: String): Deck = deck
        override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? =
            cards.find { it.front.equals(frontWord, ignoreCase = true) }
        override fun getSessionFlow(deckId: String): Flow<StudySession?> = flowOf(null)
    }
}
