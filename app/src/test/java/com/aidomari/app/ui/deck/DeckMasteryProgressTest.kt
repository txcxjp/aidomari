package com.aidomari.app.ui.deck

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.CardStudyLog
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DeckMasteryProgressTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeMasteryRepository
    private lateinit var viewModel: DeckViewModel

    @Before
    fun setup() {
        fakeRepository = FakeMasteryRepository()
        viewModel = DeckViewModel("sub-1", fakeRepository)
    }

    @Test
    fun deckList_displaysMasteryRateAndProgressBar_forPartialCompletion() {
        val deck = Deck(id = "d1", subjectId = "sub-1", name = "進捗テスト単語帳", splitSize = 2)
        val cards = listOf(
            Card(id = "c1", deckId = "d1", front = "f1", back = "b1"),
            Card(id = "c2", deckId = "d1", front = "f2", back = "b2"),
            Card(id = "c3", deckId = "d1", front = "f3", back = "b3"),
            Card(id = "c4", deckId = "d1", front = "f4", back = "b4"),
        )
        fakeRepository.decksList.add(deck)
        fakeRepository.cardsList.addAll(cards)
        fakeRepository.sessionsMap["d1"] = StudySession(
            deckId = "d1",
            partIndex = 1,
            isCompleted = false,
            completedPartIndices = setOf(0),
        )
        fakeRepository.studyLogs.add(CardStudyLog(id = "l1", cardId = "c1", isCorrect = true, studiedAt = 1000L))
        fakeRepository.studyLogs.add(CardStudyLog(id = "l2", cardId = "c2", isCorrect = true, studiedAt = 2000L))
        fakeRepository.triggerDecksUpdate()

        composeTestRule.setContent {
            DeckListScreen(
                subjectName = "英単語",
                viewModel = viewModel,
                onBack = {},
                onPartClick = { _, _ -> },
                onSpeakText = {},
            )
        }

        composeTestRule.waitForIdle()

        // Verify mastery rate 50% and card count are displayed
        composeTestRule.onNodeWithText("進捗率: 50%").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 / 4 単語").assertIsDisplayed()

        // Verify Part 1 (partIndex 0) has "完了" badge
        composeTestRule.onNodeWithText("完了").assertIsDisplayed()
    }

    @Test
    fun deckList_displaysMasteryRate100_whenAllPartsCompleted() {
        val deck = Deck(id = "d1", subjectId = "sub-1", name = "完了テスト単語帳", splitSize = 2)
        val cards = listOf(
            Card(id = "c1", deckId = "d1", front = "f1", back = "b1"),
            Card(id = "c2", deckId = "d1", front = "f2", back = "b2"),
        )
        fakeRepository.decksList.add(deck)
        fakeRepository.cardsList.addAll(cards)
        fakeRepository.sessionsMap["d1"] = StudySession(
            deckId = "d1",
            partIndex = 0,
            isCompleted = true,
            completedPartIndices = setOf(0),
        )
        fakeRepository.studyLogs.add(CardStudyLog(id = "l1", cardId = "c1", isCorrect = true, studiedAt = 1000L))
        fakeRepository.studyLogs.add(CardStudyLog(id = "l2", cardId = "c2", isCorrect = true, studiedAt = 2000L))
        fakeRepository.triggerDecksUpdate()

        composeTestRule.setContent {
            DeckListScreen(
                subjectName = "英単語",
                viewModel = viewModel,
                onBack = {},
                onPartClick = { _, _ -> },
                onSpeakText = {},
            )
        }

        composeTestRule.waitForIdle()

        // Verify mastery rate 100% and card count are displayed
        composeTestRule.onNodeWithText("進捗率: 100%").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 / 2 単語").assertIsDisplayed()
        composeTestRule.onNodeWithText("完了").assertIsDisplayed()
    }

    @Test
    fun deckList_progressRemainsConsistent_whenSplitSizeChanged() {
        // 4 cards in total, 2 cards correctly answered (c1, c2)
        val deckInitial = Deck(id = "d1", subjectId = "sub-1", name = "枚数変更テスト", splitSize = 2)
        val cards = listOf(
            Card(id = "c1", deckId = "d1", front = "f1", back = "b1"),
            Card(id = "c2", deckId = "d1", front = "f2", back = "b2"),
            Card(id = "c3", deckId = "d1", front = "f3", back = "b3"),
            Card(id = "c4", deckId = "d1", front = "f4", back = "b4"),
        )
        fakeRepository.decksList.add(deckInitial)
        fakeRepository.cardsList.addAll(cards)
        fakeRepository.studyLogs.add(CardStudyLog(id = "l1", cardId = "c1", isCorrect = true, studiedAt = 1000L))
        fakeRepository.studyLogs.add(CardStudyLog(id = "l2", cardId = "c2", isCorrect = true, studiedAt = 2000L))
        fakeRepository.triggerDecksUpdate()

        composeTestRule.setContent {
            DeckListScreen(
                subjectName = "英単語",
                viewModel = viewModel,
                onBack = {},
                onPartClick = { _, _ -> },
                onSpeakText = {},
            )
        }
        composeTestRule.waitForIdle()

        // 2/4 cards learned -> 50%
        composeTestRule.onNodeWithText("進捗率: 50%").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 / 4 単語").assertIsDisplayed()

        // Now change splitSize to 4 (combine into 1 part)
        fakeRepository.decksList.clear()
        fakeRepository.decksList.add(deckInitial.copy(splitSize = 4))
        fakeRepository.triggerDecksUpdate()
        composeTestRule.waitForIdle()

        // Progress rate MUST stay 50% even after changing splitSize!
        composeTestRule.onNodeWithText("進捗率: 50%").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 / 4 単語").assertIsDisplayed()
    }

    private class FakeMasteryRepository : WordsRepository {
        val cardsList = mutableListOf<Card>()
        val decksList = mutableListOf<Deck>()
        val decksFlow = MutableStateFlow<List<Deck>>(emptyList())
        val sessionsMap = mutableMapOf<String, StudySession>()
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

        override suspend fun getRecentLogsForCard(cardId: String, limit: Int): List<CardStudyLog> =
            studyLogs.filter { it.cardId == cardId }.sortedByDescending { it.studiedAt }.take(limit)

        fun triggerDecksUpdate() {
            decksFlow.value = decksList.toList()
        }

        override fun getAllSubjects(): Flow<List<Subject>> = MutableStateFlow(emptyList())
        override suspend fun getSubjectById(id: String): Subject? = null
        override suspend fun saveSubject(subject: Subject) {}
        override suspend fun deleteSubject(id: String) {}

        override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> = decksFlow
        override suspend fun getDeckById(deckId: String): Deck? = decksList.find { it.id == deckId }
        override suspend fun getOrCreateBookmarkDeck(subjectId: String): Deck =
            Deck(id = "bookmark_$subjectId", subjectId = subjectId, name = "苦手単語")
        override suspend fun saveDeck(deck: Deck) {
            decksList.add(deck)
        }
        override suspend fun deleteDeck(deckId: String) {
            decksList.removeIf { it.id == deckId }
        }

        override fun getCardsByDeck(deckId: String): Flow<List<Card>> = MutableStateFlow(cardsList)
        override suspend fun getCardsByDeckSync(deckId: String): List<Card> = cardsList
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
            val idx = cardsList.indexOfFirst { it.id == cardId }
            if (idx != -1) {
                cardsList[idx] = cardsList[idx].copy(isBookmarked = isBookmarked)
            }
        }
        override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> = MutableStateFlow(emptyList())

        override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> = emptyList()
        override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? = null
        override suspend fun getAllCardsInSubject(subjectId: String): List<Card> = cardsList

        override suspend fun getSession(deckId: String): StudySession? = sessionsMap[deckId]
        override fun getSessionFlow(deckId: String): Flow<StudySession?> = MutableStateFlow(sessionsMap[deckId])
        override suspend fun saveSession(session: StudySession) {
            sessionsMap[session.deckId] = session
        }
        override suspend fun deleteSession(deckId: String) {
            sessionsMap.remove(deckId)
        }
    }
}
