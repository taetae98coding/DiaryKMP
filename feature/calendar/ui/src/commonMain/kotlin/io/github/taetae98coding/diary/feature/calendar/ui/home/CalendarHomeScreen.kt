package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.permission.RequestPermissionEffect
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.core.permission.PermissionResult
import io.github.taetae98coding.diary.feature.calendar.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.CalendarHomeBirthdayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateRange
import org.koin.compose.viewmodel.koinViewModel
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
    syncViewModel: CalendarHomeSyncViewModel,
    modifier: Modifier = Modifier,
) {
    val holidayList by holidayViewModel.holidayList.collectAsStateWithLifecycle()
    val memoList by memoViewModel.memoList.collectAsStateWithLifecycle()
    val birthdayList by birthdayViewModel.birthdayList.collectAsStateWithLifecycle()
    val filterUiState by memoViewModel.filterUiState.collectAsStateWithLifecycle()
    val weatherReport by weatherViewModel.weatherReport.collectAsStateWithLifecycle()
    val isSyncRefreshing by syncViewModel.isRefreshing.collectAsStateWithLifecycle()
    val isHolidayFetching by holidayViewModel.isFetching.collectAsStateWithLifecycle()
    val isWeatherLoading by weatherViewModel.isLoading.collectAsStateWithLifecycle()

    UpdateTodayEffect(state = state)
    FetchHolidayEffect(
        state = state,
        holidayViewModel = holidayViewModel,
    )
    FetchMemoEffect(
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
        weatherProvider = { weatherReport },
        holidayProvider = { holidayList },
        memoProvider = { memoList },
        birthdayProvider = { birthdayList },
        filterUiStateProvider = { filterUiState },
        uiStateProvider = {
            CalendarHomeScaffoldUiState(
                isRefreshing = isSyncRefreshing || isHolidayFetching || isWeatherLoading,
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

@Composable
private fun UpdateTodayEffect(state: CalendarHomeScaffoldState = rememberCalendarHomeScaffoldState()) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        state.updateToday()
    }
}

@Composable
private fun FetchHolidayEffect(
    holidayViewModel: CalendarHomeHolidayViewModel,
    state: CalendarHomeScaffoldState = rememberCalendarHomeScaffoldState(),
) {
    LaunchedEffect(state, holidayViewModel) {
        snapshotFlow { state.calendarState.currentYearMonth }
            .collect { yearMonth ->
                holidayViewModel.fetch(yearMonth = yearMonth)
            }
    }
}

@Composable
private fun FetchWeatherEffect(weatherViewModel: CalendarHomeWeatherViewModel) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        weatherViewModel.fetch()
    }
}

@Composable
private fun ScrollItemToTopOnFirstWeatherEffect(
    weatherViewModel: CalendarHomeWeatherViewModel,
    state: CalendarHomeScaffoldState = rememberCalendarHomeScaffoldState(),
) {
    LaunchedEffect(state, weatherViewModel) {
        val hasWeatherFlow =
            weatherViewModel
                .weatherReport
                .map { report -> report.weatherList.isNotEmpty() }
                .distinctUntilChanged()

        if (hasWeatherFlow.first()) return@LaunchedEffect

        hasWeatherFlow.first { hasWeather -> hasWeather }
        state.calendarState.itemScrollState.scrollToTop()
    }
}

@Composable
private fun RequestLocationPermissionEffect(
    weatherViewModel: CalendarHomeWeatherViewModel,
    permissionManager: PermissionManager = rememberPermissionManager(),
) {
    RequestPermissionEffect(
        permission = Permission.LOCATION,
        onResult = { result ->
            if (result == PermissionResult.GRANTED) {
                weatherViewModel.refreshOnLocationPermissionGranted()
            }
        },
        permissionManager = permissionManager,
    )
}

@Composable
private fun FetchMemoEffect(
    memoViewModel: CalendarHomeMemoViewModel,
    state: CalendarHomeScaffoldState = rememberCalendarHomeScaffoldState(),
) {
    LaunchedEffect(state, memoViewModel) {
        snapshotFlow { state.calendarState.currentYearMonth }
            .collect { yearMonth ->
                memoViewModel.fetch(yearMonth = yearMonth)
            }
    }
}

@Composable
private fun FetchBirthdayEffect(
    birthdayViewModel: CalendarHomeBirthdayViewModel,
    state: CalendarHomeScaffoldState = rememberCalendarHomeScaffoldState(),
) {
    LaunchedEffect(state, birthdayViewModel) {
        snapshotFlow { state.calendarState.currentYearMonth }
            .collect { yearMonth ->
                birthdayViewModel.fetch(yearMonth = yearMonth)
            }
    }
}
