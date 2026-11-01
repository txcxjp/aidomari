package com.aidomari.app.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class DeckWithCards(
    @Embedded
    val deck: DeckEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "deck_id",
    )
    val cards: List<CardEntity>,
)
