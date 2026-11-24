package com.aidomari.app.ui.digest

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.aidomari.app.core.ai.GeminiApiClient
import com.aidomari.app.core.ai.GeminiPreferences
import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.model.ai.CardExtractionMode
import com.aidomari.app.domain.model.ai.GeneratedCardProposal
import com.aidomari.app.domain.model.article.ArticleStatus
import com.aidomari.app.domain.model.article.SourceArticle
import com.aidomari.app.domain.repository.SourceArticleRepository
import com.aidomari.app.domain.repository.WordsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BrowsingDigestViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var fakeArticleRepository: FakeSourceArticleRepository
    private lateinit var fakeWordsRepository: FakeWordsRepository
    private lateinit var fakeGeminiApiClient: FakeGeminiApiClient
    private lateinit var geminiPreferences: GeminiPreferences
    private lateinit var viewModel: BrowsingDigestViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("aidomari_gemini_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        geminiPreferences = GeminiPreferences(context)
        fakeArticleRepository = FakeSourceArticleRepository()
        fakeWordsRepository = FakeWordsRepository()
        fakeGeminiApiClient = FakeGeminiApiClient()

        viewModel = BrowsingDigestViewModel(
            sourceArticleRepository = fakeArticleRepository,
            wordsRepository = fakeWordsRepository,
            geminiApiClient = fakeGeminiApiClient,
            geminiPreferences = geminiPreferences,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsArticlesAndPendingCount() = runTest {
        val article1 = SourceArticle(id = "1", url = "https://example.com/1", domain = "example.com", title = "記事1", extractedText = "本文1", status = ArticleStatus.PENDING)
        val article2 = SourceArticle(id = "2", url = "https://example.com/2", domain = "example.com", title = "記事2", extractedText = "本文2", status = ArticleStatus.PENDING)
        val article3 = SourceArticle(id = "3", url = "https://example.com/3", domain = "example.com", title = "記事3", extractedText = "本文3", status = ArticleStatus.GENERATED, generatedCardCount = 5)

        fakeArticleRepository.setArticles(listOf(article1, article2, article3))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.articles.size)
        assertEquals(2, state.pendingCount)
        assertEquals(2, state.filteredArticles.size)
    }

    @Test
    fun filteredArticles_onlyIncludesPendingArticles() = runTest {
        val pendingArticle = SourceArticle(id = "1", url = "https://example.com/1", domain = "example.com", title = "未処理記事", extractedText = "本文", status = ArticleStatus.PENDING)
        val generatedArticle = SourceArticle(id = "2", url = "https://example.com/2", domain = "example.com", title = "生成済記事", extractedText = "本文", status = ArticleStatus.GENERATED, generatedCardCount = 3)

        fakeArticleRepository.setArticles(listOf(pendingArticle, generatedArticle))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.filteredArticles.size)
        assertEquals("未処理記事", viewModel.uiState.value.filteredArticles[0].title)
    }

    @Test
    fun generateCards_failsWhenGeminiApiKeyNotConfigured() = runTest {
        geminiPreferences.clearApiKey() // 未設定
        val article = SourceArticle(id = "1", url = "https://example.com/1", domain = "example.com", title = "テスト記事", extractedText = "本文", status = ArticleStatus.PENDING)

        viewModel.generateCards(article)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("APIキーが設定されていません"))
        assertFalse(viewModel.uiState.value.isGenerating)
        assertNull(viewModel.uiState.value.reviewArticle)
    }

    @Test
    fun generateCards_populatesCandidateCardsOnSuccess() = runTest {
        geminiPreferences.setApiKey("valid_api_key")
        fakeGeminiApiClient.stubbedProposals = listOf(
            GeneratedCardProposal(front = "問題1", back = "解答1", userSupplement = "補足1", sourceUrl = "https://example.com/1", sourceTitle = "テスト記事"),
            GeneratedCardProposal(front = "問題2", back = "解答2", userSupplement = "補足2", sourceUrl = "https://example.com/1", sourceTitle = "テスト記事"),
        )
        val article = SourceArticle(id = "1", url = "https://example.com/1", domain = "example.com", title = "テスト記事", extractedText = "本文", status = ArticleStatus.PENDING)

        viewModel.generateCards(article)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGenerating)
        assertNull(state.errorMessage)
        assertNotNull(state.reviewArticle)
        assertEquals("テスト記事", state.reviewArticle!!.title)
        assertEquals(2, state.candidateCards.size)
        assertEquals("問題1", state.candidateCards[0].front)
        assertEquals("解答1", state.candidateCards[0].back)
        assertTrue(state.candidateCards[0].isSelected)
        assertTrue(state.candidateCards[1].isSelected)
        assertEquals(CardExtractionMode.SMART, fakeGeminiApiClient.lastMode)
    }

    @Test
    fun selectMode_and_generateCards_passesSpecifiedMode() = runTest {
        geminiPreferences.setApiKey("valid_api_key")
        val article = SourceArticle(id = "1", url = "https://example.com", domain = "example.com", title = "記事", extractedText = "本文", status = ArticleStatus.PENDING)

        // デフォルトは SMART
        assertEquals(CardExtractionMode.SMART, viewModel.uiState.value.selectedMode)

        // KNOWLEDGEに変更
        viewModel.selectMode(CardExtractionMode.KNOWLEDGE)
        assertEquals(CardExtractionMode.KNOWLEDGE, viewModel.uiState.value.selectedMode)

        viewModel.generateCards(article)
        advanceUntilIdle()

        assertEquals(CardExtractionMode.KNOWLEDGE, fakeGeminiApiClient.lastMode)

        // ダイアログ内で CORE に変更して再生成
        viewModel.generateCards(article, mode = CardExtractionMode.CORE)
        advanceUntilIdle()

        assertEquals(CardExtractionMode.CORE, fakeGeminiApiClient.lastMode)
        assertEquals(CardExtractionMode.CORE, viewModel.uiState.value.selectedMode)
    }

    @Test
    fun toggleCardSelection_and_selectAll_worksCorrectly() = runTest {
        geminiPreferences.setApiKey("valid_api_key")
        fakeGeminiApiClient.stubbedProposals = listOf(
            GeneratedCardProposal(front = "Q1", back = "A1"),
            GeneratedCardProposal(front = "Q2", back = "A2"),
        )
        val article = SourceArticle(id = "1", url = "https://example.com", domain = "example.com", title = "記事", extractedText = "本文", status = ArticleStatus.PENDING)

        viewModel.generateCards(article)
        advanceUntilIdle()

        val card1Id = viewModel.uiState.value.candidateCards[0].id
        viewModel.toggleCardSelection(card1Id)
        assertFalse(viewModel.uiState.value.candidateCards[0].isSelected)
        assertTrue(viewModel.uiState.value.candidateCards[1].isSelected)
        assertEquals(1, viewModel.uiState.value.selectedCandidateCount)

        // 全解除
        viewModel.selectAllCandidates(false)
        assertEquals(0, viewModel.uiState.value.selectedCandidateCount)

        // 全選択
        viewModel.selectAllCandidates(true)
        assertEquals(2, viewModel.uiState.value.selectedCandidateCount)
    }

    @Test
    fun generateCards_updatesProgressMessageAndFractionDuringExecution() = runTest {
        geminiPreferences.setApiKey("valid_api_key")
        val article = SourceArticle(id = "1", url = "https://example.com", domain = "example.com", title = "記事", extractedText = "本文", status = ArticleStatus.PENDING)

        var capturedProgressMessage: String? = null
        var capturedProgressFraction: Float? = null

        fakeGeminiApiClient.onProgressInvoked = {
            capturedProgressMessage = viewModel.uiState.value.generationProgressMessage
            capturedProgressFraction = viewModel.uiState.value.generationProgressFraction
        }

        viewModel.generateCards(article)
        advanceUntilIdle()

        assertNotNull(capturedProgressMessage)
        assertTrue(capturedProgressMessage!!.contains("主要テーマ"))
        assertNotNull(capturedProgressFraction)
        assertTrue(capturedProgressFraction!! > 0f)
        assertNull(viewModel.uiState.value.generationProgressMessage)
        assertNull(viewModel.uiState.value.generationProgressFraction)
        assertFalse(viewModel.uiState.value.isGenerating)
    }

    @Test
    fun generateCards_clearsProgressOnFailure() = runTest {
        geminiPreferences.setApiKey("valid_api_key")
        fakeGeminiApiClient.shouldFail = true
        val article = SourceArticle(id = "1", url = "https://example.com", domain = "example.com", title = "記事", extractedText = "本文", status = ArticleStatus.PENDING)

        viewModel.generateCards(article)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isGenerating)
        assertNull(viewModel.uiState.value.generationProgressMessage)
        assertNull(viewModel.uiState.value.generationProgressFraction)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun updateCandidate_updatesContent() = runTest {
        geminiPreferences.setApiKey("valid_api_key")
        fakeGeminiApiClient.stubbedProposals = listOf(
            GeneratedCardProposal(front = "旧表面", back = "旧裏面", userSupplement = "旧補足"),
        )
        val article = SourceArticle(id = "1", url = "https://example.com", domain = "example.com", title = "記事", extractedText = "本文", status = ArticleStatus.PENDING)

        viewModel.generateCards(article)
        advanceUntilIdle()

        val cardId = viewModel.uiState.value.candidateCards[0].id
        viewModel.updateCandidate(cardId, "新表面", "新裏面", "新補足")

        val updated = viewModel.uiState.value.candidateCards[0]
        assertEquals("新表面", updated.front)
        assertEquals("新裏面", updated.back)
        assertEquals("新補足", updated.supplement)
    }

    @Test
    fun saveSelectedCards_persistsCardsAndUpdatesArticleStatus() = runTest {
        val subject = Subject(id = "sub1", name = "既存教科")
        fakeWordsRepository.saveSubject(subject)

        geminiPreferences.setApiKey("valid_api_key")
        fakeGeminiApiClient.stubbedProposals = listOf(
            GeneratedCardProposal(front = "Q1", back = "A1"),
            GeneratedCardProposal(front = "Q2", back = "A2"),
        )
        val article = SourceArticle(id = "art1", url = "https://example.com", domain = "example.com", title = "学習記事", extractedText = "本文", status = ArticleStatus.PENDING)
        fakeArticleRepository.setArticles(listOf(article))

        viewModel.generateCards(article)
        advanceUntilIdle()

        viewModel.selectSubject("sub1")
        viewModel.updateNewDeckName("テストデッキ")
        viewModel.saveSelectedCards()
        advanceUntilIdle()

        // 状態検証
        assertNull(viewModel.uiState.value.reviewArticle)
        assertTrue(viewModel.uiState.value.candidateCards.isEmpty())
        assertNotNull(viewModel.uiState.value.successMessage)
        assertTrue(viewModel.uiState.value.successMessage!!.contains("2 枚のカードを登録しました"))

        // リポジトリ検証
        assertEquals(1, fakeWordsRepository.decksList.size)
        assertEquals("テストデッキ", fakeWordsRepository.decksList[0].name)
        assertEquals("sub1", fakeWordsRepository.decksList[0].subjectId)
        assertEquals(2, fakeWordsRepository.cardsList.size)
        assertEquals("Q1", fakeWordsRepository.cardsList[0].front)
        assertEquals("Q2", fakeWordsRepository.cardsList[1].front)

        // 記事ステータスが GENERATED に更新されていること
        val updatedArticle = fakeArticleRepository.getArticleById("art1")
        assertNotNull(updatedArticle)
        assertEquals(ArticleStatus.GENERATED, updatedArticle!!.status)
        assertEquals(2, updatedArticle.generatedCardCount)
    }

    @Test
    fun deleteArticle_removesArticleFromRepository() = runTest {
        val article = SourceArticle(id = "art_del", url = "https://example.com", domain = "example.com", title = "削除記事", extractedText = "本文", status = ArticleStatus.PENDING)
        fakeArticleRepository.setArticles(listOf(article))
        advanceUntilIdle()

        viewModel.deleteArticle("art_del")
        advanceUntilIdle()

        assertNull(fakeArticleRepository.getArticleById("art_del"))
    }
}

