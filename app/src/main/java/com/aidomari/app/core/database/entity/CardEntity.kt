package com.aidomari.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deck_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("deck_id"),
        Index("front"),
        Index("snoozed_until"),
    ],
)
data class CardEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "deck_id")
    val deckId: String,
    val front: String,
    val back: String,
    @ColumnInfo(name = "user_question")
    val userQuestion: String = "",
    @ColumnInfo(name = "user_supplement")
    val userSupplement: String = "",
    @ColumnInfo(name = "is_bookmarked")
    val isBookmarked: Boolean = false,
    @ColumnInfo(name = "order_index")
    val orderIndex: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "snoozed_until")
    val snoozedUntil: Long? = null,
    @ColumnInfo(name = "snooze_level", defaultValue = "0")
    val snoozeLevel: Int = 0,
)
