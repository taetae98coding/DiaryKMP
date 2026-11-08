package io.github.taetae98coding.diary.feature.timetable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.timetable.TimetableEvent
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import kotlin.uuid.Uuid

@Composable
internal fun CalendarTimetableScreen(
    navigateUp: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToDDayDetail: (Uuid) -> Unit,
    navigateToMemoAdd: (MemoDateTime) -> Unit,
    navigateToContactDetail: (Uuid) -> Unit,
    state: CalendarTimetableScaffoldState,
    memoViewModel: CalendarTimetableViewModel,
    holidayViewModel: CalendarTimetableHolidayViewModel,
    weatherViewModel: CalendarTimetableWeatherViewModel,
    ddayViewModel: CalendarTimetableDDayViewModel,
    birthdayViewModel: CalendarTimetableBirthdayViewModel,
    modifier: Modifier = Modifier,
) {
    val memoUiState by memoViewModel.uiState.collectAsStateWithLifecycle()
    val holidayUiState by holidayViewModel.uiState.collectAsStateWithLifecycle()
    val weatherUiState by weatherViewModel.uiState.collectAsStateWithLifecycle()
    val ddayUiState by ddayViewModel.uiState.collectAsStateWithLifecycle()
    val birthdayUiState by birthdayViewModel.uiState.collectAsStateWithLifecycle()

    UpdateNowEffect(state = state)
    SelectMemoEffect(
        state = state,
        memoViewModel = memoViewModel,
    )
    FetchHolidayEffect(
        state = state,
        holidayViewModel = holidayViewModel,
    )
    FetchWeatherEffect(weatherViewModel = weatherViewModel)
    FetchDDayEffect(
        state = state,
        ddayViewModel = ddayViewModel,
    )
    FetchBirthdayEffect(
        state = state,
        birthdayViewModel = birthdayViewModel,
    )
    CalendarTimetableScaffold(
        onEvent = { event ->
            when (event) {
                is CalendarTimetableScaffoldEvent.ClickNavigateUp -> navigateUp()
                is CalendarTimetableScaffoldEvent.ClickMemo -> navigateToMemoDetail(event.id)
                is CalendarTimetableScaffoldEvent.ClickDDay -> navigateToDDayDetail(event.id)
                is CalendarTimetableScaffoldEvent.ClickContactBirthday -> navigateToContactDetail(event.contactId)
            }
        },
        onTimetableEvent = { event ->
            when (event) {
                is TimetableEvent.SelectTime -> navigateToMemoAdd(MemoDateTime.DateTime(start = event.start, endInclusive = event.endInclusive))
                is TimetableEvent.SelectDate -> navigateToMemoAdd(MemoDateTime.AllDay(dateRange = event.dateRange))
            }
        },
        modifier = modifier,
        state = state,
        memoUiStateProvider = { memoUiState },
        ddayUiStateProvider = { ddayUiState },
        holidayUiStateProvider = { holidayUiState },
        weatherUiStateProvider = { weatherUiState },
        birthdayUiStateProvider = { birthdayUiState },
    )
}
