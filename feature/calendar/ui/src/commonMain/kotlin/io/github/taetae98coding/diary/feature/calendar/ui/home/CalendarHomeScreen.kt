package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.feature.calendar.ui.permission.LocationPermissionRequestResult
import io.github.taetae98coding.diary.feature.calendar.ui.permission.LocationPermissionRequester
import io.github.taetae98coding.diary.feature.calendar.ui.permission.rememberLocationPermissionRequester
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
    state: CalendarHomeScaffoldState,
    locationPermissionRequester: LocationPermissionRequester,
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
        locationPermissionRequester = locationPermissionRequester,
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
                navigateToMemoAdd = navigateToMemoAdd,
                navigateToContactDetail = navigateToContactDetail,
                navigateToFilter = navigateToFilter,
            )
        },
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
    locationPermissionRequester: LocationPermissionRequester = rememberLocationPermissionRequester(),
) {
    LaunchedEffect(locationPermissionRequester, weatherViewModel) {
        if (locationPermissionRequester.request() == LocationPermissionRequestResult.GRANTED) {
            weatherViewModel.refreshOnLocationPermissionGranted()
        }
    }
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
