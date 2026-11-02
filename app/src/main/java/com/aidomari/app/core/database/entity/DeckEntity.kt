package com.aidomari.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "decks",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("subject_id"),
    ],
)
data class DeckEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "subject_id")
    val subjectId: String,
    val name: String,
    @ColumnInfo(name = "split_size")
    val splitSize: Int = 50,
    @ColumnInfo(name = "auto_continue")
    val autoContinue: Boolean = false,
    @ColumnInfo(name = "shuffle_mode")
    val shuffleMode: Boolean = false,
    @ColumnInfo(name = "reverse_mode")
    val reverseMode: Boolean = false,
    @ColumnInfo(name = "auto_speak")
    val autoSpeak: Boolean = false,
    @ColumnInfo(name = "speech_rate")
    val speechRate: Float = 1.0f,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)
