package com.aidomari.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 1,
    exportSchema = false,
)
abstract class AidomariDatabase : RoomDatabase() {

    abstract fun aidomariDao(): AidomariDao

    companion object {
        private const val DATABASE_NAME = "aidomari.db"

        @Volatile
        private var INSTANCE: AidomariDatabase? = null

        fun getInstance(context: Context): AidomariDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AidomariDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AidomariDatabase::class.java,
                DATABASE_NAME,
            ).build()
        }
    }
}
