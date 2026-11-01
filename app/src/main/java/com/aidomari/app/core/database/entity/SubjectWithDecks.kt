package com.aidomari.app.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SubjectWithDecks(
    @Embedded
    val subject: SubjectEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "subject_id",
    )
    val decks: List<DeckEntity>,
)