// === Fakes ===

private class FakeSourceArticleRepository : SourceArticleRepository {
    private val articlesList = mutableListOf<SourceArticle>()
    private val articlesFlow = MutableStateFlow<List<SourceArticle>>(emptyList())

    fun setArticles(articles: List<SourceArticle>) {
        articlesList.clear()
        articlesList.addAll(articles)
        articlesFlow.value = articlesList.toList()
    }

    override fun getPendingArticles(): Flow<List<SourceArticle>> =
        MutableStateFlow(articlesList.filter { it.status == ArticleStatus.PENDING })

    override fun getAllArticles(): Flow<List<SourceArticle>> = articlesFlow.asStateFlow()

    override suspend fun getPendingArticlesSync(): List<SourceArticle> =
        articlesList.filter { it.status == ArticleStatus.PENDING }

    override suspend fun getArticleById(id: String): SourceArticle? =
        articlesList.find { it.id == id }

    override suspend fun ingestArticle(url: String, title: String, content: String): Result<SourceArticle> {
        val article = SourceArticle(id = UUID.randomUUID().toString(), url = url, domain = "example.com", title = title, extractedText = content)
        articlesList.add(article)
        articlesFlow.value = articlesList.toList()
        return Result.success(article)
    }

    override suspend fun updateArticleStatus(id: String, status: ArticleStatus, cardCount: Int) {
        val index = articlesList.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = articlesList[index]
            articlesList[index] = old.copy(status = status, generatedCardCount = cardCount)
            articlesFlow.value = articlesList.toList()
        }
    }

    override suspend fun deleteArticle(id: String) {
        articlesList.removeIf { it.id == id }
        articlesFlow.value = articlesList.toList()
    }

    override suspend fun cleanupOldArticles(textRetentionDays: Int, pendingRetentionDays: Int): com.aidomari.app.domain.repository.CleanupResult = com.aidomari.app.domain.repository.CleanupResult()
}

