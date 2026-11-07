package com.aidomari.app.ui.deck

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.CardStudyLog
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
            reverseMode = true,
        )
        advanceUntilIdle()

        assertEquals(1, fakeRepository.decksList.size)
        val deck = fakeRepository.decksList[0]
        assertEquals("ターゲット1900", deck.name)
        assertEquals(50, deck.splitSize)
        assertTrue(deck.autoContinue)
        assertTrue(deck.shuffleMode)
        assertTrue(deck.reverseMode)
    }

    @Test
    fun createDeck_withTtsSettings_savesAutoSpeakAndSpeechRate() = runTest {
        viewModel.createDeck(
            name = "リスニング用",
            splitSize = 30,
            autoSpeak = true,
            speechRate = 1.25f,
        )
        advanceUntilIdle()

        val deck = fakeRepository.decksList.find { it.name == "リスニング用" }
        assertEquals(true, deck?.autoSpeak)
        assertEquals(1.25f, deck?.speechRate ?: 0f, 0.001f)
    }

    @Test
    fun toggleAutoSpeak_invertsAutoSpeakAndSaves() = runTest {
        val deck = Deck(id = "d-tts", subjectId = "sub-1", name = "TTS Test", autoSpeak = false)
        fakeRepository.decksList.add(deck)
        fakeRepository.triggerDecksUpdate()
        advanceUntilIdle()

        viewModel.toggleAutoSpeak("d-tts")
        advanceUntilIdle()

        val updated = fakeRepository.decksList.find { it.id == "d-tts" }
        assertEquals(true, updated?.autoSpeak)

        viewModel.toggleAutoSpeak("d-tts")
        advanceUntilIdle()

        val toggledBack = fakeRepository.decksList.find { it.id == "d-tts" }
        assertEquals(false, toggledBack?.autoSpeak)
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
    fun observeBookmarks_updatesBookmarkDeckUiModel() = runTest {
        assertEquals(null, viewModel.bookmarkDeckUiModel.value)

        val card1 = Card(id = "c1", deckId = "d1", front = "A", back = "a", isBookmarked = true)
        val card2 = Card(id = "c2", deckId = "d1", front = "B", back = "b", isBookmarked = true)
        fakeRepository.bookmarkedFlow.value = listOf(card1, card2)
        advanceUntilIdle()

        val bookmarkModel = viewModel.bookmarkDeckUiModel.value
        assertEquals(2, bookmarkModel?.totalCount)
        assertEquals("bookmark_sub-1", bookmarkModel?.deck?.id)
        assertEquals(1, bookmarkModel?.parts?.size)
        assertEquals(2, bookmarkModel?.parts?.get(0)?.part?.count)

        // When all bookmarks are removed
        fakeRepository.bookmarkedFlow.value = emptyList()
        advanceUntilIdle()

        assertEquals(null, viewModel.bookmarkDeckUiModel.value)
    }

    @Test
    fun updateDeck_modifiesDeckSettingsAndSaves() = runTest {
        val deck = Deck(id = "d1", subjectId = "sub-1", name = "Old Name", splitSize = 50, reverseMode = false)
        fakeRepository.decksList.add(deck)
        fakeRepository.triggerDecksUpdate()
        advanceUntilIdle()

        viewModel.updateDeck(deck.copy(name = "New Name", splitSize = 30, autoContinue = true, reverseMode = true))
        advanceUntilIdle()

        val updated = fakeRepository.decksList.find { it.id == "d1" }
        assertEquals("New Name", updated?.name)
        assertEquals(30, updated?.splitSize)
        assertTrue(updated?.autoContinue == true)
        assertTrue(updated?.reverseMode == true)
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

    @Test
    fun onSearchQueryChanged_matchingQuery_updatesSearchResults() = runTest {
        val c1 = Card(id = "c1", deckId = "d1", front = "Abundant", back = "豊富な")
        val c2 = Card(id = "c2", deckId = "d1", front = "Scarcity", back = "不足、希少")
        val c3 = Card(id = "c3", deckId = "d1", front = "Dog", back = "犬", userSupplement = "Canine friend")
        fakeRepository.cardsList.addAll(listOf(c1, c2, c3))

        viewModel.onSearchQueryChanged("Abund")
        advanceUntilIdle()

        assertEquals(1, viewModel.searchResults.value.size)
        assertEquals("Abundant", viewModel.searchResults.value[0].front)

        // Search by meaning
        viewModel.onSearchQueryChanged("希少")
        advanceUntilIdle()
        assertEquals(1, viewModel.searchResults.value.size)
        assertEquals("Scarcity", viewModel.searchResults.value[0].front)

        // Search by supplement note
        viewModel.onSearchQueryChanged("canine")
        advanceUntilIdle()
        assertEquals(1, viewModel.searchResults.value.size)
        assertEquals("Dog", viewModel.searchResults.value[0].front)

        // Empty query clears
        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()
        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    @Test
    fun clearSearch_resetsQueryAndResults() = runTest {
        fakeRepository.cardsList.add(Card(id = "c1", deckId = "d1", front = "Apple", back = "りんご"))
        viewModel.onSearchQueryChanged("Apple")
        advanceUntilIdle()
        assertEquals(1, viewModel.searchResults.value.size)

        viewModel.clearSearch()
        assertEquals("", viewModel.searchQuery.value)
        assertTrue(viewModel.searchResults.value.isEmpty())
    }

    @Test
    fun toggleBookmark_updatesSearchResultsAndSubjectCards() = runTest {
        val card = Card(id = "c1", deckId = "d1", front = "Apple", back = "りんご", isBookmarked = false)
        fakeRepository.cardsList.add(card)
        viewModel.onSearchQueryChanged("Apple")
        advanceUntilIdle()

        viewModel.toggleBookmark(card)
        advanceUntilIdle()

        val searchCard = viewModel.searchResults.value.find { it.id == "c1" }
        assertTrue(searchCard!!.isBookmarked)

        val repoCard = fakeRepository.cardsList.find { it.id == "c1" }
        assertTrue(repoCard!!.isBookmarked)
    }

    @Test
    fun updateCardNotes_updatesSearchResultsAndRepository() = runTest {
        val card = Card(id = "c1", deckId = "d1", front = "Apple", back = "りんご")
        fakeRepository.cardsList.add(card)
        viewModel.onSearchQueryChanged("Apple")
        advanceUntilIdle()

        viewModel.updateCardNotes("c1", "どんな果物？", "バラ科の植物")
        advanceUntilIdle()

        val searchCard = viewModel.searchResults.value.find { it.id == "c1" }
        assertEquals("どんな果物？", searchCard?.userQuestion)
        assertEquals("バラ科の植物", searchCard?.userSupplement)

        val repoCard = fakeRepository.cardsList.find { it.id == "c1" }
        assertEquals("どんな果物？", repoCard?.userQuestion)
        assertEquals("バラ科の植物", repoCard?.userSupplement)
    }

    @Test
    fun setRandomQuestionCount_updatesState() {
        assertEquals(10, viewModel.randomQuestionCount.value)
        viewModel.setRandomQuestionCount(20)
        assertEquals(20, viewModel.randomQuestionCount.value)
        viewModel.setRandomQuestionCount(-1)
        assertEquals(-1, viewModel.randomQuestionCount.value)
    }

    @Test
    fun loadAllSubjectCards_populatesSubjectCards() = runTest {
        val card1 = Card(id = "c1", deckId = "d1", front = "Front 1", back = "Back 1")
        val card2 = Card(id = "c2", deckId = "d1", front = "Front 2", back = "Back 2")
        fakeRepository.cardsList.addAll(listOf(card1, card2))

        viewModel.loadAllSubjectCards()
        advanceUntilIdle()

        assertEquals(2, viewModel.allSubjectCards.value.size)
    }

    @Test
    fun loadDecks_withCompletedParts_calculatesMasteryRateCorrectly() = runTest {
        val deck = Deck(id = "d1", subjectId = "sub-1", name = "Test Deck", splitSize = 2)
        val cards = listOf(
            Card(id = "c1", deckId = "d1", front = "f1", back = "b1"),
            Card(id = "c2", deckId = "d1", front = "f2", back = "b2"),
            Card(id = "c3", deckId = "d1", front = "f3", back = "b3"),
            Card(id = "c4", deckId = "d1", front = "f4", back = "b4"),
        )
        fakeRepository.decksList.add(deck)
        fakeRepository.cardsList.addAll(cards)
        fakeRepository.savedSession = StudySession(
            deckId = "d1",
            partIndex = 1,
            isCompleted = false,
            completedPartIndices = setOf(0),
        )
        fakeRepository.studyLogs.add(CardStudyLog(id = "l1", cardId = "c1", isCorrect = true, studiedAt = 1000L))
        fakeRepository.studyLogs.add(CardStudyLog(id = "l2", cardId = "c2", isCorrect = true, studiedAt = 2000L))
        fakeRepository.triggerDecksUpdate()
        advanceUntilIdle()

        val uiModels = viewModel.decksWithParts.value
        assertEquals(1, uiModels.size)
        val deckModel = uiModels[0]
        assertEquals(2, deckModel.totalPartsCount)
        assertEquals(1, deckModel.completedPartsCount)
        assertEquals(2, deckModel.learnedCardCount)
        assertEquals(4, deckModel.totalCardCount)
        assertEquals(0.5f, deckModel.masteryRate, 0.001f)
        assertEquals(false, deckModel.isAllCompleted)

        assertEquals(true, deckModel.parts[0].isCompleted)
        assertEquals(false, deckModel.parts[0].isInProgress)
        assertEquals(false, deckModel.parts[1].isCompleted)
        assertEquals(true, deckModel.parts[1].isInProgress)
    }

    @Test
    fun loadDecks_allPartsCompleted_masteryRateIs100Percent() = runTest {
        val deck = Deck(id = "d1", subjectId = "sub-1", name = "Test Deck", splitSize = 2)
        val cards = listOf(
            Card(id = "c1", deckId = "d1", front = "f1", back = "b1"),
            Card(id = "c2", deckId = "d1", front = "f2", back = "b2"),
        )
        fakeRepository.decksList.add(deck)
        fakeRepository.cardsList.addAll(cards)
        fakeRepository.savedSession = StudySession(
            deckId = "d1",
            partIndex = 0,
            isCompleted = true,
            completedPartIndices = setOf(0),
        )
        fakeRepository.studyLogs.add(CardStudyLog(id = "l1", cardId = "c1", isCorrect = true, studiedAt = 1000L))
        fakeRepository.studyLogs.add(CardStudyLog(id = "l2", cardId = "c2", isCorrect = true, studiedAt = 2000L))
        fakeRepository.triggerDecksUpdate()
        advanceUntilIdle()

        val deckModel = viewModel.decksWithParts.value[0]
        assertEquals(1, deckModel.completedPartsCount)
        assertEquals(1.0f, deckModel.masteryRate, 0.001f)
        assertEquals(true, deckModel.isAllCompleted)
        assertEquals(true, deckModel.parts[0].isCompleted)
    }

    @Test
    fun loadSubjectInfo_andUnarchiveSubject_updatesState() = runTest {
        val subject = Subject(id = "sub-1", name = "Test Subject", isArchived = true)
        fakeRepository.subjectsList.add(subject)

        viewModel.loadSubjectInfo()
        advanceUntilIdle()

        assertTrue(viewModel.isSubjectArchived.value)

        var unarchivedCallbackInvoked = false
        viewModel.unarchiveSubject {
            unarchivedCallbackInvoked = true
        }
        advanceUntilIdle()

        assertTrue(unarchivedCallbackInvoked)
        assertEquals(false, viewModel.isSubjectArchived.value)
        assertEquals(false, fakeRepository.subjectsList[0].isArchived)
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
        override suspend fun getSubjectById(id: String): Subject? = subjectsList.find { it.id == id }
        override suspend fun saveSubject(subject: Subject) {
            subjectsList.removeIf { it.id == subject.id }
            subjectsList.add(subject)
        }
        override suspend fun deleteSubject(id: String) {
        }

        override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> = decksFlow
        override suspend fun getDeckById(deckId: String): Deck? = decksList.find { it.id == deckId }
        override suspend fun getOrCreateBookmarkDeck(subjectId: String): Deck {
            val id = "bookmark_$subjectId"
            val existing = decksList.find { it.id == id }
            if (existing != null) return existing
            val newDeck = Deck(
                id = id,
                subjectId = subjectId,
                name = "苦手単語（ブックマーク）",
                splitSize = 50,
            )
            decksList.add(newDeck)
            return newDeck
        }
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
            cardsList.removeIf { it.id == card.id }
            cardsList.add(card)
        }
        override suspend fun saveCards(newCards: List<Card>) {
            cardsList.addAll(newCards)
        }
        override suspend fun deleteCard(cardId: String) {
            cardsList.removeIf { it.id == cardId }
        }
        val bookmarkedFlow = MutableStateFlow<List<Card>>(emptyList())

        override suspend fun setBookmark(cardId: String, isBookmarked: Boolean) {
            val idx = cardsList.indexOfFirst { it.id == cardId }
            if (idx != -1) {
                cardsList[idx] = cardsList[idx].copy(isBookmarked = isBookmarked)
            }
        }
        override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> = bookmarkedFlow
        override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> =
            cardsList.filter {
                it.front.contains(query, ignoreCase = true) ||
                    it.back.contains(query, ignoreCase = true) ||
                    it.userQuestion.contains(query, ignoreCase = true) ||
                    it.userSupplement.contains(query, ignoreCase = true)
            }
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

        val studyLogs = mutableListOf<CardStudyLog>()
        override suspend fun getLatestLogsForDeckSync(deckId: String): List<CardStudyLog> {
            val deckCardIds = cardsList.filter { it.deckId == deckId }.map { it.id }.toSet()
            return studyLogs
                .filter { it.cardId in deckCardIds }
                .groupBy { it.cardId }
                .mapValues { (_, logs) -> logs.maxByOrNull { it.studiedAt }!! }
                .values
                .toList()
        }
    }
}
