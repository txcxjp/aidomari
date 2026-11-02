package com.aidomari.app.ui.study

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeStudyWordsRepository
    private lateinit var viewModel: StudyViewModel

    private val testDeck = Deck(
        id = "deck-1",
        subjectId = "sub-1",
        name = "テスト単語帳",
        splitSize = 50,
        autoContinue = false,
        shuffleMode = false,
    )

    private val testCards = listOf(
        Card(id = "c1", deckId = "deck-1", front = "Apple", back = "りんご", orderIndex = 0),
        Card(id = "c2", deckId = "deck-1", front = "Banana", back = "バナナ", orderIndex = 1),
        Card(id = "c3", deckId = "deck-1", front = "Cherry", back = "さくらんぼ", orderIndex = 2),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeStudyWordsRepository().apply {
            decks.add(testDeck)
            cards.addAll(testCards)
        }
        viewModel = StudyViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadDeckAndPart_initializesStateCorrectly() = runTest {
        viewModel.loadDeckAndPart("deck-1", 0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("deck-1", state.deck?.id)
        assertEquals(0, state.partIndex)
        assertEquals("c1", state.currentCard?.id)
        assertEquals("c2", state.nextCard?.id)
        assertFalse(state.isFlipped)
        assertEquals(3, state.queueSize)
        assertEquals(3, state.totalInPart)
        assertEquals(0, state.completedCount)
        assertEquals(0, state.retryCount)
        assertFalse(state.isPartCompleted)
    }

    @Test
    fun loadDeckAndPart_withReverseMode_retainsReverseModeInDeck() = runTest {
        val reverseDeck = testDeck.copy(id = "deck-rev", reverseMode = true)
        fakeRepository.decks.add(reverseDeck)
        fakeRepository.cards.add(
            Card(id = "c-rev", deckId = "deck-rev", front = "Dog", back = "犬", orderIndex = 0),
        )

        viewModel.loadDeckAndPart("deck-rev", 0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(true, state.deck?.reverseMode)
        assertEquals("c-rev", state.currentCard?.id)
        assertFalse(state.isFlipped)
    }

    @Test
    fun loadDeckAndPart_bookmarkMode_loadsOnlyBookmarkedCards() = runTest {
        // testCards: c1, c2, c3. Bookmark only c2
        fakeRepository.setBookmark("c2", true)

        viewModel.loadDeckAndPart("bookmark_sub-1", 0)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("bookmark_sub-1", state.deck?.id)
        assertEquals("苦手単語（ブックマーク）", state.deck?.name)
        assertEquals("c2", state.currentCard?.id)
        assertEquals(1, state.queueSize)
        assertEquals(1, state.totalInPart)
    }

    @Test
    fun flipCard_togglesIsFlipped() = runTest {
        viewModel.loadDeckAndPart("deck-1", 0)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isFlipped)

        viewModel.flipCard()
        assertTrue(viewModel.uiState.value.isFlipped)

        viewModel.flipCard()
        assertFalse(viewModel.uiState.value.isFlipped)
    }

    @Test
    fun markDone_advancesCardAndIncrementsCompletedCount() = runTest {
        viewModel.loadDeckAndPart("deck-1", 0)
        advanceUntilIdle()

        viewModel.flipCard() // Flip to back
        viewModel.markDone()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("c2", state.currentCard?.id)
        assertEquals("c3", state.nextCard?.id)
        assertFalse(state.isFlipped)
        assertEquals(1, state.completedCount)
        assertEquals(2, state.queueSize)
        assertNotNull(fakeRepository.lastSavedSession)
        assertEquals(1, fakeRepository.lastSavedSession?.currentCardIndex)
    }

    @Test
    fun markRetry_addsCardToRetryQueueAndAdvances() = runTest {
        viewModel.loadDeckAndPart("deck-1", 0)
        advanceUntilIdle()

        viewModel.flipCard()
        viewModel.markRetry() // c1 -> retry
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("c2", state.currentCard?.id)
        assertEquals(1, state.retryCount)
        assertEquals(2, state.queueSize)

        viewModel.markDone() // c2 -> done
        advanceUntilIdle()

        viewModel.markDone() // c3 -> done
        advanceUntilIdle()

        // c2 and c3 done, active queue was empty so c1 rolled over to active queue!
        val stateAfterQueueRoll = viewModel.uiState.value
        assertEquals("c1", stateAfterQueueRoll.currentCard?.id)
        assertEquals(1, stateAfterQueueRoll.queueSize)
        assertEquals(0, stateAfterQueueRoll.retryCount)
        assertFalse(stateAfterQueueRoll.isPartCompleted)

        // Finish c1
        viewModel.markDone()
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertNull(finalState.currentCard)
        assertTrue(finalState.isPartCompleted)
        assertTrue(fakeRepository.lastSavedSession?.isCompleted == true)
    }

    @Test
    fun volumeKeys_handleProperStudyFlow() = runTest {
        viewModel.loadDeckAndPart("deck-1", 0)
        advanceUntilIdle()

        // Front face: Volume Up flips to back
        val handledUp = viewModel.onVolumeKey(isUp = true)
        assertTrue(handledUp)
        assertTrue(viewModel.uiState.value.isFlipped)

        // Back face: Volume Down marks Done
        val handledDown = viewModel.onVolumeKey(isUp = false)
        assertTrue(handledDown)
        assertEquals("c2", viewModel.uiState.value.currentCard?.id)
        assertFalse(viewModel.uiState.value.isFlipped)

        // Front face: Volume Down flips to back
        val handledDownFlip = viewModel.onVolumeKey(isUp = false)
        assertTrue(handledDownFlip)
        assertTrue(viewModel.uiState.value.isFlipped)

        // Back face: Volume Up marks Retry
        val handledUpRetry = viewModel.onVolumeKey(isUp = true)
        assertTrue(handledUpRetry)
        assertEquals("c3", viewModel.uiState.value.currentCard?.id)
        assertEquals(1, viewModel.uiState.value.retryCount)
    }

    @Test
    fun toggleBookmark_updatesCardStateAndRepository() = runTest {
        viewModel.loadDeckAndPart("deck-1", 0)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.currentCard?.isBookmarked == true)

        viewModel.toggleBookmark()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.currentCard?.isBookmarked == true)
        assertTrue(fakeRepository.cards.first { it.id == "c1" }.isBookmarked)

        viewModel.toggleBookmark()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.currentCard?.isBookmarked == true)
        assertFalse(fakeRepository.cards.first { it.id == "c1" }.isBookmarked)
    }

    @Test
    fun updateCardNotes_updatesRepositoryAndUiState() = runTest {
        viewModel.loadDeckAndPart("deck-1", 0)
        advanceUntilIdle()

        val cardId = viewModel.uiState.value.currentCard?.id ?: ""
        assertEquals("c1", cardId)

        viewModel.updateCardNotes(
            cardId = cardId,
            userQuestion = "How to use?",
            userSupplement = "An edible fruit produced by an apple tree.",
        )
        advanceUntilIdle()

        val currentCard = viewModel.uiState.value.currentCard
        assertEquals("How to use?", currentCard?.userQuestion)
        assertEquals("An edible fruit produced by an apple tree.", currentCard?.userSupplement)

        val savedInRepo = fakeRepository.cards.first { it.id == "c1" }
        assertEquals("How to use?", savedInRepo.userQuestion)
        assertEquals("An edible fruit produced by an apple tree.", savedInRepo.userSupplement)

        // Clear notes
        viewModel.updateCardNotes(cardId = cardId, userQuestion = "", userSupplement = "")
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.currentCard?.userQuestion)
        assertEquals("", viewModel.uiState.value.currentCard?.userSupplement)
    }

    private class FakeStudyWordsRepository : WordsRepository {
        val subjects = mutableListOf<Subject>()
        val decks = mutableListOf<Deck>()
        val cards = mutableListOf<Card>()
        var lastSavedSession: StudySession? = null

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
        override suspend fun getOrCreateBookmarkDeck(subjectId: String): Deck {
            val id = "bookmark_$subjectId"
            val existing = decks.find { it.id == id }
            if (existing != null) return existing
            val newDeck = Deck(
                id = id,
                subjectId = subjectId,
                name = "苦手単語（ブックマーク）",
                splitSize = 50,
            )
            decks.add(newDeck)
            return newDeck
        }
        override suspend fun saveDeck(deck: Deck) {
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
        override suspend fun saveCards(newCards: List<Card>) {
            cards.addAll(newCards)
        }
        override suspend fun deleteCard(cardId: String) {
            cards.removeIf { it.id == cardId }
        }
        override suspend fun setBookmark(cardId: String, isBookmarked: Boolean) {
            val idx = cards.indexOfFirst { it.id == cardId }
            if (idx != -1) cards[idx] = cards[idx].copy(isBookmarked = isBookmarked)
        }
        override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> =
            flowOf(cards.filter { it.isBookmarked })
        override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> = emptyList()
        override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? = null
        override suspend fun getAllCardsInSubject(subjectId: String): List<Card> = cards

        override suspend fun getSession(deckId: String): StudySession? = lastSavedSession
        override fun getSessionFlow(deckId: String): Flow<StudySession?> = flowOf(lastSavedSession)
        override suspend fun saveSession(session: StudySession) {
            lastSavedSession = session
        }
        override suspend fun deleteSession(deckId: String) {
            lastSavedSession = null
        }
    }
}
