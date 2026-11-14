package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.dialog.DiaryDatePickerDialogHost
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.shortcut.isAddShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.previewRoutineSummary
import io.github.taetae98coding.diary.compose.routine.previewRoutineToday
import io.github.taetae98coding.diary.compose.routine.rememberRoutineListState
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.home.amount.RoutineAmountDialogHost
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendar
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.RoutineHomeViewMode
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.RoutineHomeViewModePreviewParameter
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.rememberRoutineHomeViewModeState
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_add_button_content_description
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.datetime.yearMonth
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineHomeScaffold(
    onEvent: (RoutineHomeScaffoldEvent) -> Unit,
    onRoutineListEvent: (RoutineListEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: RoutineHomeScaffoldState = rememberRoutineHomeScaffoldState(),
    routinePagingItems: LazyPagingItems<RoutineSummary> = remember { flowOf(PagingData.empty<RoutineSummary>()) }.collectAsLazyPagingItems(),
    filterUiStateProvider: () -> RoutineHomeScaffoldFilterUiState = { RoutineHomeScaffoldFilterUiState() },
    calendarUiStateProvider: () -> RoutineHomeCalendarUiState = { RoutineHomeCalendarUiState() },
    holidayUiStateProvider: () -> RoutineHomeHolidayUiState = { RoutineHomeHolidayUiState() },
    syncUiStateProvider: () -> SyncRefreshUiState = { SyncRefreshUiState() },
    componentVisibleProvider: () -> RoutineHomeScaffoldComponentVisible = { RoutineHomeScaffoldComponentVisible() },
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier =
            modifier.keyShortcut(isEnableProvider = { !state.isDialogVisible }) { keyEvent ->
                when {
                    keyEvent.isAddShortcut() && componentVisibleProvider().isAddButtonVisible -> {
                        onEvent(RoutineHomeScaffoldEvent.ClickAdd)
                        true
                    }

                    state.viewMode == RoutineHomeViewMode.CALENDAR && keyEvent.isPreviousMonthShortcut() -> {
                        coroutineScope.launch { state.calendarState.animateScrollToPreviousMonth() }
                        true
                    }

                    state.viewMode == RoutineHomeViewMode.CALENDAR && keyEvent.isNextMonthShortcut() -> {
                        coroutineScope.launch { state.calendarState.animateScrollToNextMonth() }
                        true
                    }

                    else -> false
                }
            },
        topBar = {
            RoutineHomeTopBar(
                onEvent = onEvent,
                state = state,
                filterUiStateProvider = filterUiStateProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        floatingActionButton = {
            if (componentVisibleProvider().isAddButtonVisible) {
                FloatingAddButton(
                    onClick = { onEvent(RoutineHomeScaffoldEvent.ClickAdd) },
                    contentDescription = stringResource(Res.string.routine_home_add_button_content_description),
                )
            }
        },
    ) { paddingValues ->
        RoutineHomeContent(
            onEvent = onEvent,
            onRoutineListEvent = onRoutineListEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            routinePagingItems = routinePagingItems,
            filterUiStateProvider = filterUiStateProvider,
            calendarUiStateProvider = calendarUiStateProvider,
            holidayUiStateProvider = holidayUiStateProvider,
            syncUiStateProvider = syncUiStateProvider,
            bottomPadding = if (componentVisibleProvider().isAddButtonVisible) DiaryTheme.dimens.floatingActionButtonClearance else DiaryTheme.dimens.screenVerticalPadding,
        )
    }

    RoutineHomeDialogHost(
        onEvent = onEvent,
        state = state,
    )
}

@Composable
private fun RoutineHomeContent(
    onEvent: (RoutineHomeScaffoldEvent) -> Unit,
    onRoutineListEvent: (RoutineListEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: RoutineHomeScaffoldState = rememberRoutineHomeScaffoldState(),
    routinePagingItems: LazyPagingItems<RoutineSummary> = remember { flowOf(PagingData.empty<RoutineSummary>()) }.collectAsLazyPagingItems(),
    filterUiStateProvider: () -> RoutineHomeScaffoldFilterUiState = { RoutineHomeScaffoldFilterUiState() },
    calendarUiStateProvider: () -> RoutineHomeCalendarUiState = { RoutineHomeCalendarUiState() },
    holidayUiStateProvider: () -> RoutineHomeHolidayUiState = { RoutineHomeHolidayUiState() },
    syncUiStateProvider: () -> SyncRefreshUiState = { SyncRefreshUiState() },
    bottomPadding: Dp = DiaryTheme.dimens.screenVerticalPadding,
) {
    DiaryCrossfade(
        targetState = state.viewMode,
        modifier = modifier,
    ) { viewMode ->
        when (viewMode) {
            RoutineHomeViewMode.LIST ->
                RoutineHomeList(
                    onEvent = onEvent,
                    onRoutineListEvent = onRoutineListEvent,
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    routinePagingItems = routinePagingItems,
                    isRefreshingProvider = { syncUiStateProvider().isRefreshing },
                    filterUiStateProvider = filterUiStateProvider,
                    bottomPadding = bottomPadding,
                )

            RoutineHomeViewMode.CALENDAR ->
                RoutineHomeCalendar(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    calendarUiStateProvider = calendarUiStateProvider,
                    holidayUiStateProvider = holidayUiStateProvider,
                    isRefreshingProvider = { syncUiStateProvider().isRefreshing || holidayUiStateProvider().isFetching },
                    bottomPadding = bottomPadding,
                )
        }
    }
}

@Composable
private fun RoutineHomeDialogHost(
    onEvent: (RoutineHomeScaffoldEvent) -> Unit,
    state: RoutineHomeScaffoldState = rememberRoutineHomeScaffoldState(),
) {
    val coroutineScope = rememberCoroutineScope()

    DiaryDatePickerDialogHost(
        initialDateProvider = { state.datePickerInitialDate },
        onConfirm = { date ->
            coroutineScope.launch { state.calendarState.animateScrollTo(date.yearMonth) }
        },
        dialogState = state.datePickerDialogState,
    )
    RoutineAmountDialogHost(
        onConfirm = { record -> onEvent(RoutineHomeScaffoldEvent.AmountRecord(record = record)) },
        state = state.amountDialogState,
    )
}

private fun KeyEvent.isPreviousMonthShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.DirectionLeft

private fun KeyEvent.isNextMonthShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.DirectionRight

@ScreenPreview
@Composable
private fun RoutineHomeScaffoldPreview(
    @PreviewParameter(RoutineHomeViewModePreviewParameter::class) viewMode: RoutineHomeViewMode,
) {
    val pagingData = remember { flowOf(PagingData.from(listOf(previewRoutineSummary(title = "물 마시기"), previewRoutineSummary(title = "운동")))) }

    DiaryTheme {
        RoutineHomeScaffold(
            onEvent = {},
            onRoutineListEvent = {},
            state =
                rememberRoutineHomeScaffoldState(
                    viewModeState = rememberRoutineHomeViewModeState(initialViewMode = viewMode),
                    listState = rememberRoutineListState(initialToday = previewRoutineToday()),
                ),
            routinePagingItems = pagingData.collectAsLazyPagingItems(),
        )
    }
}
