package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.calendar.title.calendarYearMonthTitle
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.RoutineHomeViewMode
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.RoutineHomeViewModePreviewParameter
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.rememberRoutineHomeViewModeState
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_calendar_view_mode_button_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_drop_down_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_drop_up_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_filter_action_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_filter_applied_state_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_list_view_mode_button_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_title
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_today_button_content_description
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineHomeTopBar(
    onEvent: (RoutineHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: RoutineHomeScaffoldState = rememberRoutineHomeScaffoldState(),
    filterUiStateProvider: () -> RoutineHomeScaffoldFilterUiState = { RoutineHomeScaffoldFilterUiState() },
) {
    val coroutineScope = rememberCoroutineScope()
    val viewMode = state.viewMode
    val title = stringResource(Res.string.routine_home_title)
    val yearMonthTitle = calendarYearMonthTitle(yearMonth = state.calendarState.currentYearMonth)
    val expandContentDescription = stringResource(Res.string.routine_home_drop_down_content_description)
    val collapseContentDescription = stringResource(Res.string.routine_home_drop_up_content_description)
    val todayContentDescription = stringResource(Res.string.routine_home_today_button_content_description)
    val calendarViewModeContentDescription = stringResource(Res.string.routine_home_calendar_view_mode_button_content_description)
    val listViewModeContentDescription = stringResource(Res.string.routine_home_list_view_mode_button_content_description)
    val filterContentDescription = stringResource(Res.string.routine_home_filter_action_content_description)
    val filterAppliedStateDescription = stringResource(Res.string.routine_home_filter_applied_state_description)

    DiaryTopBar(
        title = {
            when (viewMode) {
                RoutineHomeViewMode.LIST -> Title(text = title)

                RoutineHomeViewMode.CALENDAR ->
                    TitleButton(
                        text = yearMonthTitle,
                        isExpanded = state.datePickerDialogState.isVisible,
                        expandContentDescription = expandContentDescription,
                        collapseContentDescription = collapseContentDescription,
                        onClick = state.datePickerDialogState::show,
                    )
            }
        },
        modifier = modifier,
        isTitleCentered = viewMode == RoutineHomeViewMode.CALENDAR,
        actions = {
            when (viewMode) {
                RoutineHomeViewMode.LIST ->
                    IconAction(
                        icon = DiaryTopBarIcon.CALENDAR_MONTH,
                        contentDescription = calendarViewModeContentDescription,
                        onClick = state::toggleViewMode,
                    )

                RoutineHomeViewMode.CALENDAR -> {
                    TextAction(
                        text = state.today.day.toString(),
                        contentDescription = todayContentDescription,
                        onClick = { coroutineScope.launch { state.animateScrollToToday() } },
                    )
                    IconAction(
                        icon = DiaryTopBarIcon.LIST,
                        contentDescription = listViewModeContentDescription,
                        onClick = state::toggleViewMode,
                    )
                }
            }
            AppliedAction(
                icon = DiaryTopBarIcon.FILTER,
                contentDescription = filterContentDescription,
                isApplied = filterUiStateProvider().isApplied,
                appliedStateDescription = filterAppliedStateDescription,
                onClick = { onEvent(RoutineHomeScaffoldEvent.ClickFilter) },
            )
        },
    )
}

@ComponentPreview
@Composable
private fun RoutineHomeTopBarPreview(
    @PreviewParameter(RoutineHomeViewModePreviewParameter::class) viewMode: RoutineHomeViewMode,
) {
    DiaryTheme {
        RoutineHomeTopBar(
            onEvent = {},
            state = rememberRoutineHomeScaffoldState(viewModeState = rememberRoutineHomeViewModeState(initialViewMode = viewMode)),
        )
    }
}
