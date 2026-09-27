package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.daysUntil

internal fun Modifier.timetableDateSelectBackground(
    dateRange: LocalDateRange,
    startInset: Dp,
    state: TimetableSelectState,
    color: Color,
): Modifier =
    drawBehind {
        val selection = state.selection as? TimetableSelection.Date ?: return@drawBehind
        val start = maxOf(selection.dateRange.start, dateRange.start)
        val endInclusive = minOf(selection.dateRange.endInclusive, dateRange.endInclusive)

        if (start > endInclusive) return@drawBehind

        val left = startInset.toPx()
        val dayWidth = (size.width - left) / dateRange.count()

        drawRect(
            color = color,
            topLeft = Offset(x = left + dateRange.start.daysUntil(start) * dayWidth, y = 0F),
            size = Size(width = (start.daysUntil(endInclusive) + 1) * dayWidth, height = size.height),
        )
    }

internal fun Modifier.timetableTimeSelectBackground(
    date: LocalDate,
    state: TimetableSelectState,
    color: Color,
): Modifier =
    drawBehind {
        val selection = state.selection as? TimetableSelection.Time ?: return@drawBehind
        val indexRange = selection.slotIndexRangeOn(date = date) ?: return@drawBehind
        val slotHeight = size.height / SELECT_SLOTS_PER_DAY

        drawRect(
            color = color,
            topLeft = Offset(x = 0F, y = indexRange.first * slotHeight),
            size = Size(width = size.width, height = (indexRange.last - indexRange.first + 1) * slotHeight),
        )
    }
