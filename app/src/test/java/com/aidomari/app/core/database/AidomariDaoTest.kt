package com.aidomari.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aidomari.app.core.database.entity.CardEntity
import com.aidomari.app.core.database.entity.CardStudyLogEntity
import com.aidomari.app.core.database.entity.DeckEntity
import com.aidomari.app.core.database.entity.StudySessionEntity
import com.aidomari.app.core.database.entity.SubjectEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AidomariDaoTest {

    private lateinit var db: AidomariDatabase
    private lateinit var dao: AidomariDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AidomariDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.aidomariDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetSubject() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語", ttsLanguage = "en-US")
        dao.insertSubject(subject)

        val retrieved = dao.getSubjectById("sub-1")
        assertNotNull(retrieved)
        assertEquals("英語", retrieved?.name)
        assertEquals("en-US", retrieved?.ttsLanguage)
    }

    @Test
    fun insertDeckAndCards_cascadeDelete() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)

        val deck = DeckEntity(id = "deck-1", subjectId = "sub-1", name = "単語帳A", splitSize = 50)
        dao.insertDeck(deck)

        val card1 = CardEntity(id = "card-1", deckId = "deck-1", front = "apple", back = "りんご")
        val card2 = CardEntity(id = "card-2", deckId = "deck-1", front = "book", back = "本")
        dao.insertCards(listOf(card1, card2))

        val cards = dao.getCardsByDeckSync("deck-1")
        assertEquals(2, cards.size)

        // Delete subject -> deck & cards should cascade delete
        dao.deleteSubjectById("sub-1")
        val remainingDecks = dao.getDecksBySubject("sub-1").first()
        val remainingCards = dao.getCardsByDeckSync("deck-1")

        assertTrue(remainingDecks.isEmpty())
        assertTrue(remainingCards.isEmpty())
    }

    @Test
    fun updateSubject_doesNotCascadeDeleteDecksAndCards() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)

        val deck = DeckEntity(id = "deck-1", subjectId = "sub-1", name = "単語帳A", splitSize = 50)
        dao.insertDeck(deck)

        val card1 = CardEntity(id = "card-1", deckId = "deck-1", front = "apple", back = "りんご")
        val card2 = CardEntity(id = "card-2", deckId = "deck-1", front = "book", back = "本")
        dao.insertCards(listOf(card1, card2))

        // Update subject (e.g. archiving subject) using insertSubject (@Upsert)
        dao.insertSubject(subject.copy(isArchived = true))

        val updatedSubject = dao.getSubjectById("sub-1")
        assertNotNull(updatedSubject)
        assertTrue(updatedSubject?.isArchived == true)

        // Verify decks and cards are NOT deleted
        val remainingDecks = dao.getDecksBySubject("sub-1").first()
        val remainingCards = dao.getCardsByDeckSync("deck-1")
        assertEquals(1, remainingDecks.size)
        assertEquals(2, remainingCards.size)
    }

    @Test
    fun updateDeck_doesNotCascadeDeleteCards() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)

        val deck = DeckEntity(id = "deck-1", subjectId = "sub-1", name = "単語帳A", splitSize = 50)
        dao.insertDeck(deck)

        val card1 = CardEntity(id = "card-1", deckId = "deck-1", front = "apple", back = "りんご")
        val card2 = CardEntity(id = "card-2", deckId = "deck-1", front = "book", back = "本")
        dao.insertCards(listOf(card1, card2))

        // Update deck name using insertDeck (@Upsert)
        dao.insertDeck(deck.copy(name = "単語帳A（改）"))

        val updatedDeck = dao.getDeckById("deck-1")
        assertNotNull(updatedDeck)
        assertEquals("単語帳A（改）", updatedDeck?.name)

        // Verify cards are NOT deleted
        val remainingCards = dao.getCardsByDeckSync("deck-1")
        assertEquals(2, remainingCards.size)
    }

    @Test
    fun getDecksBySubject_excludesBookmarkDecks() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)

        val regularDeck = DeckEntity(id = "deck-1", subjectId = "sub-1", name = "単語帳A")
        val bookmarkDeck = DeckEntity(id = "bookmark_sub-1", subjectId = "sub-1", name = "苦手単語（ブックマーク）")
        dao.insertDeck(regularDeck)
        dao.insertDeck(bookmarkDeck)

        val decks = dao.getDecksBySubject("sub-1").first()
        assertEquals(1, decks.size)
        assertEquals("deck-1", decks[0].id)
    }

    @Test
    fun insertAndGetDeck_preservesTtsSettings() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)

        val deck = DeckEntity(
            id = "deck-tts",
            subjectId = "sub-1",
            name = "TTS Deck",
            autoSpeak = true,
            speechRate = 1.25f,
        )
        dao.insertDeck(deck)

        val decks = dao.getDecksBySubject("sub-1").first()
        assertEquals(1, decks.size)
        assertTrue(decks[0].autoSpeak)
        assertEquals(1.25f, decks[0].speechRate, 0.001f)
    }

    @Test
    fun bookmarkToggleAndFilter() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)
        val deck = DeckEntity(id = "deck-1", subjectId = "sub-1", name = "単語帳A")
        dao.insertDeck(deck)

        val card = CardEntity(id = "card-1", deckId = "deck-1", front = "difficult", back = "難しい", isBookmarked = false)
        dao.insertCard(card)

        dao.setBookmark("card-1", true)
        val bookmarked = dao.getBookmarkedCardsBySubject("sub-1").first()
        assertEquals(1, bookmarked.size)
        assertEquals("difficult", bookmarked[0].front)
    }

    @Test
    fun studySession_saveAndRestore() = runBlocking {
        val subject = SubjectEntity(id = "sub-1", name = "英語")
        dao.insertSubject(subject)
        val deck = DeckEntity(id = "deck-1", subjectId = "sub-1", name = "単語帳A")
        dao.insertDeck(deck)

        val session = StudySessionEntity(
            deckId = "deck-1",
            partIndex = 1,
            currentCardIndex = 15,
            queueCardIdsJson = "[\"c1\",\"c2\"]",
            retryCardIdsJson = "[\"c3\"]",
            isCompleted = false,
        )
        dao.insertOrUpdateSession(session)

        val restored = dao.getSession("deck-1")
        assertNotNull(restored)
        assertEquals(1, restored?.partIndex)
        assertEquals(15, restored?.currentCardIndex)
        assertEquals("[\"c1\",\"c2\"]", restored?.queueCardIdsJson)
    }

    @Test
    fun searchCardsInSubject_matchesFrontBackAndNotes() = runBlocking {
        val subject = SubjectEntity(id = "sub-search", name = "英語")
        dao.insertSubject(subject)
        val deck = DeckEntity(id = "deck-search", subjectId = "sub-search", name = "単語帳S")
        dao.insertDeck(deck)

        val c1 = CardEntity(id = "c1", deckId = "deck-search", front = "apple", back = "りんご")
        val c2 = CardEntity(
            id = "c2",
            deckId = "deck-search",
            front = "banana",
            back = "バナナ",
            userSupplement = "tropical fruit",
        )
        val c3 = CardEntity(
            id = "c3",
            deckId = "deck-search",
            front = "cat",
            back = "ねこ",
            userQuestion = "kitten?",
        )
        dao.insertCards(listOf(c1, c2, c3))

        val frontMatches = dao.searchCardsInSubject("sub-search", "app")
        assertEquals(1, frontMatches.size)
        assertEquals("apple", frontMatches[0].front)

        val backMatches = dao.searchCardsInSubject("sub-search", "バナナ")
        assertEquals(1, backMatches.size)
        assertEquals("banana", backMatches[0].front)

        val supplementMatches = dao.searchCardsInSubject("sub-search", "tropical")
        assertEquals(1, supplementMatches.size)
        assertEquals("banana", supplementMatches[0].front)

        val questionMatches = dao.searchCardsInSubject("sub-search", "kitten")
        assertEquals(1, questionMatches.size)
        assertEquals("cat", questionMatches[0].front)

        val emptyMatches = dao.searchCardsInSubject("sub-search", "zebra")
        assertTrue(emptyMatches.isEmpty())
    }

    @Test
    fun insertAndGetStudySession_withCompletedPartIndices() = runBlocking {
        val subject = SubjectEntity(id = "sub-session", name = "英語")
        dao.insertSubject(subject)
        val deck = DeckEntity(id = "deck-session", subjectId = "sub-session", name = "単語帳")
        dao.insertDeck(deck)

        val session = StudySessionEntity(
            deckId = "deck-session",
            partIndex = 1,
            isCompleted = false,
            completedPartIndicesJson = "[0,1]",
        )
        dao.insertOrUpdateSession(session)

        val retrieved = dao.getSession("deck-session")
        assertNotNull(retrieved)
        assertEquals("[0,1]", retrieved?.completedPartIndicesJson)
        assertEquals(1, retrieved?.partIndex)
        assertEquals(false, retrieved?.isCompleted)
    }

    @Test
    fun getRandomCard_excludesCardsFromArchivedSubjects() = runBlocking {
        // Active subject with card
        val activeSubject = SubjectEntity(id = "sub-active", name = "アクティブ教科", isArchived = false)
        val activeDeck = DeckEntity(id = "deck-active", subjectId = "sub-active", name = "アクティブデッキ")
        val activeCard = CardEntity(id = "card-active", deckId = "deck-active", front = "active", back = "有効")
        dao.insertSubject(activeSubject)
        dao.insertDeck(activeDeck)
        dao.insertCard(activeCard)

        // Archived subject with card
        val archivedSubject = SubjectEntity(id = "sub-archived", name = "アーカイブ教科", isArchived = true)
        val archivedDeck = DeckEntity(id = "deck-archived", subjectId = "sub-archived", name = "アーカイブデッキ")
        val archivedCard = CardEntity(id = "card-archived", deckId = "deck-archived", front = "archived", back = "保存")
        dao.insertSubject(archivedSubject)
        dao.insertDeck(archivedDeck)
        dao.insertCard(archivedCard)

        // Verify getRandomCard only returns the card from active subject
        val result = dao.getRandomCard()
        assertNotNull(result)
        assertEquals("card-active", result?.id)

        // Archive activeSubject as well
        dao.insertSubject(activeSubject.copy(isArchived = true))
        val emptyResult = dao.getRandomCard()
        assertTrue(emptyResult == null)
    }

    @Test
    fun getRandomBookmarkedCard_excludesCardsFromArchivedSubjects() = runBlocking {
        val activeSubject = SubjectEntity(id = "sub-bm-active", name = "アクティブ教科", isArchived = false)
        val activeDeck = DeckEntity(id = "deck-bm-active", subjectId = "sub-bm-active", name = "デッキ")
        val activeBmCard = CardEntity(id = "card-bm-active", deckId = "deck-bm-active", front = "active", back = "有効", isBookmarked = true)
        dao.insertSubject(activeSubject)
        dao.insertDeck(activeDeck)
        dao.insertCard(activeBmCard)

        val archivedSubject = SubjectEntity(id = "sub-bm-archived", name = "アーカイブ教科", isArchived = true)
        val archivedDeck = DeckEntity(id = "deck-bm-archived", subjectId = "sub-bm-archived", name = "デッキ")
        val archivedBmCard = CardEntity(id = "card-bm-archived", deckId = "deck-bm-archived", front = "archived", back = "保存", isBookmarked = true)
        dao.insertSubject(archivedSubject)
        dao.insertDeck(archivedDeck)
        dao.insertCard(archivedBmCard)

        val result = dao.getRandomBookmarkedCard()
        assertNotNull(result)
        assertEquals("card-bm-active", result?.id)
    }

    @Test
    fun insertAndGetRecentLogsForCard() = runBlocking {
        val subject = SubjectEntity(id = "sub-log", name = "英語")
        val deck = DeckEntity(id = "deck-log", subjectId = "sub-log", name = "単語帳")
        val card = CardEntity(id = "card-log-1", deckId = "deck-log", front = "apple", back = "りんご")
        dao.insertSubject(subject)
        dao.insertDeck(deck)
        dao.insertCard(card)

        // Insert multiple study logs
        dao.insertStudyLog(CardStudyLogEntity(id = "log-1", cardId = "card-log-1", isCorrect = false, studiedAt = 1000L))
        dao.insertStudyLog(CardStudyLogEntity(id = "log-2", cardId = "card-log-1", isCorrect = true, studiedAt = 2000L))
        dao.insertStudyLog(CardStudyLogEntity(id = "log-3", cardId = "card-log-1", isCorrect = true, studiedAt = 3000L))

        val recentLogs = dao.getRecentLogsForCard("card-log-1", limit = 2)
        assertEquals(2, recentLogs.size)
        // Should be ordered by studied_at DESC
        assertEquals("log-3", recentLogs[0].id)
        assertEquals("log-2", recentLogs[1].id)

        val totalReviews = dao.getReviewCountForCard("card-log-1")
        val totalCorrect = dao.getCorrectCountForCard("card-log-1")
        assertEquals(3, totalReviews)
        assertEquals(2, totalCorrect)
    }

    @Test
    fun getLatestLogsForDeckSync_returnsOnlyLatestLogPerCard() = runBlocking {
        val subject = SubjectEntity(id = "sub-latest", name = "英語")
        val deck = DeckEntity(id = "deck-latest", subjectId = "sub-latest", name = "単語帳")
        val card1 = CardEntity(id = "card-1", deckId = "deck-latest", front = "apple", back = "りんご")
        val card2 = CardEntity(id = "card-2", deckId = "deck-latest", front = "banana", back = "バナナ")
        dao.insertSubject(subject)
        dao.insertDeck(deck)
        dao.insertCards(listOf(card1, card2))

        // card1: answered twice, latest is correct
        dao.insertStudyLog(CardStudyLogEntity(id = "l1", cardId = "card-1", isCorrect = false, studiedAt = 1000L))
        dao.insertStudyLog(CardStudyLogEntity(id = "l2", cardId = "card-1", isCorrect = true, studiedAt = 2000L))

        // card2: answered once, incorrect
        dao.insertStudyLog(CardStudyLogEntity(id = "l3", cardId = "card-2", isCorrect = false, studiedAt = 1500L))

        val latestLogs = dao.getLatestLogsForDeckSync("deck-latest")
        assertEquals(2, latestLogs.size)

        val c1Log = latestLogs.find { it.cardId == "card-1" }
        val c2Log = latestLogs.find { it.cardId == "card-2" }
        assertNotNull(c1Log)
        assertNotNull(c2Log)
        assertEquals("l2", c1Log?.id)
        assertTrue(c1Log?.isCorrect == true)
        assertEquals("l3", c2Log?.id)
        assertTrue(c2Log?.isCorrect == false)
    }

    @Test
    fun deleteCard_cascadeDeletesCardStudyLogs() = runBlocking {
        val subject = SubjectEntity(id = "sub-cas", name = "英語")
        val deck = DeckEntity(id = "deck-cas", subjectId = "sub-cas", name = "単語帳")
        val card = CardEntity(id = "card-cas", deckId = "deck-cas", front = "apple", back = "りんご")
        dao.insertSubject(subject)
        dao.insertDeck(deck)
        dao.insertCard(card)

        dao.insertStudyLog(CardStudyLogEntity(id = "log-cas", cardId = "card-cas", isCorrect = true, studiedAt = 1000L))
        assertEquals(1, dao.getReviewCountForCard("card-cas"))

        // Delete card -> log should be deleted by cascade
        dao.deleteCard(card)
        assertEquals(0, dao.getReviewCountForCard("card-cas"))
    }

    @Test
    fun getRandomCardOverDaysAgo_returnsOnlyOldStudiedCards_excludesUnstudiedRecentAndArchived() = runBlocking {
        val now = 100_000L
        val cutoff = now - 30_000L // 70_000L

        // Active Subject
        val activeSubject = SubjectEntity(id = "sub-active", name = "英語", isArchived = false)
        val activeDeck = DeckEntity(id = "deck-active", subjectId = "sub-active", name = "単語帳")
        dao.insertSubject(activeSubject)
        dao.insertDeck(activeDeck)

        // card-unstudied: ログなし（未学習・新規）
        val unstudiedCard = CardEntity(id = "c-unstudied", deckId = "deck-active", front = "new", back = "新しい")
        // card-old: 過去に学習済み（studiedAt = 50_000L <= cutoff）
        val oldCard = CardEntity(id = "c-old", deckId = "deck-active", front = "old", back = "古い")
        // card-recent: 最近学習済み（studiedAt = 90_000L > cutoff）
        val recentCard = CardEntity(id = "c-recent", deckId = "deck-active", front = "recent", back = "最近")
        dao.insertCards(listOf(unstudiedCard, oldCard, recentCard))

        dao.insertStudyLog(CardStudyLogEntity(id = "log-old", cardId = "c-old", isCorrect = true, studiedAt = 50_000L))
        dao.insertStudyLog(CardStudyLogEntity(id = "log-recent", cardId = "c-recent", isCorrect = true, studiedAt = 90_000L))

        // Archived Subject
        val archivedSubject = SubjectEntity(id = "sub-arch", name = "保存教科", isArchived = true)
        val archivedDeck = DeckEntity(id = "deck-arch", subjectId = "sub-arch", name = "保存デッキ")
        val archivedCard = CardEntity(id = "c-arch", deckId = "deck-arch", front = "archived", back = "保存")
        dao.insertSubject(archivedSubject)
        dao.insertDeck(archivedDeck)
        dao.insertCard(archivedCard)
        dao.insertStudyLog(CardStudyLogEntity(id = "log-arch", cardId = "c-arch", isCorrect = true, studiedAt = 40_000L))

        // c-old のみが返ること（c-unstudied, c-recent, c-arch は除外される）
        val card = dao.getRandomCardOverDaysAgo(cutoff)
        assertNotNull(card)
        assertEquals("c-old", card?.id)
    }

    @Test
    fun getOldestStudiedCard_returnsCardWithOldestMaxStudiedAt_excludesArchivedAndUnstudied() = runBlocking {
        val activeSubject = SubjectEntity(id = "sub-oldest", name = "英語", isArchived = false)
        val activeDeck = DeckEntity(id = "deck-oldest", subjectId = "sub-oldest", name = "単語帳")
        dao.insertSubject(activeSubject)
        dao.insertDeck(activeDeck)

        val unstudied = CardEntity(id = "c-unstudied", deckId = "deck-oldest", front = "unstudied", back = "未学習")
        val old1 = CardEntity(id = "c-old1", deckId = "deck-oldest", front = "older", back = "より古い")
        val old2 = CardEntity(id = "c-old2", deckId = "deck-oldest", front = "newer", back = "より新しい")
        dao.insertCards(listOf(unstudied, old1, old2))

        // old1: last studied at 1000L
        dao.insertStudyLog(CardStudyLogEntity(id = "l1", cardId = "c-old1", isCorrect = true, studiedAt = 1000L))
        // old2: last studied at 5000L
        dao.insertStudyLog(CardStudyLogEntity(id = "l2", cardId = "c-old2", isCorrect = true, studiedAt = 5000L))

        val oldestCard = dao.getOldestStudiedCard()
        assertNotNull(oldestCard)
        assertEquals("c-old1", oldestCard?.id)
    }

    @Test
    fun getOldestStudiedCard_whenNoCardsStudied_returnsNull() = runBlocking {
        val subject = SubjectEntity(id = "sub-none", name = "英語", isArchived = false)
        val deck = DeckEntity(id = "deck-none", subjectId = "sub-none", name = "単語帳")
        val unstudied = CardEntity(id = "c-none", deckId = "deck-none", front = "unstudied", back = "未学習")
        dao.insertSubject(subject)
        dao.insertDeck(deck)
        dao.insertCard(unstudied)

        val card = dao.getOldestStudiedCard()
        assertTrue(card == null)
    }

    @Test
    fun getDecksBySubject_excludesPastDayVirtualDecks() = runBlocking {
        val subject = SubjectEntity(id = "sub-virtual", name = "英語")
        dao.insertSubject(subject)

        val normalDeck = DeckEntity(id = "deck-normal", subjectId = "sub-virtual", name = "通常デッキ")
        val bookmarkDeck = DeckEntity(id = "bookmark_sub-virtual", subjectId = "sub-virtual", name = "苦手単語")
        val randomDeck = DeckEntity(id = "random_10_sub-virtual", subjectId = "sub-virtual", name = "ランダム出題")
        val pastYesterdayDeck = DeckEntity(id = "past_yesterday_sub-virtual", subjectId = "sub-virtual", name = "昨日のカード")
        val pastWeekDeck = DeckEntity(id = "past_week_sub-virtual", subjectId = "sub-virtual", name = "先週のカード")
        val pastMonthDeck = DeckEntity(id = "past_month_sub-virtual", subjectId = "sub-virtual", name = "先月のカード")

        dao.insertDecks(listOf(normalDeck, bookmarkDeck, randomDeck, pastYesterdayDeck, pastWeekDeck, pastMonthDeck))

        val decks = dao.getDecksBySubject("sub-virtual").first()
        assertEquals(1, decks.size)
        assertEquals("deck-normal", decks[0].id)
    }

    @Test
    fun getCardsWithLatestStudyTimeBySubject_returnsLatestTimestampPerCardInSubject() = runBlocking {
        val subject1 = SubjectEntity(id = "sub-p1", name = "英語")
        val subject2 = SubjectEntity(id = "sub-p2", name = "数学")
        dao.insertSubjects(listOf(subject1, subject2))

        val deck1 = DeckEntity(id = "deck-p1", subjectId = "sub-p1", name = "英単語")
        val deck2 = DeckEntity(id = "deck-p2", subjectId = "sub-p2", name = "数式")
        dao.insertDecks(listOf(deck1, deck2))

        val card1 = CardEntity(id = "c1", deckId = "deck-p1", front = "apple", back = "りんご")
        val card2 = CardEntity(id = "c2", deckId = "deck-p1", front = "book", back = "本")
        val unstudiedCard = CardEntity(id = "c3", deckId = "deck-p1", front = "cat", back = "猫")
        val otherSubjectCard = CardEntity(id = "c4", deckId = "deck-p2", front = "formula", back = "公式")
        dao.insertCards(listOf(card1, card2, unstudiedCard, otherSubjectCard))

        // card1 has two study logs: 1000L and 3000L -> latest is 3000L
        dao.insertStudyLog(CardStudyLogEntity(id = "log-1", cardId = "c1", isCorrect = true, studiedAt = 1000L))
        dao.insertStudyLog(CardStudyLogEntity(id = "log-2", cardId = "c1", isCorrect = false, studiedAt = 3000L))

        // card2 has one study log: 2000L
        dao.insertStudyLog(CardStudyLogEntity(id = "log-3", cardId = "c2", isCorrect = true, studiedAt = 2000L))

        // otherSubjectCard has log: 5000L
        dao.insertStudyLog(CardStudyLogEntity(id = "log-4", cardId = "c4", isCorrect = true, studiedAt = 5000L))

        val results = dao.getCardsWithLatestStudyTimeBySubject("sub-p1").first()
        val syncResults = dao.getCardsWithLatestStudyTimeBySubjectSync("sub-p1")

        assertEquals(2, results.size)
        assertEquals(2, syncResults.size)

        // Ordered by max_studied_at DESC -> c1 (3000L) then c2 (2000L)
        assertEquals("c1", results[0].card.id)
        assertEquals(3000L, results[0].maxStudiedAt)

        assertEquals("c2", results[1].card.id)
        assertEquals(2000L, results[1].maxStudiedAt)
    }
}
