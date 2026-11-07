package com.aidomari.app.ui.card

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
import com.aidomari.app.domain.util.CardSnoozeCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeCardRepository
    private lateinit var viewModel: CardListViewModel

    private val testDeck = Deck(
        id = "deck-1",
        subjectId = "sub-1",
        name = "TOEIC頻出",
        splitSize = 50,
    )

    private val card1 = Card(
        id = "c1",
        deckId = "deck-1",
        front = "apple",
        back = "りんご",
        userQuestion = "赤い果物は？",
        userSupplement = "青森名産",
        isBookmarked = false,
    )

    private val card2 = Card(
        id = "c2",
        deckId = "deck-1",
        front = "banana",
        back = "バナナ",
        isBookmarked = true,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCardRepository(
            deck = testDeck,
            initialCards = listOf(card1, card2),
        )
        viewModel = CardListViewModel(
            deckId = "deck-1",
            repository = fakeRepository,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadData_populatesCardsAndDeck() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.deck)
        assertEquals("TOEIC頻出", state.deck?.name)
        assertEquals(2, state.cards.size)
        assertEquals(2, state.filteredCards.size)
    }

    @Test
    fun onSearchQueryChanged_filtersCardsByFrontBackAndNotes() = runTest {
        advanceUntilIdle()

        // Filter by front
        viewModel.onSearchQueryChanged("apple")
        assertEquals(1, viewModel.uiState.value.filteredCards.size)
        assertEquals("apple", viewModel.uiState.value.filteredCards[0].front)

        // Filter by meaning
        viewModel.onSearchQueryChanged("バナナ")
        assertEquals(1, viewModel.uiState.value.filteredCards.size)
        assertEquals("banana", viewModel.uiState.value.filteredCards[0].front)

        // Filter by supplement note
        viewModel.onSearchQueryChanged("青森")
        assertEquals(1, viewModel.uiState.value.filteredCards.size)
        assertEquals("apple", viewModel.uiState.value.filteredCards[0].front)

        // Clear filter
        viewModel.onSearchQueryChanged("")
        assertEquals(2, viewModel.uiState.value.filteredCards.size)
    }

    @Test
    fun toggleBookmark_updatesCardAndCallsRepository() = runTest {
        advanceUntilIdle()

        viewModel.toggleBookmark(card1)
        advanceUntilIdle()

        val updatedCard1 = viewModel.uiState.value.cards.find { it.id == "c1" }
        assertNotNull(updatedCard1)
        assertTrue(updatedCard1!!.isBookmarked)

        val repoCard = fakeRepository.cards.find { it.id == "c1" }
        assertNotNull(repoCard)
        assertTrue(repoCard!!.isBookmarked)
    }

    @Test
    fun saveCard_newCard_addsToStateAndRepository() = runTest {
        advanceUntilIdle()

        viewModel.saveCard(
            cardId = null,
            front = "cherry",
            back = "さくらんぼ",
            userQuestion = "山形名産は？",
            userSupplement = "佐藤錦",
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.cards.size)
        val addedCard = state.cards.find { it.front == "cherry" }
        assertNotNull(addedCard)
        assertEquals("さくらんぼ", addedCard?.back)
        assertEquals("山形名産は？", addedCard?.userQuestion)
        assertEquals("佐藤錦", addedCard?.userSupplement)

        val repoCard = fakeRepository.cards.find { it.front == "cherry" }
        assertNotNull(repoCard)
    }

    @Test
    fun saveCard_existingCard_updatesInStateAndRepository() = runTest {
        advanceUntilIdle()

        viewModel.saveCard(
            cardId = "c1",
            front = "apple (updated)",
            back = "リンゴ（更新）",
            userQuestion = "Q",
            userSupplement = "Memo",
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.cards.size)
        val updatedCard = state.cards.find { it.id == "c1" }
        assertEquals("apple (updated)", updatedCard?.front)
        assertEquals("リンゴ（更新）", updatedCard?.back)

        val repoCard = fakeRepository.cards.find { it.id == "c1" }
        assertEquals("apple (updated)", repoCard?.front)
    }

    @Test
    fun deleteCard_removesFromStateAndRepository() = runTest {
        advanceUntilIdle()

        viewModel.deleteCard("c1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.cards.size)
        assertEquals("banana", state.cards[0].front)

        assertFalse(fakeRepository.cards.any { it.id == "c1" })
    }

    @Test
    fun updateCardNotes_updatesNotesInStateAndRepository() = runTest {
        advanceUntilIdle()

        viewModel.updateCardNotes(
            cardId = "c1",
            userQuestion = "更新された疑問点",
            userSupplement = "更新されたメモ",
        )
        advanceUntilIdle()

        val updatedCard = viewModel.uiState.value.cards.find { it.id == "c1" }
        assertEquals("更新された疑問点", updatedCard?.userQuestion)
        assertEquals("更新されたメモ", updatedCard?.userSupplement)

        val repoCard = fakeRepository.cards.find { it.id == "c1" }
        assertEquals("更新された疑問点", repoCard?.userQuestion)
        assertEquals("更新されたメモ", repoCard?.userSupplement)
    }

    @Test
    fun snoozeCard_updatesSnoozeInStateAndRepository() = runTest {
        advanceUntilIdle()

        var notifiedLabel = ""
        viewModel.snoozeCard(card1) { label -> notifiedLabel = label }
        advanceUntilIdle()

        val stateCard = viewModel.uiState.value.cards.find { it.id == "c1" }
        assertNotNull(stateCard)
        assertTrue(stateCard!!.isSnoozed)
        assertEquals(1, stateCard.snoozeLevel)
        assertEquals("明日まで", notifiedLabel)

        val repoCard = fakeRepository.cards.find { it.id == "c1" }
        assertNotNull(repoCard)
        assertTrue(repoCard!!.isSnoozed)
        assertEquals(1, repoCard.snoozeLevel)
    }

    @Test
    fun unsnoozeCard_clearsSnoozeInStateAndRepository() = runTest {
        advanceUntilIdle()

        viewModel.snoozeCard(card1)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.cards.find { it.id == "c1" }!!.isSnoozed)

        var unSnoozedNotified = false
        val snoozedCard = viewModel.uiState.value.cards.find { it.id == "c1" }!!
        viewModel.unsnoozeCard(snoozedCard) { unSnoozedNotified = true }
        advanceUntilIdle()

        assertTrue(unSnoozedNotified)
        val stateCard = viewModel.uiState.value.cards.find { it.id == "c1" }
        assertNotNull(stateCard)
        assertFalse(stateCard!!.isSnoozed)
        assertEquals(0, stateCard.snoozeLevel)

        val repoCard = fakeRepository.cards.find { it.id == "c1" }
        assertNotNull(repoCard)
        assertFalse(repoCard!!.isSnoozed)
        assertEquals(0, repoCard.snoozeLevel)
    }

    private class FakeCardRepository(
        private val deck: Deck,
        initialCards: List<Card> = emptyList(),
    ) : WordsRepository {
        val cards = initialCards.toMutableList()

        override suspend fun snoozeCard(cardId: String): Card? {
            val idx = cards.indexOfFirst { it.id == cardId }
            if (idx == -1) return null
            val card = cards[idx]
            val next = CardSnoozeCalculator.calculateNextSnooze(card.snoozeLevel)
            val updated = card.copy(
                snoozedUntil = next.snoozedUntil,
                snoozeLevel = next.level,
            )
            cards[idx] = updated
            return updated
        }

        override suspend fun unsnoozeCard(cardId: String): Card? {
            val idx = cards.indexOfFirst { it.id == cardId }
            if (idx == -1) return null
            val updated = cards[idx].copy(
                snoozedUntil = null,
                snoozeLevel = 0,
            )
            cards[idx] = updated
            return updated
        }

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