private class FakeWordsRepository : WordsRepository {
    val subjectsList = mutableListOf<Subject>()
    val decksList = mutableListOf<Deck>()
    val cardsList = mutableListOf<Card>()

    private val subjectsFlow = MutableStateFlow<List<Subject>>(emptyList())

    override fun getAllSubjects(): Flow<List<Subject>> = subjectsFlow.asStateFlow()
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
    override suspend fun setBookmark(cardId: String, isBookmarked: Boolean) {}

    override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> = MutableStateFlow(emptyList())
    override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> = emptyList()
    override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? = null
    override suspend fun getAllCardsInSubject(subjectId: String): List<Card> = cardsList
    override suspend fun getSession(deckId: String): StudySession? = null
    override fun getSessionFlow(deckId: String): Flow<StudySession?> = MutableStateFlow(null)
    override suspend fun saveSession(session: StudySession) {}
    override suspend fun deleteSession(deckId: String) {}
}

private class FakeGeminiApiClient : GeminiApiClient {
    var stubbedProposals: List<GeneratedCardProposal> = emptyList()
    var lastMode: CardExtractionMode? = null
    var shouldFail: Boolean = false
    var onProgressInvoked: (() -> Unit)? = null

    override suspend fun testConnection(apiKey: String, model: String): Result<Boolean> = Result.success(true)

