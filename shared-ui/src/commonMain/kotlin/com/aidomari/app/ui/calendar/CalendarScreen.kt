package com.aidomari.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aidomari.app.domain.model.DailyStudyStats
import com.aidomari.app.domain.model.StudySummaryStats
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "学習記録・カレンダー",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("CalendarBackButton"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // Month Navigation
            MonthHeader(
                currentMonth = uiState.currentMonth,
                onPreviousMonth = viewModel::previousMonth,
                onNextMonth = viewModel::nextMonth,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Calendar Grid Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("CalendarGridCard"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                ) {
                    WeekDaysHeader()
                    Spacer(modifier = Modifier.height(8.dp))
                    CalendarDaysGrid(
                        currentMonth = uiState.currentMonth,
                        selectedDate = uiState.selectedDate,
                        dailyStatsMap = uiState.dailyStatsMap,
                        onDateSelect = viewModel::selectDate,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Day Details
            SelectedDayDetailsSection(
                selectedDate = uiState.selectedDate,
                stats = uiState.selectedDayStats,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Month Summary Card
            MonthSummarySection(
                currentMonth = uiState.currentMonth,
                summary = uiState.monthSummary,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MonthHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthTitle = "${currentMonth.year}年${currentMonth.monthValue}月"

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.testTag("PreviousMonthButton"),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "前月",
            )
        }

        Text(
            text = monthTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("CurrentMonthText"),
        )

        IconButton(
            onClick = onNextMonth,
            modifier = Modifier.testTag("NextMonthButton"),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "翌月",
            )
        }
    }
}

@Composable
private fun WeekDaysHeader(modifier: Modifier = Modifier) {
    val daysOfWeek = listOf("日", "月", "火", "水", "木", "金", "土")
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        daysOfWeek.forEachIndexed { index, day ->
            val color = when (index) {
                0 -> MaterialTheme.colorScheme.error // 日曜
                6 -> MaterialTheme.colorScheme.primary // 土曜
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

@Composable
private fun CalendarDaysGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    dailyStatsMap: Map<LocalDate, DailyStudyStats>,
    onDateSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    // DayOfWeek: MONDAY=1, ..., SUNDAY=7. Convert to SUNDAY=0, MONDAY=1, ..., SATURDAY=6
    val firstDayOfWeek = if (firstDayOfMonth.dayOfWeek == DayOfWeek.SUNDAY) 0 else firstDayOfMonth.dayOfWeek.value
    val today = LocalDate.now()

    val totalCells = firstDayOfWeek + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayOfMonth = cellIndex - firstDayOfWeek + 1

                    if (dayOfMonth in 1..daysInMonth) {
                        val date = currentMonth.atDay(dayOfMonth)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val stats = dailyStatsMap[date]

                        CalendarDayCell(
                            date = date,
                            isSelected = isSelected,
                            isToday = isToday,
                            stats = stats,
                            onSelect = { onDateSelect(date) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        // Empty spacer cell
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    stats: DailyStudyStats?,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayNumber = date.dayOfMonth.toString()
    val totalAnswers = stats?.totalAnswers ?: 0

    // Achievement dot color based on study volume
    val dotColor = when {
        totalAnswers >= 30 -> Color(0xFF2E7D32) // 深い緑（しっかり）
        totalAnswers >= 10 -> Color(0xFF4CAF50) // 明るい緑
        totalAnswers > 0 -> Color(0xFF81C784) // 薄い緑
        else -> null
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            )
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                    )
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onSelect)
            .testTag("CalendarDay_$date"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = dayNumber,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else if (isToday) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )

            // Achievement Dot
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SelectedDayDetailsSection(
    selectedDate: LocalDate,
    stats: DailyStudyStats?,
    modifier: Modifier = Modifier,
) {
    val formatter = DateTimeFormatter.ofPattern("M月d日")
    val dayOfWeekStr = when (selectedDate.dayOfWeek) {
        DayOfWeek.SUNDAY -> "日"
        DayOfWeek.MONDAY -> "月"
        DayOfWeek.TUESDAY -> "火"
        DayOfWeek.WEDNESDAY -> "水"
        DayOfWeek.THURSDAY -> "木"
        DayOfWeek.FRIDAY -> "金"
        DayOfWeek.SATURDAY -> "土"
    }
    val dateText = "${selectedDate.format(formatter)} ($dayOfWeekStr) の実績"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("SelectedDayDetailsCard"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (stats == null || stats.totalAnswers == 0) {
                Text(
                    text = "この日の学習記録はありません。\n隙間時間に少しだけでも単語を解いてみましょう！",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DetailMetricItem(
                        icon = Icons.Default.AutoAwesome,
                        label = "新しく覚えた",
                        value = "${stats.learnedCount} 単語",
                        iconColor = Color(0xFFFFB300),
                        modifier = Modifier.weight(1f),
                    )
                    DetailMetricItem(
                        icon = Icons.Default.FitnessCenter,
                        label = "復習した",
                        value = "${stats.reviewedCount} 単語",
                        iconColor = Color(0xFF26A69A),
                        modifier = Modifier.weight(1f),
                    )
                    val accuracyPercent = (stats.accuracy * 100).toInt()
                    DetailMetricItem(
                        icon = Icons.Default.School,
                        label = "総演習数",
                        value = "${stats.totalAnswers} 問",
                        subValue = "正答率 $accuracyPercent%",
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailMetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    modifier: Modifier = Modifier,
    subValue: String? = null,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            if (subValue != null) {
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun MonthSummarySection(
    currentMonth: YearMonth,
    summary: StudySummaryStats,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("MonthSummaryCard"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "${currentMonth.monthValue}月の累計サマリー",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                SummaryStatCell(
                    label = "学習日数",
                    value = "${summary.studyDaysCount}日",
                )
                SummaryStatCell(
                    label = "新規習得",
                    value = "${summary.totalLearnedCount}単語",
                )
                SummaryStatCell(
                    label = "総演習数",
                    value = "${summary.totalAnswers}問",
                )
                val accuracyPercent = (summary.overallAccuracy * 100).toInt()
                SummaryStatCell(
                    label = "平均正答率",
                    value = "$accuracyPercent%",
                )
            }
        }
    }
}

@Composable
private fun SummaryStatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
