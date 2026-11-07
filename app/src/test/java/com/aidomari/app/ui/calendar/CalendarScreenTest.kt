package com.aidomari.app.ui.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.CardStudyLog
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CalendarScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val zoneId = ZoneId.of("Asia/Tokyo")
    private lateinit var fakeRepository: FakeCalendarScreenRepository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setup() {
        fakeRepository = FakeCalendarScreenRepository()
        viewModel = CalendarViewModel(fakeRepository, zoneId)
    }

    private fun dateToMillis(year: Int, month: Int, day: Int, hour: Int = 12): Long = ZonedDateTime.of(year, month, day, hour, 0, 0, 0, zoneId).toInstant().toEpochMilli()

    @Test
    fun calendarScreen_displaysHeaderAndSummaryCards() {
        val targetMonth = YearMonth.of(2026, 11)
        viewModel.setMonth(targetMonth)

        composeTestRule.setContent {
            CalendarScreen(
                viewModel = viewModel,
                onBackClick = {},
            )
        }

        composeTestRule.onNodeWithText("学習記録・カレンダー").assertIsDisplayed()
        composeTestRule.onNodeWithText("2026年11月").assertIsDisplayed()
        composeTestRule.onNodeWithTag("CalendarGridCard").assertIsDisplayed()
        composeTestRule.onNodeWithTag("SelectedDayDetailsCard").assertExists()
        composeTestRule.onNodeWithTag("MonthSummaryCard").assertExists()
        composeTestRule.onNodeWithText("11月の累計サマリー").assertExists()
    }

    @Test
    fun calendarScreen_clickDayCell_updatesSelectedDayDetails() {
        val nov8 = dateToMillis(2026, 11, 8, 10)
        fakeRepository.setLogs(
            listOf(
                CardStudyLog(id = "l1", cardId = "c1", isCorrect = true, studiedAt = nov8),
            ),
        )

        val targetMonth = YearMonth.of(2026, 11)
        viewModel.setMonth(targetMonth)

        composeTestRule.setContent {
            CalendarScreen(
                viewModel = viewModel,
                onBackClick = {},
            )
        }

        // Click November 8th cell
        val targetDate = LocalDate.of(2026, 11, 8)
        composeTestRule.onNodeWithTag("CalendarDay_$targetDate").performClick()
        composeTestRule.waitForIdle()

        // Details should show stats for Nov 8
        composeTestRule.onNodeWithText("新しく覚えた").assertExists()
        composeTestRule.onNodeWithText("1 単語").assertExists()
    }

    @Test
    fun calendarScreen_monthNavigation_changesMonth() {
        viewModel.setMonth(YearMonth.of(2026, 11))

        composeTestRule.setContent {
            CalendarScreen(
                viewModel = viewModel,
                onBackClick = {},
            )
        }

        composeTestRule.onNodeWithText("2026年11月").assertIsDisplayed()

        // Click next month
        composeTestRule.onNodeWithTag("NextMonthButton").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("2026年12月").assertIsDisplayed()

        // Click previous month twice
        composeTestRule.onNodeWithTag("PreviousMonthButton").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("2026年11月").assertIsDisplayed()

        composeTestRule.onNodeWithTag("PreviousMonthButton").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("2026年10月").assertIsDisplayed()
    }

    @Test
    fun calendarScreen_backButton_triggersCallback() {
        var backClicked = false

        composeTestRule.setContent {
            CalendarScreen(
                viewModel = viewModel,
                onBackClick = { backClicked = true },
            )
        }

        composeTestRule.onNodeWithTag("CalendarBackButton").performClick()
        assertTrue(backClicked)
    }
}

private class FakeCalendarScreenRepository : WordsRepository {
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
