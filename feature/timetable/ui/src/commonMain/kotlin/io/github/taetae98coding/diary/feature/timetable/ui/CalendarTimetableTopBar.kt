package io.github.taetae98coding.diary.feature.timetable.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.calendar.title.calendarYearMonthTitle
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CalendarTimetableTopBar(
    onEvent: (CalendarTimetableScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: CalendarTimetableScaffoldState = rememberCalendarTimetableScaffoldState(),
) {
    val startDate = state.timetableState.currentDateRange.start

    DiaryNavigateUpTopBar(
        title = calendarYearMonthTitle(yearMonth = YearMonth(year = startDate.year, month = startDate.month)),
        onNavigateUp = { onEvent(CalendarTimetableScaffoldEvent.ClickNavigateUp) },
        modifier = modifier,
        navigateUpContentDescription = stringResource(Res.string.calendar_timetable_navigate_up_content_description),
    )
}

@ComponentPreview
@Composable
private fun CalendarTimetableTopBarPreview() {
    DiaryTheme {
        CalendarTimetableTopBar(onEvent = {})
    }
}
