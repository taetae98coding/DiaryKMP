package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.calendar.ui.Res
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_drop_down_content_description
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_drop_up_content_description
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_filter_action_content_description
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_filter_applied_state_description
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_month_titles
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_today_button_content_description
import io.github.taetae98coding.diary.feature.calendar.ui.calendar_home_year_month_title
import kotlinx.coroutines.launch
import kotlinx.datetime.number
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CalendarHomeTopBar(
    onEvent: (CalendarHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: CalendarHomeScaffoldState = rememberCalendarHomeScaffoldState(),
    filterUiStateProvider: () -> CalendarHomeScaffoldFilterUiState = { CalendarHomeScaffoldFilterUiState() },
) {
    val coroutineScope = rememberCoroutineScope()
    val yearMonth = state.calendarState.currentYearMonth
    val monthTitle = stringArrayResource(Res.array.calendar_home_month_titles).getOrNull(yearMonth.month.number - 1).orEmpty()
    val title = stringResource(Res.string.calendar_home_year_month_title, yearMonth.year.toString(), monthTitle)
    val expandContentDescription = stringResource(Res.string.calendar_home_drop_down_content_description)
    val collapseContentDescription = stringResource(Res.string.calendar_home_drop_up_content_description)
    val todayContentDescription = stringResource(Res.string.calendar_home_today_button_content_description)
    val filterContentDescription = stringResource(Res.string.calendar_home_filter_action_content_description)
    val filterAppliedStateDescription = stringResource(Res.string.calendar_home_filter_applied_state_description)
    val today = state.today
    val filterUiState = filterUiStateProvider()

    DiaryTopBar(
        title = {
            TitleButton(
                text = title,
                isExpanded = state.isDatePickerVisible,
                expandContentDescription = expandContentDescription,
                collapseContentDescription = collapseContentDescription,
                onClick = state::showDatePicker,
            )
        },
        modifier = modifier,
        isTitleCentered = true,
        actions = {
            if (today != null) {
                TextAction(
                    text = today.day.toString(),
                    contentDescription = todayContentDescription,
                    onClick = { coroutineScope.launch { state.animateScrollToToday() } },
                )
            }
            AppliedAction(
                icon = DiaryTopBarIcon.FILTER,
                contentDescription = filterContentDescription,
                isApplied = filterUiState.isApplied,
                appliedStateDescription = filterAppliedStateDescription,
                onClick = { onEvent(CalendarHomeScaffoldEvent.ClickFilter) },
            )
        },
    )
}

@ComponentPreview
@Composable
private fun CalendarHomeTopBarPreview() {
    DiaryTheme {
        CalendarHomeTopBar(onEvent = {})
    }
}
