package com.aidomari.app.ui.subject

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.aidomari.app.domain.model.Card
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
}

private class FakeSubjectScreenRepository : WordsRepository {
    private val subjectsFlow = MutableStateFlow<List<Subject>>(emptyList())

    override fun getAllSubjects(): Flow<List<Subject>> = subjectsFlow
    override suspend fun getSubjectById(id: String): Subject? = null
    override suspend fun saveSubject(subject: Subject) {}
    override suspend fun deleteSubject(id: String) {}

    override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> = MutableStateFlow(emptyList())
    override suspend fun getDeckById(id: String): Deck? = null
    override suspend fun saveDeck(deck: Deck) {}
    override suspend fun deleteDeck(id: String) {}

    override fun getCardsByDeck(deckId: String): Flow<List<Card>> = MutableStateFlow(emptyList())
    override fun getCardsBySubject(subjectId: String): Flow<List<Card>> = MutableStateFlow(emptyList())
    override fun getBookmarkedCards(subjectId: String): Flow<List<Card>> = MutableStateFlow(emptyList())
    override suspend fun getCardById(id: String): Card? = null
    override suspend fun saveCard(card: Card) {}
    override suspend fun insertCards(cards: List<Card>) {}
    override suspend fun deleteCard(id: String) {}
    override suspend fun updateBookmark(id: String, isBookmarked: Boolean) {}

    override suspend fun getSession(deckId: String): StudySession? = null
    override suspend fun saveSession(session: StudySession) {}
    override suspend fun clearSession(deckId: String) {}
    override fun getSessionsBySubject(subjectId: String): Flow<List<StudySession>> = MutableStateFlow(emptyList())
}
