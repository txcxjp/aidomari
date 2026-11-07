package com.aidomari.app.ui.subject

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
class SubjectViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSubjectRepository
    private lateinit var viewModel: SubjectViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSubjectRepository()
        viewModel = SubjectViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createSubject_savesSubjectToRepository() = runTest {
        viewModel.createSubject("英単語", "en-US")
        advanceUntilIdle()

        assertEquals(1, fakeRepository.subjectsList.size)
        assertEquals("英単語", fakeRepository.subjectsList[0].name)
        assertEquals("en-US", fakeRepository.subjectsList[0].ttsLanguage)
    }

    @Test
    fun createSubject_withColorTheme_savesColorTheme() = runTest {
        viewModel.createSubject("世界史", "ja-JP", "AMBER")
        advanceUntilIdle()

        assertEquals(1, fakeRepository.subjectsList.size)
        assertEquals("世界史", fakeRepository.subjectsList[0].name)
        assertEquals("AMBER", fakeRepository.subjectsList[0].colorTheme)
    }

    @Test
    fun updateSubject_updatesColorTheme() = runTest {
        viewModel.createSubject("数学", "ja-JP", "FOREST")
        advanceUntilIdle()

        val id = fakeRepository.subjectsList[0].id
        viewModel.updateSubject(id, "数学II・B", "ja-JP", "SAKURA")
        advanceUntilIdle()

        val updated = fakeRepository.subjectsList[0]
        assertEquals("数学II・B", updated.name)
        assertEquals("SAKURA", updated.colorTheme)
    }

    @Test
    fun deleteSubject_removesSubjectFromRepository() = runTest {
        viewModel.createSubject("世界史", "ja-JP")
        advanceUntilIdle()

        val id = fakeRepository.subjectsList[0].id
        viewModel.deleteSubject(id)
        advanceUntilIdle()

        assertTrue(fakeRepository.subjectsList.isEmpty())
    }

    @Test
    fun importData_importsJsonSuccessfully() = runTest {
        val json = """
            [
              {
                "title": "IT用語",
                "content": [
                  { "front": "API", "back": "アプリケーションプログラミングインタフェース" }
                ]
              }
            ]
        """.trimIndent()

        var success = false
        viewModel.importData(json) { result ->
            success = result.isSuccess
        }
        advanceUntilIdle()

        assertTrue(success)
        assertTrue(fakeRepository.subjectsList.isNotEmpty())
        assertEquals(1, fakeRepository.decksList.size)
        assertEquals("IT用語", fakeRepository.decksList[0].name)
    }

    @Test
    fun exportData_returnsValidExportJson() = runTest {
        viewModel.createSubject("テスト教科", "ja-JP")
        advanceUntilIdle()

        val json = viewModel.exportData()
        assertTrue(json.contains("テスト教科"))
    }

    @Test
    fun archiveAndUnarchiveSubject_separatesActiveAndArchivedLists() = runTest {
        viewModel.createSubject("英語", "en-US")
        viewModel.createSubject("数学", "ja-JP")
        advanceUntilIdle()

        val englishId = fakeRepository.subjectsList.find { it.name == "英語" }!!.id
        val mathId = fakeRepository.subjectsList.find { it.name == "数学" }!!.id

        // Archive English
        viewModel.archiveSubject(englishId)
        advanceUntilIdle()

        assertEquals(1, viewModel.subjects.value.size)
        assertEquals("数学", viewModel.subjects.value[0].name)
        assertEquals(1, viewModel.archivedSubjects.value.size)
        assertEquals("英語", viewModel.archivedSubjects.value[0].name)

        // Unarchive English
        viewModel.unarchiveSubject(englishId)
        advanceUntilIdle()

        assertEquals(2, viewModel.subjects.value.size)
        assertEquals(0, viewModel.archivedSubjects.value.size)
    }

    @Test
    fun updateSubject_updatesNameAndTtsLanguage() = runTest {
        viewModel.createSubject("英語", "en-US")
        advanceUntilIdle()

        val id = fakeRepository.subjectsList[0].id
        viewModel.updateSubject(id, "英語（上級）", "ja-JP")
        advanceUntilIdle()

        val updated = fakeRepository.getSubjectById(id)
        assertEquals("英語（上級）", updated?.name)
        assertEquals("ja-JP", updated?.ttsLanguage)
    }

    private class FakeSubjectRepository : WordsRepository {
        val subjectsList = mutableListOf<Subject>()
        val decksList = mutableListOf<Deck>()
        val cardsList = mutableListOf<Card>()
        val subjectsFlow = MutableStateFlow<List<Subject>>(emptyList())

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

        override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> =
            MutableStateFlow(decksList.filter { it.subjectId == subjectId })
        override suspend fun getDeckById(deckId: String): Deck? = decksList.find { it.id == deckId }
        override suspend fun saveDeck(deck: Deck) {
            decksList.add(deck)
        }
        override suspend fun deleteDeck(deckId: String) {
            decksList.removeIf { it.id == deckId }
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

        override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> =
            MutableStateFlow(emptyList())

        override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> = emptyList()

        override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? = null

        override suspend fun getAllCardsInSubject(subjectId: String): List<Card> = cardsList

        override suspend fun getSession(deckId: String): StudySession? = null

        override fun getSessionFlow(deckId: String): Flow<StudySession?> = MutableStateFlow(null)

        override suspend fun saveSession(session: StudySession) {
        }

        override suspend fun deleteSession(deckId: String) {
        }
    }
}
