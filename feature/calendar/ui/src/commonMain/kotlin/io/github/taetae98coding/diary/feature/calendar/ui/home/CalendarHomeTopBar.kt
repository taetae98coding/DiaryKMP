package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.yearMonth

@Composable
internal fun CalendarHomeTopBar(
    onEvent: (CalendarHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: CalendarHomeScaffoldState = rememberCalendarHomeScaffoldState(),
    filterUiStateProvider: () -> CalendarHomeScaffoldFilterUiState = { CalendarHomeScaffoldFilterUiState() },
) {
    val coroutineScope = rememberCoroutineScope()

    CenterAlignedTopAppBar(
        title = {
            CalendarHomeYearMonthTitle(
                state = state,
                onClick = state::showDatePicker,
            )
        },
        modifier = modifier,
        actions = {
            state.today?.let { today ->
                CalendarHomeTodayButton(
                    today = today,
                    onClick = { coroutineScope.launch { state.calendarState.animateScrollTo(today.yearMonth) } },
                )
            }
            CalendarHomeFilterButton(
                filterUiStateProvider = filterUiStateProvider,
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
