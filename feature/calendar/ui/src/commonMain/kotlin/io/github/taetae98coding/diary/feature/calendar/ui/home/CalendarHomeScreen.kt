package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.feature.calendar.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.CalendarHomeBirthdayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherViewModel
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import kotlinx.datetime.LocalDateRange
import kotlin.uuid.Uuid

@Composable
internal fun CalendarHomeScreen(
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToMemoAdd: (LocalDateRange) -> Unit,
    navigateToContactDetail: (Uuid) -> Unit,
    navigateToFilter: () -> Unit,
    navigateToTimetable: (CalendarTimetableNavKey) -> Unit,
    state: CalendarHomeScaffoldState,
    permissionManager: PermissionManager,
    holidayViewModel: CalendarHomeHolidayViewModel,
    memoViewModel: CalendarHomeMemoViewModel,
    birthdayViewModel: CalendarHomeBirthdayViewModel,
    weatherViewModel: CalendarHomeWeatherViewModel,
    syncViewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val holidayUiState by holidayViewModel.uiState.collectAsStateWithLifecycle()
    val memoUiState by memoViewModel.uiState.collectAsStateWithLifecycle()
    val birthdayUiState by birthdayViewModel.uiState.collectAsStateWithLifecycle()
    val filterUiState by memoViewModel.filterUiState.collectAsStateWithLifecycle()
    val weatherUiState by weatherViewModel.uiState.collectAsStateWithLifecycle()
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()

    UpdateTodayEffect(state = state)
    FetchHolidayEffect(
        state = state,
        holidayViewModel = holidayViewModel,
    )
    SelectMemoEffect(
        state = state,
        memoViewModel = memoViewModel,
    )
    FetchBirthdayEffect(
        state = state,
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
                navigateToContactDetail = navigateToContactDetail,
                navigateToFilter = navigateToFilter,
            )
        },
        onCalendarEvent = { event -> handleCalendarHomeCalendarEvent(event = event, state = state, navigateToMemoAdd = navigateToMemoAdd, navigateToTimetable = navigateToTimetable) },
    )
}
