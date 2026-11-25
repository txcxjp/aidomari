package com.aidomari.app.desktop

import com.aidomari.app.core.database.DefaultSampleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID

/**
 * Thread-safe SQLite helper for Aidomari Desktop matching the Android Room v10 schema.
 */
class DesktopDatabase(
    databaseFile: File = DesktopAppDirs.databaseFile(),
    seedSampleDataIfEmpty: Boolean = true,
) : Closeable {

    private val lock = Any()
    private val connection: Connection
    private val _changeVersion = MutableStateFlow(0L)
    val changeVersion: StateFlow<Long> = _changeVersion.asStateFlow()

    init {
        databaseFile.parentFile?.let { parent ->
            if (!parent.exists()) parent.mkdirs()
        }
        connection = DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}")
        synchronized(lock) {
            connection.createStatement().use { stmt ->
                stmt.execute("PRAGMA foreign_keys = ON")
                stmt.execute("PRAGMA busy_timeout = 5000")
            }
            createSchema()
            if (seedSampleDataIfEmpty) {
                seedInitialDataIfEmpty()
            }
        }
    }

    suspend fun <T> read(block: (Connection) -> T): T = withContext(Dispatchers.IO) {
        synchronized(lock) {
            block(connection)
        }
    }

    suspend fun <T> write(block: (Connection) -> T): T = withContext(Dispatchers.IO) {
        val result = synchronized(lock) {
            connection.autoCommit = false
            try {
                val res = block(connection)
                connection.commit()
                res
            } catch (e: Throwable) {
                runCatching { connection.rollback() }
                throw e
            } finally {
                runCatching { connection.autoCommit = true }
            }
        }
        _changeVersion.update { it + 1 }
        result
    }

    fun notifyChanged() {
        _changeVersion.update { it + 1 }
    }

    override fun close() {
        synchronized(lock) {
            runCatching { connection.close() }
        }
    }

    private fun createSchema() {
        connection.createStatement().use { stmt ->
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS subjects (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    tts_language TEXT NOT NULL DEFAULT 'auto',
                    sort_order INTEGER NOT NULL DEFAULT 0,
                    created_at INTEGER NOT NULL,
                    is_archived INTEGER NOT NULL DEFAULT 0,
                    color_theme TEXT
                )
                """.trimIndent(),
            )
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS decks (
                    id TEXT NOT NULL PRIMARY KEY,
                    subject_id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    split_size INTEGER NOT NULL DEFAULT 50,
                    auto_continue INTEGER NOT NULL DEFAULT 0,
                    shuffle_mode INTEGER NOT NULL DEFAULT 0,
                    reverse_mode INTEGER NOT NULL DEFAULT 0,
                    auto_speak INTEGER NOT NULL DEFAULT 0,
                    speech_rate REAL NOT NULL DEFAULT 1.0,
                    sort_order INTEGER NOT NULL DEFAULT 0,
                    created_at INTEGER NOT NULL,
                    FOREIGN KEY(subject_id) REFERENCES subjects(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            stmt.execute("CREATE INDEX IF NOT EXISTS index_decks_subject_id ON decks(subject_id)")
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS cards (
                    id TEXT NOT NULL PRIMARY KEY,
                    deck_id TEXT NOT NULL,
                    front TEXT NOT NULL,
                    back TEXT NOT NULL,
                    user_question TEXT NOT NULL DEFAULT '',
                    user_supplement TEXT NOT NULL DEFAULT '',
                    is_bookmarked INTEGER NOT NULL DEFAULT 0,
                    order_index INTEGER NOT NULL DEFAULT 0,
                    created_at INTEGER NOT NULL,
                    snoozed_until INTEGER,
                    snooze_level INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY(deck_id) REFERENCES decks(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            stmt.execute("CREATE INDEX IF NOT EXISTS index_cards_deck_id ON cards(deck_id)")
            stmt.execute("CREATE INDEX IF NOT EXISTS index_cards_front ON cards(front)")
            stmt.execute("CREATE INDEX IF NOT EXISTS index_cards_snoozed_until ON cards(snoozed_until)")
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS study_sessions (
                    deck_id TEXT NOT NULL PRIMARY KEY,
                    part_index INTEGER NOT NULL DEFAULT 0,
                    current_card_index INTEGER NOT NULL DEFAULT 0,
                    queue_card_ids_json TEXT NOT NULL DEFAULT '[]',
                    retry_card_ids_json TEXT NOT NULL DEFAULT '[]',
                    is_completed INTEGER NOT NULL DEFAULT 0,
                    last_studied_at INTEGER NOT NULL,
                    completed_part_indices_json TEXT NOT NULL DEFAULT '[]',
                    FOREIGN KEY(deck_id) REFERENCES decks(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS card_study_logs (
                    id TEXT NOT NULL PRIMARY KEY,
                    card_id TEXT NOT NULL,
                    is_correct INTEGER NOT NULL,
                    studied_at INTEGER NOT NULL,
                    FOREIGN KEY(card_id) REFERENCES cards(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            stmt.execute("CREATE INDEX IF NOT EXISTS index_card_study_logs_card_id ON card_study_logs(card_id)")
            stmt.execute("CREATE INDEX IF NOT EXISTS index_card_study_logs_studied_at ON card_study_logs(studied_at)")
            stmt.execute(
                """
                CREATE TABLE IF NOT EXISTS source_articles (
                    id TEXT NOT NULL PRIMARY KEY,
                    url TEXT NOT NULL,
                    title TEXT NOT NULL,
                    domain TEXT NOT NULL,
                    extracted_text TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    created_at INTEGER NOT NULL,
                    generated_card_count INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            stmt.execute("CREATE INDEX IF NOT EXISTS index_source_articles_url ON source_articles(url)")
            stmt.execute("CREATE INDEX IF NOT EXISTS index_source_articles_status ON source_articles(status)")
            stmt.execute("CREATE INDEX IF NOT EXISTS index_source_articles_created_at ON source_articles(created_at)")
        }
    }

    private fun seedInitialDataIfEmpty() {
        val count = connection.createStatement().use { stmt ->
            stmt.executeQuery("SELECT COUNT(*) FROM subjects").use { rs ->
                if (rs.next()) rs.getInt(1) else 0
            }
        }
        if (count > 0) return

        val now = System.currentTimeMillis()
        val subjectId = UUID.randomUUID().toString()
        val deckId = UUID.randomUUID().toString()

        connection.prepareStatement(
            """
            INSERT INTO subjects (id, name, tts_language, sort_order, created_at, is_archived, color_theme)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { ps ->
            ps.setString(1, subjectId)
            ps.setString(2, DefaultSampleData.SAMPLE_SUBJECT_NAME)
            ps.setString(3, "en-US")
            ps.setInt(4, 0)
            ps.setLong(5, now)
            ps.setInt(6, 0)
            ps.setString(7, "INDIGO")
            ps.executeUpdate()
        }

        connection.prepareStatement(
            """
            INSERT INTO decks (id, subject_id, name, split_size, auto_continue, shuffle_mode, reverse_mode, auto_speak, speech_rate, sort_order, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { ps ->
            ps.setString(1, deckId)
            ps.setString(2, subjectId)
            ps.setString(3, DefaultSampleData.SAMPLE_DECK_NAME)
            ps.setInt(4, 50)
            ps.setInt(5, 0)
            ps.setInt(6, 0)
            ps.setInt(7, 0)
            ps.setInt(8, 0)
            ps.setFloat(9, 1.0f)
            ps.setInt(10, 0)
            ps.setLong(11, now)
            ps.executeUpdate()
        }

        connection.prepareStatement(
            """
            INSERT INTO cards (id, deck_id, front, back, user_question, user_supplement, is_bookmarked, order_index, created_at, snoozed_until, snooze_level)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
        ).use { ps ->
            DefaultSampleData.sampleCards.forEachIndexed { index, card ->
                ps.setString(1, UUID.randomUUID().toString())
                ps.setString(2, deckId)
                ps.setString(3, card.term)
                ps.setString(4, card.description)
                ps.setString(5, card.userQuestion)
                ps.setString(6, card.userSupplement)
                ps.setInt(7, if (card.isBookmarked) 1 else 0)
                ps.setInt(8, index)
                ps.setLong(9, now + index)
                ps.setNull(10, java.sql.Types.INTEGER)
                ps.setInt(11, 0)
                ps.executeUpdate()
            }
        }
    }
}
