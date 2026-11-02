package com.aidomari.app.ui.study

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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
