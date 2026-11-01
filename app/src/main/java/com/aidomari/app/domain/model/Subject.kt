package com.aidomari.app.domain.model

data class Subject(
    val id: String,
    val name: String,
    val ttsLanguage: String = "auto",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
