package com.aidomari.app.domain.model

data class CardStudyLog(
    val id: String,
    val cardId: String,
    val isCorrect: Boolean,
    val studiedAt: Long = System.currentTimeMillis(),
)
