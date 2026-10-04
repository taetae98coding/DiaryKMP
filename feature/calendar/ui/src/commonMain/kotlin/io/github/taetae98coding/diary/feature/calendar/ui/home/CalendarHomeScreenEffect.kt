package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.permission.RequestPermissionEffect
import io.github.taetae98coding.diary.core.permission.Permission
import io.github.taetae98coding.diary.core.permission.PermissionManager
import io.github.taetae98coding.diary.core.permission.PermissionResult
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.CalendarHomeBirthdayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

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
internal fun ScrollItemToTopOnFirstWeatherEffect(
    weatherViewModel: CalendarHomeWeatherViewModel,
    state: CalendarHomeScaffoldState,
) {
    // 처음 관찰한 날씨 목록이 비어 있다가 채워지는 순간에만 맨 위로 이동한다.
    // 화면이 보이지 않는 동안 도착했거나 한 번 처리한 뒤의 변화는 스크롤을 되돌리지 않는다.
    var isHandled by remember { mutableStateOf(false) }
    val firstWeatherFlow =
        remember(weatherViewModel) {
            flow {
                var hadWeather: Boolean? = null

                weatherViewModel
                    .uiState
                    .map { uiState -> uiState.weatherReport.weatherList.isNotEmpty() }
                    .distinctUntilChanged()
                    .collect { hasWeather ->
                        if (hadWeather == null && hasWeather) isHandled = true
                        if (!isHandled && hadWeather == false && hasWeather) emit(Unit)
                        hadWeather = hasWeather
                    }
            }
        }

    CollectEffect(effect = firstWeatherFlow) {
        isHandled = true
        state.calendarState.itemScrollState.scrollToTop()
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
