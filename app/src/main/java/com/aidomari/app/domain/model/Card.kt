package com.aidomari.app.domain.model

data class Card(
    val id: String,
    val deckId: String,
    val front: String,
    val back: String,
    val userQuestion: String = "",
    val userSupplement: String = "",
    val isBookmarked: Boolean = false,
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
