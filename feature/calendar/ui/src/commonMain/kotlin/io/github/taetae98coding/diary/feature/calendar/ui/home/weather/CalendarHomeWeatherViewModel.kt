package io.github.taetae98coding.diary.feature.calendar.ui.home.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.domain.weather.usecase.FetchCurrentWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.GetCurrentCalendarWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.RefreshCurrentWeatherUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
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
                started = SharingStarted.WhileUiSubscribed,
                initialValue = CalendarWeatherReport(),
            )

    private var isRefreshPending: Boolean = false

    fun fetch() {
        if (isLoading.value) return

        load { fetchCurrentWeatherUseCase(parameter = Unit) }
    }

    fun refreshOnLocationPermissionGranted() {
        if (isLoading.value) {
            isRefreshPending = true
            return
        }

        load { refreshCurrentWeatherUseCase(parameter = Unit) }
    }

    private fun load(block: suspend () -> Unit) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                block()
                while (isRefreshPending) {
                    isRefreshPending = false
                    refreshCurrentWeatherUseCase(parameter = Unit)
                }
            } finally {
                isRefreshPending = false
                isLoading.value = false
            }
        }
    }
}
