package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.calendar.ui.Res
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_month_titles
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_year_month_title
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_timetable_navigate_up_content_description
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CalendarTimetableTopBar(
    onEvent: (CalendarTimetableScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: CalendarTimetableScaffoldState = rememberCalendarTimetableScaffoldState(),
) {
    val startDate = state.timetableState.currentDateRange.start
    val monthTitle = stringArrayResource(Res.array.calendar_home_month_titles).getOrNull(startDate.month.number - 1).orEmpty()

    DiaryNavigateUpTopBar(
        title = stringResource(Res.string.calendar_home_year_month_title, startDate.year.toString(), monthTitle),
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
