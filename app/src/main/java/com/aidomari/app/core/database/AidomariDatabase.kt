package com.aidomari.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aidomari.app.core.database.entity.CardEntity
import com.aidomari.app.core.database.entity.DeckEntity
import com.aidomari.app.core.database.entity.StudySessionEntity
import com.aidomari.app.core.database.entity.SubjectEntity

@Database(
    entities = [
        SubjectEntity::class,
        DeckEntity::class,
        CardEntity::class,
        StudySessionEntity::class,
    ],
    version = 2,
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
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
    }
}
