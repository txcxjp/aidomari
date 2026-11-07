package com.aidomari.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aidomari.app.domain.model.DailyStudyStats
import com.aidomari.app.domain.repository.WordsRepository
import com.aidomari.app.domain.util.StudyStatsCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class CalendarViewModel(
    private val repository: WordsRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadStudyLogs()
    }

    private fun loadStudyLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAllStudyLogsFlow().collect { logs ->
                val dailyStats = StudyStatsCalculator.calculateDailyStats(logs, zoneId)
                _uiState.update { current ->
                    val monthDailyStats = filterMonthStats(dailyStats, current.currentMonth)
                    val summary = StudyStatsCalculator.calculateSummaryStats(monthDailyStats.values)
                    val selectedStats = dailyStats[current.selectedDate]
                    current.copy(
                        dailyStatsMap = dailyStats,
                        monthSummary = summary,
                        selectedDayStats = selectedStats,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { current ->
            current.copy(
                selectedDate = date,
                selectedDayStats = current.dailyStatsMap[date],
            )
        }
    }

    fun previousMonth() {
        _uiState.update { current ->
            val prev = current.currentMonth.minusMonths(1)
            val monthStats = filterMonthStats(current.dailyStatsMap, prev)
            val summary = StudyStatsCalculator.calculateSummaryStats(monthStats.values)
            current.copy(
                currentMonth = prev,
                monthSummary = summary,
            )
        }
    }

    fun nextMonth() {
        _uiState.update { current ->
            val next = current.currentMonth.plusMonths(1)
            val monthStats = filterMonthStats(current.dailyStatsMap, next)
            val summary = StudyStatsCalculator.calculateSummaryStats(monthStats.values)
            current.copy(
                currentMonth = next,
                monthSummary = summary,
            )
        }
    }

    fun setMonth(yearMonth: YearMonth) {
        _uiState.update { current ->
            val monthStats = filterMonthStats(current.dailyStatsMap, yearMonth)
            val summary = StudyStatsCalculator.calculateSummaryStats(monthStats.values)
            current.copy(
                currentMonth = yearMonth,
                monthSummary = summary,
            )
        }
    }

    private fun filterMonthStats(
        allStats: Map<LocalDate, DailyStudyStats>,
        month: YearMonth,
    ): Map<LocalDate, DailyStudyStats> = allStats.filter { (date, _) ->
        date.year == month.year && date.month == month.month
    }
}