    override suspend fun fetchAvailableModels(apiKey: String): Result<List<String>> =
        Result.success(listOf("gemini-3.8-flash", "gemini-2.5-flash"))

    override suspend fun extractThemes(
        title: String,
        url: String,
        content: String,
        apiKey: String,
        model: String,
        mode: CardExtractionMode,
    ): Result<List<String>> = Result.success(listOf("主要テーマ"))

    override suspend fun generateCardsForTheme(
        title: String,
        url: String,
        content: String,
        theme: String,
        apiKey: String,
        model: String,
        temperature: Float,
        mode: CardExtractionMode,
    ): Result<List<GeneratedCardProposal>> = Result.success(stubbedProposals)

    override suspend fun generateCardsFromArticle(
        title: String,
        url: String,
        content: String,
        apiKey: String,
        model: String,
        temperature: Float,
        mode: CardExtractionMode,
        onProgress: (com.aidomari.app.core.ai.GenerationProgress) -> Unit,
    ): Result<List<GeneratedCardProposal>> {
        lastMode = mode
        if (shouldFail) {
            return Result.failure(RuntimeException("Gemini API error"))
        }
        onProgress(com.aidomari.app.core.ai.GenerationProgress.AnalyzingThemes())
        onProgress(com.aidomari.app.core.ai.GenerationProgress.GeneratingThemeCards(1, 1, "主要テーマ", 0))
        onProgressInvoked?.invoke()
        onProgress(com.aidomari.app.core.ai.GenerationProgress.Deduplicating(stubbedProposals.size))
        return Result.success(stubbedProposals)
    }
}
