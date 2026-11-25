package com.aidomari.app.ui.sugoroku

import com.aidomari.app.domain.model.Act1TravelRoute
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.HokkaidoAct1Route
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.model.SugorokuProgress
import com.aidomari.app.domain.model.advanceByDistance
import com.aidomari.app.domain.repository.WordsRepository
import com.aidomari.app.domain.util.SugorokuWalkingCalculator
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun initialState_beforeLoadingProgress_hasIsProgressLoadedFalse() = runTest {
        val initialVm = SugorokuMapViewModel(fakeRepository)
        assertFalse(initialVm.uiState.value.isProgressLoaded)
        assertTrue(initialVm.uiState.value.isLoading)

        advanceUntilIdle()
        assertTrue(initialVm.uiState.value.isProgressLoaded)
        assertFalse(initialVm.uiState.value.isLoading)
    }

    @Test
    fun initialState_loadsProgressAndStartNode() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isProgressLoaded)
        assertFalse(state.isLoading)
        assertEquals(0, state.progress.currentStepIndex)
        assertEquals("函館山", state.currentNode.name)
        assertEquals(HokkaidoAct1Route.totalNodes, state.totalNodes)
        assertEquals(HokkaidoAct1Route.maxStepIndex, state.remainingSteps)
        assertEquals(0f, state.totalDistanceMeters, 0.001f)
        assertNotNull(state.playerWorldOffset)
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
    fun addDistance_advancesDistanceAndContinuousOffset() = runTest {
        advanceUntilIdle()

        viewModel.addDistance(300f)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(300f, state.totalDistanceMeters, 0.001f)
        assertEquals(0, state.progress.currentStepIndex) // Still on leg 0 (558m)
        assertTrue(state.playerWorldOffset.x > 0f)
    }

    @Test
    fun advanceToNextAnchor_jumpsToNextAnchor() = runTest {
        advanceUntilIdle()

        viewModel.advanceToNextAnchor()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.progress.currentStepIndex)
        assertEquals("柳川熊吉翁の碑", state.currentNode.name)
        assertTrue(state.totalDistanceMeters >= 558f)
        assertNotNull(state.arrivalCelebrationNode)
        assertEquals("柳川熊吉翁の碑", state.arrivalCelebrationNode?.name)
    }

    @Test
    fun dismissCelebration_clearsArrivalCelebrationNode() = runTest {
        advanceUntilIdle()

        viewModel.advanceToNextAnchor()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.arrivalCelebrationNode)

        viewModel.dismissCelebration()
        assertNull(viewModel.uiState.value.arrivalCelebrationNode)
    }

    @Test
    fun triggerCelebration_manuallySetsArrivalCelebrationNode() = runTest {
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.arrivalCelebrationNode)

        viewModel.triggerCelebration()
        assertNotNull(viewModel.uiState.value.arrivalCelebrationNode)
        assertEquals("函館山", viewModel.uiState.value.arrivalCelebrationNode?.name)
    }

    @Test
    fun resetProgress_clearsProgressToZero() = runTest {
        advanceUntilIdle()

        viewModel.addDistance(1000f)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.totalDistanceMeters > 0f)

        viewModel.triggerCelebration()
        assertNotNull(viewModel.uiState.value.arrivalCelebrationNode)

        viewModel.resetProgress()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0f, state.totalDistanceMeters, 0.001f)
        assertEquals(0, state.progress.currentStepIndex)
        assertEquals("函館山", state.currentNode.name)
        assertNull(state.arrivalCelebrationNode)
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

        override suspend fun recordStudyWalkingProgress(
            durationSeconds: Float,
            speedMultiplier: Float,
        ): SugorokuProgress {
            val dist = SugorokuWalkingCalculator.calculateDistanceMeters(durationSeconds, speedMultiplier)
            return recordSugorokuDistance(dist)
        }

        override suspend fun recordSugorokuDistance(distanceMetersDelta: Float): SugorokuProgress {
            val legs = Act1TravelRoute.getLegs()
            val updated = progressFlow.value.advanceByDistance(
                distanceMetersDelta = distanceMetersDelta,
                legs = legs,
                anchors = HokkaidoAct1Route.anchors,
                questionsAnsweredDelta = 1,
            )
            progressFlow.value = updated
            return updated
        }

        override suspend fun advanceToNextAnchor(): SugorokuProgress {
            val legs = Act1TravelRoute.getLegs()
            val current = progressFlow.value
            val nextStepIndex = (current.currentStepIndex + 1).coerceAtMost(legs.size)
            var targetDistance = 0f
            for (i in 0 until nextStepIndex.coerceAtMost(legs.size)) {
                targetDistance += legs[i].distanceMeters
            }
            val neededDelta = (targetDistance - current.totalDistanceMeters).coerceAtLeast(1f)
            return recordSugorokuDistance(neededDelta)
        }

        override suspend fun resetSugorokuProgress(): SugorokuProgress {
            val reset = progressFlow.value.resetProgress()
            progressFlow.value = reset
            return reset
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
