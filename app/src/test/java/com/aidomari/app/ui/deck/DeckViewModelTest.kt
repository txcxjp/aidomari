package com.aidomari.app.ui.deck

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeckViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeDeckRepository
    private lateinit var viewModel: DeckViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeDeckRepository()
        viewModel = DeckViewModel(subjectId = "sub-1", repository = fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createDeck_savesDeckWithSettings() = runTest {
        viewModel.createDeck(
            name = "ターゲット1900",
            splitSize = 50,
            autoContinue = true,
            shuffleMode = true,
        )
        advanceUntilIdle()

        assertEquals(1, fakeRepository.decksList.size)
        val deck = fakeRepository.decksList[0]
        assertEquals("ターゲット1900", deck.name)
        assertEquals(50, deck.splitSize)
        assertTrue(deck.autoContinue)
        assertTrue(deck.shuffleMode)
    }

    @Test
    fun deleteDeck_removesDeckFromRepository() = runTest {
        viewModel.createDeck("Deck to Delete")
        advanceUntilIdle()

        val deckId = fakeRepository.decksList[0].id
        viewModel.deleteDeck(deckId)
        advanceUntilIdle()

        assertTrue(fakeRepository.decksList.isEmpty())
    }

    @Test
    fun toggleShuffle_invertsShuffleModeAndSaves() = runTest {
        val deck = Deck(id = "d1", subjectId = "sub-1", name = "英単語", shuffleMode = false)
        fakeRepository.decksList.add(deck)
        fakeRepository.triggerDecksUpdate()
        advanceUntilIdle()

        viewModel.toggleShuffle("d1")
        advanceUntilIdle()

        val updated = fakeRepository.decksList.find { it.id == "d1" }
        assertTrue(updated?.shuffleMode == true)

        viewModel.toggleShuffle("d1")
        advanceUntilIdle()

        val toggledBack = fakeRepository.decksList.find { it.id == "d1" }
        assertTrue(toggledBack?.shuffleMode == false)
    }

    @Test
    fun updateDeck_modifiesDeckSettingsAndSaves() = runTest {
        val deck = Deck(id = "d1", subjectId = "sub-1", name = "Old Name", splitSize = 50)
        fakeRepository.decksList.add(deck)
        fakeRepository.triggerDecksUpdate()
        advanceUntilIdle()

        viewModel.updateDeck(deck.copy(name = "New Name", splitSize = 30, autoContinue = true))
        advanceUntilIdle()

        val updated = fakeRepository.decksList.find { it.id == "d1" }
        assertEquals("New Name", updated?.name)
        assertEquals(30, updated?.splitSize)
        assertTrue(updated?.autoContinue == true)
    }

    @Test
    fun loadDecks_calculatesPartsAndProgress() = runTest {
        val deck = Deck(id = "d1", subjectId = "sub-1", name = "英単語", splitSize = 50)
        fakeRepository.decksList.add(deck)

        // 120 cards -> 3 parts: Part 1 (1..50), Part 2 (51..100), Part 3 (101..120)
        for (i in 1..120) {
            fakeRepository.cardsList.add(
                Card(id = "c$i", deckId = "d1", front = "w$i", back = "m$i", orderIndex = i),
            )
        }

        fakeRepository.savedSession = StudySession(
            deckId = "d1",
            partIndex = 0,
            currentCardIndex = 25,
            isCompleted = false,
        )

        fakeRepository.triggerDecksUpdate()
        advanceUntilIdle()

        val models = viewModel.decksWithParts.value
        assertEquals(1, models.size)
        assertEquals(120, models[0].totalCardCount)
        assertEquals(3, models[0].parts.size)

        // Part 0 has session with progress 25
        val part0 = models[0].parts[0]
        assertEquals(0, part0.part.partIndex)
        assertEquals(50, part0.part.count)
        assertEquals(25, part0.session?.currentCardIndex)
    }

    private class FakeDeckRepository : WordsRepository {
        val subjectsList = mutableListOf<Subject>()
        val decksList = mutableListOf<Deck>()
        val cardsList = mutableListOf<Card>()
        val decksFlow = MutableStateFlow<List<Deck>>(emptyList())
        var savedSession: StudySession? = null

        fun triggerDecksUpdate() {
            decksFlow.value = decksList.toList()
        }

        override fun getAllSubjects(): Flow<List<Subject>> = MutableStateFlow(subjectsList)
        override suspend fun getSubjectById(id: String): Subject? = null
        override suspend fun saveSubject(subject: Subject) {
        }
        override suspend fun deleteSubject(id: String) {
        }

        override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> = decksFlow
        override suspend fun getDeckById(deckId: String): Deck? = decksList.find { it.id == deckId }
        override suspend fun saveDeck(deck: Deck) {
            decksList.removeIf { it.id == deck.id }
            decksList.add(deck)
            triggerDecksUpdate()
        }
        override suspend fun deleteDeck(deckId: String) {
            decksList.removeIf { it.id == deckId }
            triggerDecksUpdate()
        }

        override fun getCardsByDeck(deckId: String): Flow<List<Card>> =
            MutableStateFlow(cardsList.filter { it.deckId == deckId })
        override suspend fun getCardsByDeckSync(deckId: String): List<Card> =
            cardsList.filter { it.deckId == deckId }
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
        }
        override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> = MutableStateFlow(emptyList())
        override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> = emptyList()
        override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? = null
        override suspend fun getAllCardsInSubject(subjectId: String): List<Card> = cardsList

        override suspend fun getSession(deckId: String): StudySession? = savedSession
        override fun getSessionFlow(deckId: String): Flow<StudySession?> = MutableStateFlow(savedSession)
        override suspend fun saveSession(session: StudySession) {
            savedSession = session
        }
        override suspend fun deleteSession(deckId: String) {
            savedSession = null
        }
    }
}
