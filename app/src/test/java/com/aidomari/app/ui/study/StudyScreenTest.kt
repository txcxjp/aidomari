package com.aidomari.app.ui.study

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StudyScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeStudyWordsRepository
    private lateinit var viewModel: StudyViewModel

    private val deckWithAutoContinue = Deck(
        id = "deck-auto",
        subjectId = "sub-1",
        name = "Auto Continue Deck",
        splitSize = 1,
        autoContinue = true,
        shuffleMode = false,
    )

    private val deckWithoutAutoContinue = Deck(
        id = "deck-manual",
        subjectId = "sub-1",
        name = "Manual Deck",
        splitSize = 1,
        autoContinue = false,
        shuffleMode = false,
    )

    private val cards = listOf(
        Card(id = "c1", deckId = "deck-auto", front = "Card 1", back = "意味1", orderIndex = 0),
        Card(id = "c2", deckId = "deck-auto", front = "Card 2", back = "意味2", orderIndex = 1),
    )

    @Before
    fun setup() {
        fakeRepository = FakeStudyWordsRepository()
        viewModel = StudyViewModel(fakeRepository)
    }

    @Test
    fun studyScreen_autoContinueEnabled_automaticallyNavigatesToNextPart() {
        fakeRepository.decks.add(deckWithAutoContinue)
        fakeRepository.cards.addAll(cards)

        var navigatedPartIndex: Int? = null

        composeTestRule.setContent {
            StudyScreen(
                deckId = "deck-auto",
                partIndex = 0,
                viewModel = viewModel,
                onNavigateBack = {},
                onNavigateToPart = { navigatedPartIndex = it },
            )
        }

        composeTestRule.waitForIdle()

        // Verify card 1 is displayed
        composeTestRule.onNodeWithText("Card 1").assertIsDisplayed()

        // Complete the only card in part 0
        composeTestRule.runOnUiThread {
            viewModel.markDone()
        }

        composeTestRule.waitForIdle()

        // Should automatically advance to part 1 without showing completion dialog
        assertEquals(1, navigatedPartIndex)
    }

    @Test
    fun studyScreen_autoContinueDisabled_showsCompletionDialog() {
        val manualCards = listOf(
            Card(id = "m1", deckId = "deck-manual", front = "Manual 1", back = "意味1", orderIndex = 0),
            Card(id = "m2", deckId = "deck-manual", front = "Manual 2", back = "意味2", orderIndex = 1),
        )
        fakeRepository.decks.add(deckWithoutAutoContinue)
        fakeRepository.cards.addAll(manualCards)

        var navigatedPartIndex: Int? = null

        composeTestRule.setContent {
            StudyScreen(
                deckId = "deck-manual",
                partIndex = 0,
                viewModel = viewModel,
                onNavigateBack = {},
                onNavigateToPart = { navigatedPartIndex = it },
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.runOnUiThread {
            viewModel.markDone()
        }

        composeTestRule.waitForIdle()

        // Completion dialog must be displayed
        composeTestRule.onNodeWithText("学習完了！").assertIsDisplayed()
        assertNull(navigatedPartIndex)

        // Tapping "次のパートへ進む" navigates to next part
        composeTestRule.onNodeWithText("次のパートへ進む").performClick()
        assertEquals(1, navigatedPartIndex)
    }

    @Test
    fun studyScreen_autoContinueEnabled_lastPartShowsCompletionDialog() {
        val singleCardDeck = deckWithAutoContinue.copy(id = "deck-single")
        val singleCard = listOf(
            Card(id = "s1", deckId = "deck-single", front = "Single Card", back = "意味", orderIndex = 0),
        )
        fakeRepository.decks.add(singleCardDeck)
        fakeRepository.cards.addAll(singleCard)

        var navigatedPartIndex: Int? = null

        composeTestRule.setContent {
            StudyScreen(
                deckId = "deck-single",
                partIndex = 0,
                viewModel = viewModel,
                onNavigateBack = {},
                onNavigateToPart = { navigatedPartIndex = it },
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.runOnUiThread {
            viewModel.markDone()
        }

        composeTestRule.waitForIdle()

        // Since it's the last part (no next part), completion dialog is shown
        composeTestRule.onNodeWithText("学習完了！").assertIsDisplayed()
        assertNull(navigatedPartIndex)
    }

    @Test
    fun studyScreen_randomMode_showsContinueRandomStudyButtonAndContinues() {
        val manualCards = listOf(
            Card(id = "m1", deckId = "deck-manual", front = "Manual 1", back = "意味1", orderIndex = 0),
            Card(id = "m2", deckId = "deck-manual", front = "Manual 2", back = "意味2", orderIndex = 1),
        )
        fakeRepository.decks.add(deckWithoutAutoContinue)
        fakeRepository.cards.addAll(manualCards)

        composeTestRule.setContent {
            StudyScreen(
                deckId = "random_deck_1_deck-manual",
                partIndex = 0,
                viewModel = viewModel,
                onNavigateBack = {},
                onNavigateToPart = {},
            )
        }

        composeTestRule.waitForIdle()

        // Complete the single random question
        composeTestRule.runOnUiThread {
            viewModel.markDone()
        }

        composeTestRule.waitForIdle()

        // Random completion dialog should be displayed with "続けて出題する"
        composeTestRule.onNodeWithText("ランダム出題完了！").assertIsDisplayed()
        composeTestRule.onNodeWithText("続けて出題する").assertIsDisplayed()
        composeTestRule.onNodeWithText("このパートをもう一度学習").assertIsDisplayed()
        composeTestRule.onNodeWithText("デッキ一覧に戻る").assertIsDisplayed()

        // Click "続けて出題する"
        composeTestRule.onNodeWithText("続けて出題する").performClick()
        composeTestRule.waitForIdle()

        // Dialog should be dismissed and study resumed
        assertFalse(viewModel.uiState.value.isPartCompleted)
        assertEquals(1, viewModel.uiState.value.totalInPart)
    }

    @Test
    fun studyScreen_searchAction_opensSearchDialog() {
        fakeRepository.decks.add(deckWithAutoContinue)
        fakeRepository.cards.addAll(cards)

        composeTestRule.setContent {
            StudyScreen(
                deckId = "deck-auto",
                partIndex = 0,
                viewModel = viewModel,
                onNavigateBack = {},
                onNavigateToPart = {},
            )
        }

        composeTestRule.waitForIdle()

        // Click search icon in top bar
        composeTestRule.onNodeWithContentDescription("Search Words").performClick()

        // Verify search dialog appears
        composeTestRule.onNodeWithText("単語検索").assertIsDisplayed()
        composeTestRule.onNodeWithText("教科内の単語を検索").assertIsDisplayed()
    }

    @Test
    fun studyScreen_searchSelectCard_showsDetails_andReturningPreservesSearch() {
        fakeRepository.decks.add(deckWithAutoContinue)
        fakeRepository.cards.addAll(cards)

        composeTestRule.setContent {
            StudyScreen(
                deckId = "deck-auto",
                partIndex = 0,
                viewModel = viewModel,
                onNavigateBack = {},
                onNavigateToPart = {},
            )
        }

        composeTestRule.waitForIdle()

        // 1. Open search dialog
        composeTestRule.onNodeWithContentDescription("Search Words").performClick()
        composeTestRule.onNodeWithText("単語検索").assertIsDisplayed()

        // 2. Perform search
        composeTestRule.runOnUiThread {
            viewModel.searchCards("Card")
        }
        composeTestRule.waitForIdle()

        // 3. Search results are displayed
        composeTestRule.onNodeWithText("2件の単語が見つかりました").assertIsDisplayed()

        // 4. Click search result "Card 2"
        composeTestRule.onNodeWithText("Card 2").performClick()
        composeTestRule.waitForIdle()

        // 5. Keyword details bottom sheet is displayed (contains Close button)
        composeTestRule.onNodeWithContentDescription("Close").assertIsDisplayed()

        // 6. Dismiss the detail sheet by clicking Close
        composeTestRule.onNodeWithContentDescription("Close").performClick()
        composeTestRule.waitForIdle()

        // 7. Verify search dialog is still displayed with results preserved!
        composeTestRule.onNodeWithText("単語検索").assertIsDisplayed()
        composeTestRule.onNodeWithText("2件の単語が見つかりました").assertIsDisplayed()

        // 8. Dismiss search dialog by clicking Close Search
        composeTestRule.onNodeWithContentDescription("Close Search").performClick()
        composeTestRule.waitForIdle()

        // 9. Search dialog is closed and search state is cleared
        composeTestRule.onNodeWithText("単語検索").assertDoesNotExist()
        assertEquals("", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun studyScreen_deleteCardAction_showsConfirmationDialog_and_deletes() {
        val manualDeck = Deck(
            id = "deck-test-del",
            subjectId = "sub-1",
            name = "Test Delete Deck",
            splitSize = 10,
            autoContinue = false,
            shuffleMode = false,
        )
        val deleteCards = listOf(
            Card(id = "d1", deckId = "deck-test-del", front = "Delete Card 1", back = "意味1", orderIndex = 0),
            Card(id = "d2", deckId = "deck-test-del", front = "Delete Card 2", back = "意味2", orderIndex = 1),
        )
        fakeRepository.decks.add(manualDeck)
        fakeRepository.cards.addAll(deleteCards)

        composeTestRule.setContent {
            StudyScreen(
                deckId = "deck-test-del",
                partIndex = 0,
                viewModel = viewModel,
                onNavigateBack = {},
                onNavigateToPart = {},
            )
        }

        composeTestRule.waitForIdle()

        // Card 1 is displayed
        composeTestRule.onNodeWithText("Delete Card 1").assertIsDisplayed()

        // Open more options menu
        composeTestRule.onNodeWithContentDescription("More Options").performClick()

        // Click "カードを削除"
        composeTestRule.onNodeWithText("カードを削除").performClick()

        // Confirmation dialog is shown
        composeTestRule.onNodeWithText("「Delete Card 1」を削除しますか？\nこの操作は取り消せません。").assertIsDisplayed()

        // Confirm deletion
        composeTestRule.onNodeWithText("削除").performClick()

        composeTestRule.waitForIdle()

        // Card 1 is deleted from repo and screen advances to Card 2
        composeTestRule.onNodeWithText("Delete Card 2").assertIsDisplayed()
        assertFalse(fakeRepository.cards.any { it.id == "d1" })
    }

    @Test
    fun studyScreen_autoSpeak_normalMode_speaksFrontOnDisplay_and_backOnFlip() {
        val normalDeck = Deck(
            id = "deck-normal-speak",
            subjectId = "sub-1",
            name = "Normal Speak Deck",
            splitSize = 10,
            autoSpeak = true,
            reverseMode = false,
        )
        val normalCards = listOf(
            Card(id = "ns1", deckId = "deck-normal-speak", front = "apple", back = "りんご", orderIndex = 0),
        )
        fakeRepository.decks.add(normalDeck)
        fakeRepository.cards.addAll(normalCards)

        val spokenTexts = mutableListOf<String>()

        composeTestRule.setContent {
            StudyScreen(
                deckId = "deck-normal-speak",
                partIndex = 0,
                viewModel = viewModel,
                onNavigateBack = {},
                onNavigateToPart = {},
                onSpeakText = { spokenTexts.add(it) },
            )
        }

        composeTestRule.waitForIdle()

        // Card is displayed (オモテ面) -> speaks front
        assertEquals(listOf("apple"), spokenTexts)

        // Flip card to ウラ面
        composeTestRule.runOnUiThread {
            viewModel.flipCard()
        }
        composeTestRule.waitForIdle()

        // Card flipped (ウラ面) -> speaks back
        assertEquals(listOf("apple", "りんご"), spokenTexts)
    }

    @Test
    fun studyScreen_autoSpeak_reverseMode_speaksBackOnDisplay_and_frontOnFlip() {
        val reverseDeck = Deck(
            id = "deck-reverse-speak",
            subjectId = "sub-1",
            name = "Reverse Speak Deck",
            splitSize = 10,
            autoSpeak = true,
            reverseMode = true,
        )
        val reverseCards = listOf(
            Card(id = "rs1", deckId = "deck-reverse-speak", front = "journey", back = "旅、旅行", orderIndex = 0),
        )
        fakeRepository.decks.add(reverseDeck)
        fakeRepository.cards.addAll(reverseCards)

        val spokenTexts = mutableListOf<String>()

        composeTestRule.setContent {
            StudyScreen(
                deckId = "deck-reverse-speak",
                partIndex = 0,
                viewModel = viewModel,
                onNavigateBack = {},
                onNavigateToPart = {},
                onSpeakText = { spokenTexts.add(it) },
            )
        }

        composeTestRule.waitForIdle()

        // Reverse mode: Card is displayed (オモテ面: 設問) -> speaks back (Japanese meaning)
        assertEquals(listOf("旅、旅行"), spokenTexts)

        // Flip card to ウラ面 (解答)
        composeTestRule.runOnUiThread {
            viewModel.flipCard()
        }
        composeTestRule.waitForIdle()

        // Card flipped (ウラ面: 解答) -> speaks front (target English word)
        assertEquals(listOf("旅、旅行", "journey"), spokenTexts)
    }

    @Test
    fun studyScreen_lastCardRetried_remainsDisplayedAndReSpeaksFront() {
        val singleCardDeck = Deck(
            id = "deck-retry-last",
            subjectId = "sub-1",
            name = "Retry Last Deck",
            splitSize = 10,
            autoSpeak = true,
            reverseMode = false,
        )
        val singleCard = listOf(
            Card(id = "rl1", deckId = "deck-retry-last", front = "Last Card", back = "最後の意味", orderIndex = 0),
        )
        fakeRepository.decks.add(singleCardDeck)
        fakeRepository.cards.addAll(singleCard)

        val spokenTexts = mutableListOf<String>()

        composeTestRule.setContent {
            StudyScreen(
                deckId = "deck-retry-last",
                partIndex = 0,
                viewModel = viewModel,
                onNavigateBack = {},
                onNavigateToPart = {},
                onSpeakText = { spokenTexts.add(it) },
            )
        }

        composeTestRule.waitForIdle()

        // 1. Initial display: front is shown and spoken
        composeTestRule.onNodeWithText("Last Card").assertIsDisplayed()
        assertEquals(listOf("Last Card"), spokenTexts)

        // 2. Flip to back face
        composeTestRule.runOnUiThread {
            viewModel.flipCard()
        }
        composeTestRule.waitForIdle()
        assertEquals(listOf("Last Card", "最後の意味"), spokenTexts)

        // 3. Mark retry (last remaining card is marked retry -> re-rendered as front of new presentation)
        composeTestRule.runOnUiThread {
            viewModel.markRetry()
        }
        composeTestRule.waitForIdle()

        // 4. Card should still be displayed at front face, not disappeared!
        composeTestRule.onNodeWithText("Last Card").assertIsDisplayed()
        assertFalse(viewModel.uiState.value.isPartCompleted)
        assertFalse(viewModel.uiState.value.isFlipped)

        // Auto-speak re-speaks the front face of the card
        assertEquals(listOf("Last Card", "最後の意味", "Last Card"), spokenTexts)
    }

    private class FakeStudyWordsRepository : WordsRepository {
        val subjects = mutableListOf<Subject>()
        val decks = mutableListOf<Deck>()
        val cards = mutableListOf<Card>()

        override fun getAllSubjects(): Flow<List<Subject>> = flowOf(subjects)
        override suspend fun getSubjectById(id: String): Subject? = subjects.find { it.id == id }
        override suspend fun saveSubject(subject: Subject) {
            subjects.add(subject)
        }
        override suspend fun deleteSubject(id: String) {
            subjects.removeIf { it.id == id }
        }

        override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> =
            flowOf(decks.filter { it.subjectId == subjectId })
        override suspend fun getDeckById(deckId: String): Deck? = decks.find { it.id == deckId }
        override suspend fun saveDeck(deck: Deck) {
            decks.removeIf { it.id == deck.id }
            decks.add(deck)
        }
        override suspend fun deleteDeck(deckId: String) {
            decks.removeIf { it.id == deckId }
        }

        override fun getCardsByDeck(deckId: String): Flow<List<Card>> =
            flowOf(cards.filter { it.deckId == deckId })
        override suspend fun getCardsByDeckSync(deckId: String): List<Card> =
            cards.filter { it.deckId == deckId }
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
            if (idx >= 0) {
                cards[idx] = cards[idx].copy(isBookmarked = isBookmarked)
            }
        }
        override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> =
            flowOf(cards.filter { it.isBookmarked })
        override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> = emptyList()
        override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? = null
        override suspend fun getAllCardsInSubject(subjectId: String): List<Card> = cards.toList()

        override suspend fun getSession(deckId: String): StudySession? = null
        override fun getSessionFlow(deckId: String): Flow<StudySession?> = flowOf(null)
        override suspend fun saveSession(session: StudySession) {}
        override suspend fun deleteSession(deckId: String) {}
    }
}
