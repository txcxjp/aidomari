package com.aidomari.app.ui.sugoroku

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.HokkaidoAct1Route
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.model.SugorokuProgress
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SugorokuMapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSugorokuWordsRepository
    private lateinit var viewModel: SugorokuMapViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSugorokuWordsRepository()
        viewModel = SugorokuMapViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsProgressAndStartNode() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.progress.currentStepIndex)
        assertEquals("函館駅前", state.currentNode.name)
        assertEquals(2000, state.totalNodes)
        assertEquals(1999, state.remainingSteps)
        assertNull(state.selectedNode)
    }

    @Test
    fun selectNode_updatesSelectedNode() = runTest {
        advanceUntilIdle()

        val targetNode = HokkaidoAct1Route.getNodeByStepIndex(10)
        assertNotNull(targetNode)

        viewModel.selectNode(targetNode)
        assertEquals(targetNode, viewModel.uiState.value.selectedNode)

        viewModel.selectNode(null)
        assertNull(viewModel.uiState.value.selectedNode)
    }

    @Test
    fun advanceProgress_advancesStepsAndUpdatesCurrentNode() = runTest {
        advanceUntilIdle()

        // Advance by 5 steps
        viewModel.advanceProgress(5)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.progress.currentStepIndex)
        assertEquals(5, state.currentNode.stepIndex)
        assertEquals(1994, state.remainingSteps)
    }

    private class FakeSugorokuWordsRepository : WordsRepository {
        private val progressFlow = MutableStateFlow(SugorokuProgress(targetQuestionsPerStep = 1))

        override fun getSugorokuProgress(): Flow<SugorokuProgress> = progressFlow.asStateFlow()

        override suspend fun getSugorokuProgressSync(): SugorokuProgress = progressFlow.value

        override suspend fun saveSugorokuProgress(progress: SugorokuProgress) {
            progressFlow.value = progress
        }

        override suspend fun recordSugorokuProgress(questionsDelta: Int): SugorokuProgress {
            val updated = progressFlow.value.advanceByQuestions(questionsDelta)
            progressFlow.value = updated
            return updated
        }

        override fun getAllSubjects(): Flow<List<Subject>> = emptyFlow()
        override suspend fun getSubjectById(id: String): Subject? = null
        override suspend fun saveSubject(subject: Subject) {}
        override suspend fun deleteSubject(id: String) {}
        override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> = emptyFlow()
        override suspend fun getDeckById(deckId: String): Deck? = null
        override suspend fun saveDeck(deck: Deck) {}
        override suspend fun deleteDeck(deckId: String) {}
        override fun getCardsByDeck(deckId: String): Flow<List<Card>> = emptyFlow()
        override suspend fun getCardsByDeckSync(deckId: String): List<Card> = emptyList()
        override suspend fun getCardById(cardId: String): Card? = null
        override suspend fun saveCard(card: Card) {}
        override suspend fun saveCards(cards: List<Card>) {}
        override suspend fun deleteCard(cardId: String) {}
        override suspend fun setBookmark(cardId: String, isBookmarked: Boolean) {}
        override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> = emptyFlow()
        override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> = emptyList()
        override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? = null
        override suspend fun getAllCardsInSubject(subjectId: String): List<Card> = emptyList()
        override suspend fun getSession(deckId: String) = null
        override fun getSessionFlow(deckId: String) = emptyFlow<Nothing?>()
        override suspend fun saveSession(session: com.aidomari.app.domain.model.StudySession) {}
        override suspend fun deleteSession(deckId: String) {}
    }
}
