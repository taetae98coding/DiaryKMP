package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.compose.timetable.TimetableState
import io.github.taetae98coding.diary.compose.timetable.TimetableType
import io.github.taetae98coding.diary.compose.timetable.rememberTimetableState
import io.github.taetae98coding.diary.feature.calendar.api.CalendarTimetableNavKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Stable
internal class CalendarTimetableScaffoldState(
    val timetableState: TimetableState,
) {
    var now: LocalDateTime? by mutableStateOf(null)
        private set

    fun updateNow() {
        now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }
}

@Composable
internal fun rememberCalendarTimetableScaffoldState(
    type: CalendarTimetableNavKey.Type = CalendarTimetableNavKey.Type.DAY,
    initialDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    timetableState: TimetableState = rememberTimetableState(type = type.toTimetableType(), initialDate = initialDate),
): CalendarTimetableScaffoldState = remember(timetableState) { CalendarTimetableScaffoldState(timetableState = timetableState) }

private fun CalendarTimetableNavKey.Type.toTimetableType(): TimetableType =
    when (this) {
        CalendarTimetableNavKey.Type.DAY -> TimetableType.DAY
        CalendarTimetableNavKey.Type.WEEK -> TimetableType.WEEK
    }
