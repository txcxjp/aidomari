package com.aidomari.app.domain.model

import java.time.LocalDate

data class DailyStudyStats(
    val date: LocalDate,
    val learnedCount: Int,
    val reviewedCount: Int,
    val totalAnswers: Int,
    val correctAnswers: Int,
) {
    val accuracy: Float
        get() = if (totalAnswers > 0) correctAnswers.toFloat() / totalAnswers else 0f
}

data class StudySummaryStats(
    val studyDaysCount: Int,
    val totalLearnedCount: Int,
    val totalReviewedCount: Int,
    val totalAnswers: Int,
    val overallAccuracy: Float,
)
