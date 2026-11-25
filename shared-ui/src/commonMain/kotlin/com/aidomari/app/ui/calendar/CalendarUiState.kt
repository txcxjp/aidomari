package com.aidomari.app.ui.calendar

import com.aidomari.app.domain.model.DailyStudyStats
import com.aidomari.app.domain.model.StudySummaryStats
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val dailyStatsMap: Map<LocalDate, DailyStudyStats> = emptyMap(),
    val monthSummary: StudySummaryStats = StudySummaryStats(
        studyDaysCount = 0,
        totalLearnedCount = 0,
        totalReviewedCount = 0,
        totalAnswers = 0,
        overallAccuracy = 0f,
    ),
    val selectedDayStats: DailyStudyStats? = null,
    val isLoading: Boolean = false,
)
