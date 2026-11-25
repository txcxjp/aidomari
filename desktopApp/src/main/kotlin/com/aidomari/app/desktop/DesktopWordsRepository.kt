package com.aidomari.app.desktop

import com.aidomari.app.domain.model.Card
import com.aidomari.app.domain.model.CardStudyLog
import com.aidomari.app.domain.model.Deck
import com.aidomari.app.domain.model.PastDayDeckType
import com.aidomari.app.domain.model.PastDayGroup
import com.aidomari.app.domain.model.StudyDirection
import com.aidomari.app.domain.model.StudySession
import com.aidomari.app.domain.model.Subject
import com.aidomari.app.domain.repository.WordsRepository
import com.aidomari.app.domain.util.CardSnoozeCalculator
import com.aidomari.app.domain.util.PastDayDecksCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types
import java.util.UUID

/**
 * SQLite-backed [WordsRepository] implementation for Compose Desktop.
 */
class DesktopWordsRepository(
    private val db: DesktopDatabase,
) : WordsRepository {

    override fun getAllSubjects(): Flow<List<Subject>> = reactiveQuery { conn ->
        conn.prepareStatement(
            "SELECT * FROM subjects ORDER BY sort_order ASC, created_at ASC",
        ).use { ps ->
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(rs.toSubject())
                }
            }
        }
    }

    override suspend fun getSubjectById(id: String): Subject? = db.read { conn ->
        conn.prepareStatement("SELECT * FROM subjects WHERE id = ?").use { ps ->
            ps.setString(1, id)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.toSubject() else null
            }
        }
    }

    override suspend fun saveSubject(subject: Subject) {
        db.write { conn ->
            conn.prepareStatement(
                """
                INSERT INTO subjects (id, name, tts_language, sort_order, created_at, is_archived, color_theme)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    name = excluded.name,
                    tts_language = excluded.tts_language,
                    sort_order = excluded.sort_order,
                    created_at = excluded.created_at,
                    is_archived = excluded.is_archived,
                    color_theme = excluded.color_theme
                """.trimIndent(),
            ).use { ps ->
                ps.setString(1, subject.id)
                ps.setString(2, subject.name)
                ps.setString(3, subject.ttsLanguage)
                ps.setInt(4, subject.sortOrder)
                ps.setLong(5, subject.createdAt)
                ps.setInt(6, if (subject.isArchived) 1 else 0)
                if (subject.colorTheme != null) {
                    ps.setString(7, subject.colorTheme)
                } else {
                    ps.setNull(7, Types.VARCHAR)
                }
                ps.executeUpdate()
            }
        }
    }

    override suspend fun deleteSubject(id: String) {
        db.write { conn ->
            conn.prepareStatement("DELETE FROM subjects WHERE id = ?").use { ps ->
                ps.setString(1, id)
                ps.executeUpdate()
            }
        }
    }

    override fun getDecksBySubject(subjectId: String): Flow<List<Deck>> = reactiveQuery { conn ->
        conn.prepareStatement(
            """
            SELECT * FROM decks
            WHERE subject_id = ?
              AND id NOT LIKE 'bookmark_%'
              AND id NOT LIKE 'random_%'
              AND id NOT LIKE 'past_%'
            ORDER BY sort_order ASC, created_at ASC
            """.trimIndent(),
        ).use { ps ->
            ps.setString(1, subjectId)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(rs.toDeck())
                }
            }
        }
    }

    override suspend fun getDeckById(deckId: String): Deck? = db.read { conn ->
        queryDeckById(conn, deckId)
    }

    override suspend fun getOrCreateBookmarkDeck(subjectId: String): Deck =
        getOrCreateVirtualDeck(
            id = "bookmark_$subjectId",
            subjectId = subjectId,
            name = "苦手単語（ブックマーク）",
            splitSize = 50,
            autoContinue = true,
            shuffleMode = false,
        )

    override suspend fun getOrCreateRandomDeck(subjectId: String): Deck =
        getOrCreateVirtualDeck(
            id = "random_$subjectId",
            subjectId = subjectId,
            name = "ランダム出題",
            splitSize = 10,
            autoContinue = false,
            shuffleMode = true,
        )

    override suspend fun getOrCreatePastDayDeck(subjectId: String, type: PastDayDeckType): Deck =
        getOrCreateVirtualDeck(
            id = "${type.idPrefix}$subjectId",
            subjectId = subjectId,
            name = type.title,
            splitSize = 50,
            autoContinue = true,
            shuffleMode = false,
        )

    private suspend fun getOrCreateVirtualDeck(
        id: String,
        subjectId: String,
        name: String,
        splitSize: Int,
        autoContinue: Boolean,
        shuffleMode: Boolean,
    ): Deck {
        val existing = getDeckById(id)
        if (existing != null) return existing
        val newDeck = Deck(
            id = id,
            subjectId = subjectId,
            name = name,
            splitSize = splitSize,
            autoContinue = autoContinue,
            shuffleMode = shuffleMode,
        )
        saveDeck(newDeck)
        return newDeck
    }

    override suspend fun saveDeck(deck: Deck) {
        db.write { conn ->
            upsertDeck(conn, deck)
        }
    }

    override suspend fun deleteDeck(deckId: String) {
        db.write { conn ->
            conn.prepareStatement("DELETE FROM decks WHERE id = ?").use { ps ->
                ps.setString(1, deckId)
                ps.executeUpdate()
            }
        }
    }

    override fun getCardsByDeck(deckId: String): Flow<List<Card>> = reactiveQuery { conn ->
        queryCardsByDeck(conn, deckId)
    }

    override suspend fun getCardsByDeckSync(deckId: String): List<Card> = db.read { conn ->
        queryCardsByDeck(conn, deckId)
    }

    override suspend fun getCardById(cardId: String): Card? = db.read { conn ->
        queryCardById(conn, cardId)
    }

    override suspend fun saveCard(card: Card) {
        db.write { conn ->
            upsertCard(conn, card)
        }
    }

    override suspend fun saveCards(cards: List<Card>) {
        if (cards.isEmpty()) return
        db.write { conn ->
            cards.forEach { card -> upsertCard(conn, card) }
        }
    }

    override suspend fun deleteCard(cardId: String) {
        db.write { conn ->
            conn.prepareStatement("DELETE FROM cards WHERE id = ?").use { ps ->
                ps.setString(1, cardId)
                ps.executeUpdate()
            }
        }
    }

    override suspend fun setBookmark(cardId: String, isBookmarked: Boolean) {
        db.write { conn ->
            conn.prepareStatement("UPDATE cards SET is_bookmarked = ? WHERE id = ?").use { ps ->
                ps.setInt(1, if (isBookmarked) 1 else 0)
                ps.setString(2, cardId)
                ps.executeUpdate()
            }
        }
    }

    override suspend fun snoozeCard(cardId: String): Card? {
        val card = getCardById(cardId) ?: return null
        val snoozeResult = CardSnoozeCalculator.calculateNextSnooze(card.snoozeLevel)
        val updated = card.copy(
            snoozedUntil = snoozeResult.snoozedUntil,
            snoozeLevel = snoozeResult.newLevel,
        )
        db.write { conn ->
            conn.prepareStatement(
                "UPDATE cards SET snoozed_until = ?, snooze_level = ? WHERE id = ?",
            ).use { ps ->
                ps.setLong(1, snoozeResult.snoozedUntil)
                ps.setInt(2, snoozeResult.newLevel)
                ps.setString(3, cardId)
                ps.executeUpdate()
            }
        }
        return updated
    }

    override suspend fun unsnoozeCard(cardId: String): Card? {
        val card = getCardById(cardId) ?: return null
        val updated = card.copy(snoozedUntil = null, snoozeLevel = 0)
        db.write { conn ->
            conn.prepareStatement(
                "UPDATE cards SET snoozed_until = NULL, snooze_level = 0 WHERE id = ?",
            ).use { ps ->
                ps.setString(1, cardId)
                ps.executeUpdate()
            }
        }
        return updated
    }

    override fun getBookmarkedCardsBySubject(subjectId: String): Flow<List<Card>> = reactiveQuery { conn ->
        conn.prepareStatement(
            """
            SELECT cards.* FROM cards
            INNER JOIN decks ON cards.deck_id = decks.id
            WHERE decks.subject_id = ? AND cards.is_bookmarked = 1
            ORDER BY cards.created_at DESC
            """.trimIndent(),
        ).use { ps ->
            ps.setString(1, subjectId)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(rs.toCard())
                }
            }
        }
    }

    override suspend fun searchCardsInSubject(subjectId: String, query: String): List<Card> = db.read { conn ->
        val pattern = "%$query%"
        conn.prepareStatement(
            """
            SELECT cards.* FROM cards
            INNER JOIN decks ON cards.deck_id = decks.id
            WHERE decks.subject_id = ? AND (
                cards.front LIKE ?
                OR cards.back LIKE ?
                OR cards.user_question LIKE ?
                OR cards.user_supplement LIKE ?
            )
            ORDER BY cards.front ASC
            """.trimIndent(),
        ).use { ps ->
            ps.setString(1, subjectId)
            ps.setString(2, pattern)
            ps.setString(3, pattern)
            ps.setString(4, pattern)
            ps.setString(5, pattern)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(rs.toCard())
                }
            }
        }
    }

    override suspend fun findCardByFrontInSubject(subjectId: String, frontWord: String): Card? = db.read { conn ->
        conn.prepareStatement(
            """
            SELECT cards.* FROM cards
            INNER JOIN decks ON cards.deck_id = decks.id
            WHERE decks.subject_id = ? AND cards.front = ?
            LIMIT 1
            """.trimIndent(),
        ).use { ps ->
            ps.setString(1, subjectId)
            ps.setString(2, frontWord)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.toCard() else null
            }
        }
    }

    override suspend fun getAllCardsInSubject(subjectId: String): List<Card> = db.read { conn ->
        conn.prepareStatement(
            """
            SELECT cards.* FROM cards
            INNER JOIN decks ON cards.deck_id = decks.id
            WHERE decks.subject_id = ?
            """.trimIndent(),
        ).use { ps ->
            ps.setString(1, subjectId)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(rs.toCard())
                }
            }
        }
    }

    override fun getLearnedCardsBySubject(subjectId: String): Flow<List<Card>> = reactiveQuery { conn ->
        queryCardsWithLatestStudyTimeBySubject(conn, subjectId).map { it.first }
    }

    override suspend fun getLearnedCardsBySubjectSync(subjectId: String): List<Card> = db.read { conn ->
        queryCardsWithLatestStudyTimeBySubject(conn, subjectId).map { it.first }
    }

    override fun getLearnedCardsByDeck(deckId: String): Flow<List<Card>> = reactiveQuery { conn ->
        queryCardsWithLatestStudyTimeByDeck(conn, deckId)
    }

    override suspend fun getLearnedCardsByDeckSync(deckId: String): List<Card> = db.read { conn ->
        queryCardsWithLatestStudyTimeByDeck(conn, deckId)
    }

    override fun getPastDayCardsBySubject(subjectId: String): Flow<Map<PastDayDeckType, PastDayGroup>> = reactiveQuery { conn ->
        val pairs = queryCardsWithLatestStudyTimeBySubject(conn, subjectId)
        PastDayDecksCalculator.calculate(pairs)
    }

    override suspend fun getCardsForPastDayDeckSync(subjectId: String, type: PastDayDeckType): List<Card> = db.read { conn ->
        val pairs = queryCardsWithLatestStudyTimeBySubject(conn, subjectId)
        PastDayDecksCalculator.calculate(pairs)[type]?.cards ?: emptyList()
    }

    override suspend fun getRandomCard(bookmarkOnly: Boolean): Card? = db.read { conn ->
        val now = System.currentTimeMillis()
        val bookmarkClause = if (bookmarkOnly) "AND cards.is_bookmarked = 1" else ""
        conn.prepareStatement(
            """
            SELECT cards.* FROM cards
            INNER JOIN decks ON cards.deck_id = decks.id
            INNER JOIN subjects ON decks.subject_id = subjects.id
            WHERE subjects.is_archived = 0
              $bookmarkClause
              AND (cards.snoozed_until IS NULL OR cards.snoozed_until <= ?)
            ORDER BY RANDOM() LIMIT 1
            """.trimIndent(),
        ).use { ps ->
            ps.setLong(1, now)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.toCard() else null
            }
        }
    }

    override suspend fun getRandomCardOverDaysAgo(cutoffTimestamp: Long): Card? = db.read { conn ->
        val now = System.currentTimeMillis()
        conn.prepareStatement(
            """
            SELECT cards.* FROM cards
            INNER JOIN decks ON cards.deck_id = decks.id
            INNER JOIN subjects ON decks.subject_id = subjects.id
            INNER JOIN (
                SELECT card_id, MAX(studied_at) AS max_studied_at
                FROM card_study_logs
                GROUP BY card_id
            ) latest_log ON cards.id = latest_log.card_id
            WHERE subjects.is_archived = 0
              AND latest_log.max_studied_at <= ?
              AND (cards.snoozed_until IS NULL OR cards.snoozed_until <= ?)
            ORDER BY RANDOM() LIMIT 1
            """.trimIndent(),
        ).use { ps ->
            ps.setLong(1, cutoffTimestamp)
            ps.setLong(2, now)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.toCard() else null
            }
        }
    }

    override suspend fun getOldestStudiedCard(): Card? = db.read { conn ->
        val now = System.currentTimeMillis()
        conn.prepareStatement(
            """
            SELECT cards.* FROM cards
            INNER JOIN decks ON cards.deck_id = decks.id
            INNER JOIN subjects ON decks.subject_id = subjects.id
            INNER JOIN (
                SELECT card_id, MAX(studied_at) AS max_studied_at
                FROM card_study_logs
                GROUP BY card_id
            ) latest_log ON cards.id = latest_log.card_id
            WHERE subjects.is_archived = 0
              AND (cards.snoozed_until IS NULL OR cards.snoozed_until <= ?)
            ORDER BY latest_log.max_studied_at ASC
            LIMIT 1
            """.trimIndent(),
        ).use { ps ->
            ps.setLong(1, now)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.toCard() else null
            }
        }
    }

    override suspend fun getSession(deckId: String): StudySession? = db.read { conn ->
        querySession(conn, deckId)
    }

    override fun getSessionFlow(deckId: String): Flow<StudySession?> = reactiveQuery { conn ->
        querySession(conn, deckId)
    }

    override suspend fun saveSession(session: StudySession) {
        db.write { conn ->
            conn.prepareStatement(
                """
                INSERT INTO study_sessions (
                    deck_id, part_index, current_card_index, queue_card_ids_json,
                    retry_card_ids_json, is_completed, last_studied_at, completed_part_indices_json
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(deck_id) DO UPDATE SET
                    part_index = excluded.part_index,
                    current_card_index = excluded.current_card_index,
                    queue_card_ids_json = excluded.queue_card_ids_json,
                    retry_card_ids_json = excluded.retry_card_ids_json,
                    is_completed = excluded.is_completed,
                    last_studied_at = excluded.last_studied_at,
                    completed_part_indices_json = excluded.completed_part_indices_json
                """.trimIndent(),
            ).use { ps ->
                ps.setString(1, session.deckId)
                ps.setInt(2, session.partIndex)
                ps.setInt(3, session.currentCardIndex)
                ps.setString(4, session.queueCardIdsJson())
                ps.setString(5, session.retryCardIdsJson())
                ps.setInt(6, if (session.isCompleted) 1 else 0)
                ps.setLong(7, session.lastStudiedAt)
                ps.setString(8, session.completedPartIndicesJson())
                ps.executeUpdate()
            }
        }
    }

    override suspend fun deleteSession(deckId: String) {
        db.write { conn ->
            conn.prepareStatement("DELETE FROM study_sessions WHERE deck_id = ?").use { ps ->
                ps.setString(1, deckId)
                ps.executeUpdate()
            }
        }
    }

    override suspend fun recordStudyLog(cardId: String, isCorrect: Boolean): CardStudyLog {
        val log = CardStudyLog(
            id = UUID.randomUUID().toString(),
            cardId = cardId,
            isCorrect = isCorrect,
            studiedAt = System.currentTimeMillis(),
        )
        db.write { conn ->
            conn.prepareStatement(
                """
                INSERT INTO card_study_logs (id, card_id, is_correct, studied_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    card_id = excluded.card_id,
                    is_correct = excluded.is_correct,
                    studied_at = excluded.studied_at
                """.trimIndent(),
            ).use { ps ->
                ps.setString(1, log.id)
                ps.setString(2, log.cardId)
                ps.setInt(3, if (log.isCorrect) 1 else 0)
                ps.setLong(4, log.studiedAt)
                ps.executeUpdate()
            }
        }
        return log
    }

    override suspend fun getRecentLogsForCard(cardId: String, limit: Int): List<CardStudyLog> = db.read { conn ->
        conn.prepareStatement(
            "SELECT * FROM card_study_logs WHERE card_id = ? ORDER BY studied_at DESC LIMIT ?",
        ).use { ps ->
            ps.setString(1, cardId)
            ps.setInt(2, limit)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(rs.toStudyLog())
                }
            }
        }
    }

    override fun getLogsForCardFlow(cardId: String): Flow<List<CardStudyLog>> = reactiveQuery { conn ->
        conn.prepareStatement(
            "SELECT * FROM card_study_logs WHERE card_id = ? ORDER BY studied_at DESC",
        ).use { ps ->
            ps.setString(1, cardId)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(rs.toStudyLog())
                }
            }
        }
    }

    override fun getLatestLogsForDeck(deckId: String): Flow<List<CardStudyLog>> = reactiveQuery { conn ->
        queryLatestLogsForDeck(conn, deckId)
    }

    override suspend fun getLatestLogsForDeckSync(deckId: String): List<CardStudyLog> = db.read { conn ->
        queryLatestLogsForDeck(conn, deckId)
    }

    override fun getAllStudyLogsFlow(): Flow<List<CardStudyLog>> = reactiveQuery { conn ->
        queryAllStudyLogs(conn)
    }

    override suspend fun getAllStudyLogsSync(): List<CardStudyLog> = db.read { conn ->
        queryAllStudyLogs(conn)
    }

    private fun <T> reactiveQuery(block: (Connection) -> T): Flow<T> =
        db.changeVersion
            .map { db.read(block) }
            .distinctUntilChanged()

    private fun queryDeckById(conn: Connection, deckId: String): Deck? =
        conn.prepareStatement("SELECT * FROM decks WHERE id = ?").use { ps ->
            ps.setString(1, deckId)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.toDeck() else null
            }
        }

    private fun queryCardsByDeck(conn: Connection, deckId: String): List<Card> =
        conn.prepareStatement(
            "SELECT * FROM cards WHERE deck_id = ? ORDER BY order_index ASC, created_at ASC",
        ).use { ps ->
            ps.setString(1, deckId)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(rs.toCard())
                }
            }
        }

    private fun queryCardById(conn: Connection, cardId: String): Card? =
        conn.prepareStatement("SELECT * FROM cards WHERE id = ?").use { ps ->
            ps.setString(1, cardId)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.toCard() else null
            }
        }

    private fun querySession(conn: Connection, deckId: String): StudySession? =
        conn.prepareStatement("SELECT * FROM study_sessions WHERE deck_id = ?").use { ps ->
            ps.setString(1, deckId)
            ps.executeQuery().use { rs ->
                if (rs.next()) rs.toStudySession() else null
            }
        }

    private fun queryLatestLogsForDeck(conn: Connection, deckId: String): List<CardStudyLog> =
        conn.prepareStatement(
            """
            SELECT l.* FROM card_study_logs l
            INNER JOIN (
                SELECT card_id, MAX(studied_at) AS max_studied_at
                FROM card_study_logs
                GROUP BY card_id
            ) latest ON l.card_id = latest.card_id AND l.studied_at = latest.max_studied_at
            INNER JOIN cards c ON c.id = l.card_id
            WHERE c.deck_id = ?
            """.trimIndent(),
        ).use { ps ->
            ps.setString(1, deckId)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(rs.toStudyLog())
                }
            }
        }

    private fun queryAllStudyLogs(conn: Connection): List<CardStudyLog> =
        conn.prepareStatement("SELECT * FROM card_study_logs ORDER BY studied_at ASC").use { ps ->
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(rs.toStudyLog())
                }
            }
        }

    private fun queryCardsWithLatestStudyTimeBySubject(
        conn: Connection,
        subjectId: String,
    ): List<Pair<Card, Long>> =
        conn.prepareStatement(
            """
            SELECT cards.*, latest_log.max_studied_at
            FROM cards
            INNER JOIN decks ON cards.deck_id = decks.id
            INNER JOIN (
                SELECT card_id, MAX(studied_at) AS max_studied_at
                FROM card_study_logs
                GROUP BY card_id
            ) latest_log ON cards.id = latest_log.card_id
            WHERE decks.subject_id = ?
            ORDER BY latest_log.max_studied_at DESC
            """.trimIndent(),
        ).use { ps ->
            ps.setString(1, subjectId)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) {
                        add(rs.toCard() to rs.getLong("max_studied_at"))
                    }
                }
            }
        }

    private fun queryCardsWithLatestStudyTimeByDeck(
        conn: Connection,
        deckId: String,
    ): List<Card> =
        conn.prepareStatement(
            """
            SELECT cards.*, latest_log.max_studied_at
            FROM cards
            INNER JOIN (
                SELECT card_id, MAX(studied_at) AS max_studied_at
                FROM card_study_logs
                GROUP BY card_id
            ) latest_log ON cards.id = latest_log.card_id
            WHERE cards.deck_id = ?
            ORDER BY latest_log.max_studied_at DESC
            """.trimIndent(),
        ).use { ps ->
            ps.setString(1, deckId)
            ps.executeQuery().use { rs ->
                buildList {
                    while (rs.next()) add(rs.toCard())
                }
            }
        }

    private fun upsertDeck(conn: Connection, deck: Deck) {
        conn.prepareStatement(
            """
            INSERT INTO decks (
                id, subject_id, name, split_size, auto_continue, shuffle_mode,
                reverse_mode, auto_speak, speech_rate, sort_order, created_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                subject_id = excluded.subject_id,
                name = excluded.name,
                split_size = excluded.split_size,
                auto_continue = excluded.auto_continue,
                shuffle_mode = excluded.shuffle_mode,
                reverse_mode = excluded.reverse_mode,
                auto_speak = excluded.auto_speak,
                speech_rate = excluded.speech_rate,
                sort_order = excluded.sort_order,
                created_at = excluded.created_at
            """.trimIndent(),
        ).use { ps ->
            ps.setString(1, deck.id)
            ps.setString(2, deck.subjectId)
            ps.setString(3, deck.name)
            ps.setInt(4, deck.splitSize)
            ps.setInt(5, if (deck.autoContinue) 1 else 0)
            ps.setInt(6, if (deck.shuffleMode) 1 else 0)
            ps.setInt(7, if (deck.studyDirection == StudyDirection.DESCRIPTION_TO_TERM) 1 else 0)
            ps.setInt(8, if (deck.autoSpeak) 1 else 0)
            ps.setFloat(9, deck.speechRate)
            ps.setInt(10, deck.sortOrder)
            ps.setLong(11, deck.createdAt)
            ps.executeUpdate()
        }
    }

    private fun upsertCard(conn: Connection, card: Card) {
        conn.prepareStatement(
            """
            INSERT INTO cards (
                id, deck_id, front, back, user_question, user_supplement,
                is_bookmarked, order_index, created_at, snoozed_until, snooze_level
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                deck_id = excluded.deck_id,
                front = excluded.front,
                back = excluded.back,
                user_question = excluded.user_question,
                user_supplement = excluded.user_supplement,
                is_bookmarked = excluded.is_bookmarked,
                order_index = excluded.order_index,
                created_at = excluded.created_at,
                snoozed_until = excluded.snoozed_until,
                snooze_level = excluded.snooze_level
            """.trimIndent(),
        ).use { ps ->
            ps.setString(1, card.id)
            ps.setString(2, card.deckId)
            ps.setString(3, card.term)
            ps.setString(4, card.description)
            ps.setString(5, card.userQuestion)
            ps.setString(6, card.userSupplement)
            ps.setInt(7, if (card.isBookmarked) 1 else 0)
            ps.setInt(8, card.orderIndex)
            ps.setLong(9, card.createdAt)
            if (card.snoozedUntil != null) {
                ps.setLong(10, card.snoozedUntil!!)
            } else {
                ps.setNull(10, Types.INTEGER)
            }
            ps.setInt(11, card.snoozeLevel)
            ps.executeUpdate()
        }
    }

    private fun ResultSet.toSubject(): Subject = Subject(
        id = getString("id"),
        name = getString("name"),
        ttsLanguage = getString("tts_language"),
        sortOrder = getInt("sort_order"),
        createdAt = getLong("created_at"),
        isArchived = getInt("is_archived") != 0,
        colorTheme = getString("color_theme"),
    )

    private fun ResultSet.toDeck(): Deck = Deck(
        id = getString("id"),
        subjectId = getString("subject_id"),
        name = getString("name"),
        splitSize = getInt("split_size"),
        autoContinue = getInt("auto_continue") != 0,
        shuffleMode = getInt("shuffle_mode") != 0,
        studyDirection = if (getInt("reverse_mode") != 0) {
            StudyDirection.DESCRIPTION_TO_TERM
        } else {
            StudyDirection.TERM_TO_DESCRIPTION
        },
        autoSpeak = getInt("auto_speak") != 0,
        speechRate = getFloat("speech_rate"),
        sortOrder = getInt("sort_order"),
        createdAt = getLong("created_at"),
    )

    private fun ResultSet.toCard(): Card {
        val rawSnooze = getLong("snoozed_until")
        val snoozedUntil = if (wasNull()) null else rawSnooze
        return Card(
            id = getString("id"),
            deckId = getString("deck_id"),
            term = getString("front"),
            description = getString("back"),
            userQuestion = getString("user_question"),
            userSupplement = getString("user_supplement"),
            isBookmarked = getInt("is_bookmarked") != 0,
            orderIndex = getInt("order_index"),
            createdAt = getLong("created_at"),
            snoozedUntil = snoozedUntil,
            snoozeLevel = getInt("snooze_level"),
        )
    }

    private fun ResultSet.toStudySession(): StudySession = StudySession(
        deckId = getString("deck_id"),
        partIndex = getInt("part_index"),
        currentCardIndex = getInt("current_card_index"),
        queueCardIds = StudySession.parseCardIds(getString("queue_card_ids_json")),
        retryCardIds = StudySession.parseCardIds(getString("retry_card_ids_json")),
        isCompleted = getInt("is_completed") != 0,
        lastStudiedAt = getLong("last_studied_at"),
        completedPartIndices = StudySession.parsePartIndices(getString("completed_part_indices_json")),
    )

    private fun ResultSet.toStudyLog(): CardStudyLog = CardStudyLog(
        id = getString("id"),
        cardId = getString("card_id"),
        isCorrect = getInt("is_correct") != 0,
        studiedAt = getLong("studied_at"),
    )
}
