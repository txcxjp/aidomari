package com.aidomari.app.domain.model

enum class StudyDirection(val label: String) {
    TERM_TO_DESCRIPTION(label = "ことば → 説明"),
    DESCRIPTION_TO_TERM(label = "説明 → ことば"),
    ;

    fun toggled(): StudyDirection = when (this) {
        TERM_TO_DESCRIPTION -> DESCRIPTION_TO_TERM
        DESCRIPTION_TO_TERM -> TERM_TO_DESCRIPTION
    }
}
