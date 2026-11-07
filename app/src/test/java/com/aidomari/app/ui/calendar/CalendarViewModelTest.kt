package com.aidomari.app.ui.calendar

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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val zoneId = ZoneId.of("Asia/Tokyo")
    private lateinit var fakeRepository: FakeCalendarRepository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCalendarRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun dateToMillis(year: Int, month: Int, day: Int, hour: Int = 12): Long = ZonedDateTime.of(year, month, day, hour, 0, 0, 0, zoneId).toInstant().toEpochMilli()

    @Test
    fun loadStudyLogs_updatesDailyStatsAndSummary() = runTest(testDispatcher) {
        val nov1 = dateToMillis(2026, 11, 1, 10)
        val nov2 = dateToMillis(2026, 11, 2, 14)
        val logs = listOf(
            CardStudyLog(id = "l1", cardId = "c1", isCorrect = true, studiedAt = nov1),
            CardStudyLog(id = "l2", cardId = "c2", isCorrect = true, studiedAt = nov1),
            CardStudyLog(id = "l3", cardId = "c1", isCorrect = true, studiedAt = nov2),
        )
        fakeRepository.setLogs(logs)

        viewModel = CalendarViewModel(fakeRepository, zoneId)
        viewModel.setMonth(YearMonth.of(2026, 11))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(YearMonth.of(2026, 11), state.currentMonth)
        assertEquals(2, state.monthSummary.studyDaysCount)
        assertEquals(2, state.monthSummary.totalLearnedCount)
        assertEquals(1, state.monthSummary.totalReviewedCount)
        assertEquals(3, state.monthSummary.totalAnswers)

        val day1Stats = state.dailyStatsMap[LocalDate.of(2026, 11, 1)]
        assertNotNull(day1Stats)
        assertEquals(2, day1Stats?.learnedCount)
        assertEquals(0, day1Stats?.reviewedCount)
    }

    @Test
    fun selectDate_updatesSelectedDateAndSelectedDayStats() = runTest(testDispatcher) {
        val nov5 = dateToMillis(2026, 11, 5, 10)
        fakeRepository.setLogs(
            listOf(
                CardStudyLog(id = "l1", cardId = "c1", isCorrect = true, studiedAt = nov5),
            ),
        )

        viewModel = CalendarViewModel(fakeRepository, zoneId)
        advanceUntilIdle()

        val targetDate = LocalDate.of(2026, 11, 5)
        viewModel.selectDate(targetDate)

        val state = viewModel.uiState.value
        assertEquals(targetDate, state.selectedDate)
        assertNotNull(state.selectedDayStats)
        assertEquals(1, state.selectedDayStats?.learnedCount)

        // Select day without stats
        viewModel.selectDate(LocalDate.of(2026, 11, 6))
        assertNull(viewModel.uiState.value.selectedDayStats)
    }

    @Test
    fun monthNavigation_updatesCurrentMonthAndSummary() = runTest(testDispatcher) {
        viewModel = CalendarViewModel(fakeRepository, zoneId)
        viewModel.setMonth(YearMonth.of(2026, 11))
        advanceUntilIdle()

        viewModel.previousMonth()
        assertEquals(YearMonth.of(2026, 10), viewModel.uiState.value.currentMonth)

        viewModel.nextMonth()
        assertEquals(YearMonth.of(2026, 11), viewModel.uiState.value.currentMonth)

        viewModel.nextMonth()
        assertEquals(YearMonth.of(2026, 12), viewModel.uiState.value.currentMonth)
    }
}

private class FakeCalendarRepository : WordsRepository {
    private val logsFlow = MutableStateFlow<List<CardStudyLog>>(emptyList())

    fun setLogs(logs: List<CardStudyLog>) {
        logsFlow.value = logs
    }

    override fun getAllStudyLogsFlow(): Flow<List<CardStudyLog>> = logsFlow
    override suspend fun getAllStudyLogsSync(): List<CardStudyLog> = logsFlow.value

    override fun getAllSubjects(): Flow<List<Subject>> = MutableStateFlow(emptyList())
    override suspend fun getSubjectById(id: String): Subject? = null
    override suspend fun saveSubject(subject: Subject) {}
    override suspend fun deleteSubject(id: String) {}
    override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> = MutableStateFlow(emptyList())
    override suspend fun getDeckById(deckId: String): Deck? = null
    override suspend fun saveDeck(deck: Deck) {}
    override suspend fun deleteDeck(deckId: String) {}
    override fun getCardsByDeck(deckId: String): Flow<List<Card>> = MutableStateFlow(emptyList())
    override suspend fun getCardsByDeckSync(deckId: String): List<Card> = emptyList()
    override suspend fun getCardById(cardId: String): Card? = null
    override suspend fun saveCard(card: Card) {}
    override suspend fun saveCards(cards: List<Card>) {}
    override suspend fun deleteCard(cardId: String) {}
    override suspend fun setBookmark(cardId: String, isBookmarked: Boolean) {}
    override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> = MutableStateFlow(emptyList())
    override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> = emptyList()
    override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? = null
    override suspend fun getAllCardsInSubject(subjectId: String): List<Card> = emptyList()
    override suspend fun getSession(deckId: String): StudySession? = null
    override fun getSessionFlow(deckId: String): Flow<StudySession?> = MutableStateFlow(null)
    override suspend fun saveSession(session: StudySession) {}
    override suspend fun deleteSession(deckId: String) {}
}
