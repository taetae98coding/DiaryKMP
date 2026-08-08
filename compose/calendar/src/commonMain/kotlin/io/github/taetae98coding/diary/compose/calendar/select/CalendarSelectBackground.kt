package io.github.taetae98coding.diary.compose.calendar.select

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.calendar.dateRangeAt
import io.github.taetae98coding.diary.library.kotlinx.datetime.DAYS_PER_WEEK
import kotlinx.datetime.YearMonth
import kotlinx.datetime.daysUntil

internal fun Modifier.calendarSelectBackground(
    yearMonth: YearMonth,
    weekOfMonth: Int,
    state: CalendarSelectState,
    color: Color,
): Modifier =
    drawBehind {
        val dateRange = state.dateRange ?: return@drawBehind
        val weekDateRange = yearMonth.dateRangeAt(weekOfMonth = weekOfMonth)
        val start = maxOf(dateRange.start, weekDateRange.start)
        val endInclusive = minOf(dateRange.endInclusive, weekDateRange.endInclusive)

        if (start > endInclusive) return@drawBehind

        val cellWidth = size.width / DAYS_PER_WEEK

        drawRect(
            color = color,
            topLeft = Offset(x = weekDateRange.start.daysUntil(start) * cellWidth, y = 0F),
            size = Size(width = (start.daysUntil(endInclusive) + 1) * cellWidth, height = size.height),
        )
    }
