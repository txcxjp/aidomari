package com.aidomari.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aidomari.app.core.database.entity.CardEntity
import com.aidomari.app.core.database.entity.CardStudyLogEntity
import com.aidomari.app.core.database.entity.DeckEntity
import com.aidomari.app.core.database.entity.StudySessionEntity
import com.aidomari.app.core.database.entity.SubjectEntity

@Database(
    entities = [
        SubjectEntity::class,
        DeckEntity::class,
        CardEntity::class,
        StudySessionEntity::class,
        CardStudyLogEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
abstract class AidomariDatabase : RoomDatabase() {

    abstract fun aidomariDao(): AidomariDao

    companion object {
        private const val DATABASE_NAME = "aidomari.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE decks ADD COLUMN auto_speak INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE decks ADD COLUMN speech_rate REAL NOT NULL DEFAULT 1.0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE study_sessions ADD COLUMN completed_part_indices_json TEXT NOT NULL DEFAULT '[]'")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subjects ADD COLUMN is_archived INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
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
                db.execSQL("CREATE INDEX IF NOT EXISTS index_card_study_logs_card_id ON card_study_logs(card_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_card_study_logs_studied_at ON card_study_logs(studied_at)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cards ADD COLUMN snoozed_until INTEGER")
                db.execSQL("ALTER TABLE cards ADD COLUMN snooze_level INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_cards_snoozed_until ON cards(snoozed_until)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subjects ADD COLUMN color_theme TEXT")
            }
        }

        @Volatile
        private var instance: AidomariDatabase? = null

        fun getInstance(context: Context): AidomariDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }

        private fun buildDatabase(context: Context): AidomariDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AidomariDatabase::class.java,
                DATABASE_NAME,
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .addCallback(
                    object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            SampleData.seedDatabase(db)
                        }
                    },
                )
                .fallbackToDestructiveMigration()
                .build()
    }
}
