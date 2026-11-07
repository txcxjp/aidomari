package com.aidomari.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class CardWithLatestStudiedAt(
    @Embedded val card: CardEntity,
    @ColumnInfo(name = "max_studied_at") val maxStudiedAt: Long,
)
