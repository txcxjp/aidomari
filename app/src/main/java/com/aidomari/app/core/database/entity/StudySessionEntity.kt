package com.aidomari.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_sessions",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deck_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class StudySessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "deck_id")
    val deckId: String,
    @ColumnInfo(name = "part_index")
    val partIndex: Int = 0,
    @ColumnInfo(name = "current_card_index")
    val currentCardIndex: Int = 0,
    @ColumnInfo(name = "queue_card_ids_json")
    val queueCardIdsJson: String = "[]",
    @ColumnInfo(name = "retry_card_ids_json")
    val retryCardIdsJson: String = "[]",
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    @ColumnInfo(name = "last_studied_at")
    val lastStudiedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "completed_part_indices_json")
    val completedPartIndicesJson: String = "[]",
)
