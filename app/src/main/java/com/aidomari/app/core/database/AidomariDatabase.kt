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
            ).build()
    }
}
