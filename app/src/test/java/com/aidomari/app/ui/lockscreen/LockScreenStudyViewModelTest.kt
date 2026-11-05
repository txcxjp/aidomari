package com.aidomari.app.ui.lockscreen

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.aidomari.app.core.lockscreen.LockScreenPreferences
import com.aidomari.app.core.lockscreen.LockScreenTarget
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LockScreenStudyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeLockScreenRepository
    private lateinit var preferences: LockScreenPreferences
    private lateinit var viewModel: LockScreenStudyViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("aidomari_lock_screen_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        preferences = LockScreenPreferences(context)
        fakeRepository = FakeLockScreenRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadRandomCard_whenCardsExist_populatesCardAndLanguage() = runTest {
        val subject = Subject(id = "sub-1", name = "英語", ttsLanguage = "en-US")
        val deck = Deck(id = "deck-1", subjectId = "sub-1", name = "単語帳")
        val card = Card(id = "card-1", deckId = "deck-1", front = "apple", back = "りんご")
        fakeRepository.subjects.add(subject)
        fakeRepository.decks.add(deck)
        fakeRepository.cards.add(card)

        viewModel = LockScreenStudyViewModel(fakeRepository, preferences)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.card)
        assertEquals("apple", state.card?.front)
        assertEquals("en-US", state.ttsLanguage)
    }

    @Test
    fun loadRandomCard_whenNoCardsExist_setsCardToNull() = runTest {
        viewModel = LockScreenStudyViewModel(fakeRepository, preferences)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.card)
    }

    @Test
    fun flipCard_togglesIsFlippedState() = runTest {
        viewModel = LockScreenStudyViewModel(fakeRepository, preferences)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isFlipped)
        viewModel.flipCard()
        assertTrue(viewModel.uiState.value.isFlipped)
        viewModel.flipCard()
        assertFalse(viewModel.uiState.value.isFlipped)
    }

    @Test
    fun markRetry_setsBookmarkAndCallsCallback() = runTest {
        val card = Card(id = "card-1", deckId = "deck-1", front = "test", back = "テスト", isBookmarked = false)
        fakeRepository.cards.add(card)

        viewModel = LockScreenStudyViewModel(fakeRepository, preferences)
        advanceUntilIdle()

        var finished = false
        viewModel.markRetry { finished = true }
        advanceUntilIdle()

        assertTrue(finished)
        assertTrue(fakeRepository.bookmarkedCardIds.contains("card-1"))
    }

    @Test
    fun targetBookmarkOnly_requestsOnlyBookmarkedCard() = runTest {
        preferences.setTarget(LockScreenTarget.BOOKMARK_ONLY)
        val normalCard = Card(id = "card-1", deckId = "deck-1", front = "normal", back = "通常", isBookmarked = false)
        val bookmarkCard = Card(id = "card-2", deckId = "deck-1", front = "bookmark", back = "ブックマーク", isBookmarked = true)
        fakeRepository.cards.addAll(listOf(normalCard, bookmarkCard))

        viewModel = LockScreenStudyViewModel(fakeRepository, preferences)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("card-2", state.card?.id)
    }

    private class FakeLockScreenRepository : WordsRepository {
        val subjects = mutableListOf<Subject>()
        val decks = mutableListOf<Deck>()
        val cards = mutableListOf<Card>()
        val bookmarkedCardIds = mutableSetOf<String>()

        override fun getAllSubjects(): Flow<List<Subject>> = MutableStateFlow(subjects)
        override suspend fun getSubjectById(id: String): Subject? = subjects.find { it.id == id }
        override suspend fun saveSubject(subject: Subject) {
            subjects.add(subject)
        }
        override suspend fun deleteSubject(id: String) {}

        override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> = MutableStateFlow(decks)
        override suspend fun getDeckById(deckId: String): Deck? = decks.find { it.id == deckId }
        override suspend fun saveDeck(deck: Deck) {}
        override suspend fun deleteDeck(deckId: String) {}

        override fun getCardsByDeck(deckId: String): Flow<List<Card>> = MutableStateFlow(cards)
        override suspend fun getCardsByDeckSync(deckId: String): List<Card> = cards
        override suspend fun getCardById(cardId: String): Card? = cards.find { it.id == cardId }
        override suspend fun saveCard(card: Card) {}
        override suspend fun saveCards(cards: List<Card>) {}
        override suspend fun deleteCard(cardId: String) {}
        override suspend fun setBookmark(cardId: String, isBookmarked: Boolean) {
            if (isBookmarked) bookmarkedCardIds.add(cardId) else bookmarkedCardIds.remove(cardId)
        }
        override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> = MutableStateFlow(emptyList())
        override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> = emptyList()
        override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? = null
        override suspend fun getAllCardsInSubject(subjectId: String): List<Card> = cards

        override suspend fun getRandomCard(bookmarkOnly: Boolean): Card? = if (bookmarkOnly) {
            cards.find { it.isBookmarked }
        } else {
            cards.firstOrNull()
        }

        override suspend fun getSession(deckId: String) = null
        override fun getSessionFlow(deckId: String) = MutableStateFlow(null)
        override suspend fun saveSession(session: com.aidomari.app.domain.model.StudySession) {}
        override suspend fun deleteSession(deckId: String) {}
    }
}
