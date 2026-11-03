package com.aidomari.app.ui.subject

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SubjectListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var fakeRepository: FakeSubjectScreenRepository
    private lateinit var viewModel: SubjectViewModel

    @Before
    fun setup() {
        fakeRepository = FakeSubjectScreenRepository()
        viewModel = SubjectViewModel(fakeRepository)
    }

    @Test
    fun subjectListScreen_displaysKanjiAppNameTitle() {
        composeTestRule.setContent {
            SubjectListScreen(
                viewModel = viewModel,
                onSubjectClick = { _, _ -> },
            )
        }

        // Verify the Kanji application title "相泊" is displayed
        composeTestRule.onNodeWithText("相泊").assertIsDisplayed()
    }

    @Test
    fun activeSubjectRow_doesNotHaveDirectDeleteButton_andHasArchiveInMoreMenu() {
        val subject = Subject(id = "sub-1", name = "物理基礎")
        fakeRepository.seedSubjects(listOf(subject))

        composeTestRule.setContent {
            SubjectListScreen(
                viewModel = viewModel,
                onSubjectClick = { _, _ -> },
            )
        }

        // Direct delete icon should NOT exist
        composeTestRule.onNodeWithContentDescription("Delete Subject").assertDoesNotExist()

        // 3-dot menu should have "教科をアーカイブ"
        composeTestRule.onNodeWithTag("SubjectMoreOptionsButton_sub-1").performClick()
        composeTestRule.onNodeWithText("教科をアーカイブ").assertIsDisplayed()
    }

    @Test
    fun archiveSubject_andNavigateToArchivedView() {
        val subject = Subject(id = "sub-1", name = "物理基礎")
        fakeRepository.seedSubjects(listOf(subject))

        composeTestRule.setContent {
            SubjectListScreen(
                viewModel = viewModel,
                onSubjectClick = { _, _ -> },
            )
        }

        // Archive the subject
        composeTestRule.onNodeWithTag("SubjectMoreOptionsButton_sub-1").performClick()
        composeTestRule.onNodeWithText("教科をアーカイブ").performClick()
        composeTestRule.waitForIdle()

        // Should see archived section link
        composeTestRule.onNodeWithTag("ArchivedSubjectsLink").performClick()
        composeTestRule.waitForIdle()

        // In archived view
        composeTestRule.onNodeWithText("アーカイブ済み教科").assertIsDisplayed()
        composeTestRule.onNodeWithText("物理基礎").assertIsDisplayed()
        composeTestRule.onNodeWithText("アーカイブ済み").assertIsDisplayed()

        // Unarchive
        composeTestRule.onNodeWithTag("ArchivedSubjectMoreOptionsButton_sub-1").performClick()
        composeTestRule.onNodeWithText("元に戻す").performClick()
        composeTestRule.waitForIdle()

        // Now archived view should be empty
        composeTestRule.onNodeWithText("アーカイブされた教科はありません").assertIsDisplayed()
    }
}

private class FakeSubjectScreenRepository : WordsRepository {
    private val subjectsList = mutableListOf<Subject>()
    private val subjectsFlow = MutableStateFlow<List<Subject>>(emptyList())

    fun seedSubjects(subjects: List<Subject>) {
        subjectsList.clear()
        subjectsList.addAll(subjects)
        subjectsFlow.value = subjectsList.toList()
    }

    override fun getAllSubjects(): Flow<List<Subject>> = subjectsFlow
    override suspend fun getSubjectById(id: String): Subject? = subjectsList.find { it.id == id }
    override suspend fun saveSubject(subject: Subject) {
        subjectsList.removeIf { it.id == subject.id }
        subjectsList.add(subject)
        subjectsFlow.value = subjectsList.toList()
    }
    override suspend fun deleteSubject(id: String) {
        subjectsList.removeIf { it.id == id }
        subjectsFlow.value = subjectsList.toList()
    }

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
