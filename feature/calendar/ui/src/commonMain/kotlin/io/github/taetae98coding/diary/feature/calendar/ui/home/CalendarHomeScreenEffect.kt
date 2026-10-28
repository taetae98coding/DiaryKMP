package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.taetae98coding.diary.compose.permission.RequestPermissionEffect
import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.core.permission.PermissionResult
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.CalendarHomeBirthdayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherViewModel

@Composable
internal fun UpdateTodayEffect(state: CalendarHomeScaffoldState) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        state.updateToday()
    }
}

@Composable
internal fun FetchHolidayEffect(
    holidayViewModel: CalendarHomeHolidayViewModel,
    state: CalendarHomeScaffoldState,
) {
    LaunchedEffect(state, holidayViewModel) {
        snapshotFlow { state.calendarState.currentYearMonth }
            .collect { yearMonth ->
                holidayViewModel.fetch(yearMonth = yearMonth)
            }
    }
}

@Composable
internal fun FetchWeatherEffect(weatherViewModel: CalendarHomeWeatherViewModel) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        weatherViewModel.fetch()
    }
}

@Composable
internal fun RequestLocationPermissionEffect(
    weatherViewModel: CalendarHomeWeatherViewModel,
    permissionManager: PermissionManager,
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
internal fun SelectMemoEffect(
    memoViewModel: CalendarHomeMemoViewModel,
    state: CalendarHomeScaffoldState,
) {
    LaunchedEffect(state, memoViewModel) {
        snapshotFlow { state.calendarState.currentYearMonth }
            .collect { yearMonth ->
                memoViewModel.select(yearMonth = yearMonth)
            }
    }
}

@Composable
internal fun FetchBirthdayEffect(
    birthdayViewModel: CalendarHomeBirthdayViewModel,
    state: CalendarHomeScaffoldState,
) {
    LaunchedEffect(state, birthdayViewModel) {
        snapshotFlow { state.calendarState.currentYearMonth }
            .collect { yearMonth ->
                birthdayViewModel.fetch(yearMonth = yearMonth)
            }
    }
}
