package io.github.taetae98coding.diary.feature.calendar.ui.home.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.domain.weather.usecase.FetchCurrentWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.GetCurrentCalendarWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.RefreshCurrentWeatherUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class CalendarHomeWeatherViewModel(
    private val fetchCurrentWeatherUseCase: FetchCurrentWeatherUseCase,
    private val refreshCurrentWeatherUseCase: RefreshCurrentWeatherUseCase,
    private val getCurrentCalendarWeatherUseCase: GetCurrentCalendarWeatherUseCase,
) : ViewModel() {
    val isLoading: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val weatherReport: StateFlow<CalendarWeatherReport> =
        getCurrentCalendarWeatherUseCase(parameter = Unit)
            .map { result -> result.getOrDefault(CalendarWeatherReport()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = CalendarWeatherReport(),
            )

    fun fetch() {
        load { fetchCurrentWeatherUseCase(parameter = Unit) }
    }

    fun refreshOnLocationPermissionGranted() {
        load { refreshCurrentWeatherUseCase(parameter = Unit) }
    }

    private fun load(block: suspend () -> Unit) {
        if (isLoading.value) return

        isLoading.value = true
        viewModelScope.launch {
            try {
                block()
            } finally {
                isLoading.value = false
            }
        }
    }
}
