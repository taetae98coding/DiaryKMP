package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.CalendarHomeBirthdayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.dday.CalendarHomeDDayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherViewModel
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.timetable.api.CalendarTimetableNavKey
import kotlinx.datetime.LocalDateRange
import kotlin.uuid.Uuid

@Composable
internal fun CalendarHomeScreen(
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToDDayDetail: (Uuid) -> Unit,
    navigateToMemoAdd: (LocalDateRange) -> Unit,
    navigateToContactDetail: (Uuid) -> Unit,
    navigateToFilter: () -> Unit,
    navigateToTimetable: (CalendarTimetableNavKey) -> Unit,
    state: CalendarHomeScaffoldState,
    permissionManager: PermissionManager,
    holidayViewModel: CalendarHomeHolidayViewModel,
    memoViewModel: CalendarHomeMemoViewModel,
    ddayViewModel: CalendarHomeDDayViewModel,
    birthdayViewModel: CalendarHomeBirthdayViewModel,
    weatherViewModel: CalendarHomeWeatherViewModel,
    syncViewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val holidayUiState by holidayViewModel.uiState.collectAsStateWithLifecycle()
    val memoUiState by memoViewModel.uiState.collectAsStateWithLifecycle()
    val ddayUiState by ddayViewModel.uiState.collectAsStateWithLifecycle()
    val birthdayUiState by birthdayViewModel.uiState.collectAsStateWithLifecycle()
    val filterUiState by memoViewModel.filterUiState.collectAsStateWithLifecycle()
    val weatherUiState by weatherViewModel.uiState.collectAsStateWithLifecycle()
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()

    UpdateTodayEffect(state = state)
    CalendarHomeMonthEffect(
        state = state,
        holidayViewModel = holidayViewModel,
        memoViewModel = memoViewModel,
        ddayViewModel = ddayViewModel,
        birthdayViewModel = birthdayViewModel,
    )
    FetchWeatherEffect(weatherViewModel = weatherViewModel)
    ScrollItemToTopOnFirstWeatherEffect(
        state = state,
        weatherViewModel = weatherViewModel,
    )
    RequestLocationPermissionEffect(
        permissionManager = permissionManager,
        weatherViewModel = weatherViewModel,
    )
    CalendarHomeScaffold(
        modifier = modifier,
        state = state,
        weatherUiStateProvider = { weatherUiState },
        holidayUiStateProvider = { holidayUiState },
        memoUiStateProvider = { memoUiState },
        ddayUiStateProvider = { ddayUiState },
        birthdayUiStateProvider = { birthdayUiState },
        filterUiStateProvider = { filterUiState },
        uiStateProvider = {
            CalendarHomeScaffoldUiState(
                isRefreshing = syncUiState.isRefreshing || holidayUiState.isFetching || weatherUiState.isLoading,
            )
        },
        onEvent = { event ->
            handleCalendarHomeEvent(
                event = event,
                state = state,
                holidayViewModel = holidayViewModel,
                memoViewModel = memoViewModel,
                weatherViewModel = weatherViewModel,
                syncViewModel = syncViewModel,
                navigateToMemoDetail = navigateToMemoDetail,
                navigateToDDayDetail = navigateToDDayDetail,
                navigateToContactDetail = navigateToContactDetail,
                navigateToFilter = navigateToFilter,
            )
        },
        onCalendarEvent = { event -> handleCalendarHomeCalendarEvent(event = event, state = state, navigateToMemoAdd = navigateToMemoAdd, navigateToTimetable = navigateToTimetable) },
    )
}

@Composable
private fun CalendarHomeMonthEffect(
    state: CalendarHomeScaffoldState,
    holidayViewModel: CalendarHomeHolidayViewModel,
    memoViewModel: CalendarHomeMemoViewModel,
    ddayViewModel: CalendarHomeDDayViewModel,
    birthdayViewModel: CalendarHomeBirthdayViewModel,
) {
    FetchHolidayEffect(
        state = state,
        holidayViewModel = holidayViewModel,
    )
    SelectMemoEffect(
        state = state,
        memoViewModel = memoViewModel,
    )
    FetchDDayEffect(
        state = state,
        ddayViewModel = ddayViewModel,
    )
    FetchBirthdayEffect(
        state = state,
        birthdayViewModel = birthdayViewModel,
    )
}
