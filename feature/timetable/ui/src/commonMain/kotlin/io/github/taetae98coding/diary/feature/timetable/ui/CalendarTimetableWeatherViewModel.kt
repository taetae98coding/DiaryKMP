package io.github.taetae98coding.diary.feature.timetable.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.weather.usecase.FetchCurrentWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.GetCurrentCalendarWeatherUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class CalendarTimetableWeatherViewModel(
    private val fetchCurrentWeatherUseCase: FetchCurrentWeatherUseCase,
    getCurrentCalendarWeatherUseCase: GetCurrentCalendarWeatherUseCase,
) : ViewModel() {
    private var isFetching = false

    val uiState: StateFlow<CalendarTimetableWeatherUiState> =
        getCurrentCalendarWeatherUseCase()
            .map { result -> CalendarTimetableWeatherUiState(weatherReport = result.getOrDefault(CalendarWeatherReport())) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = CalendarTimetableWeatherUiState(),
            )

    fun fetch() {
        if (isFetching) return
        isFetching = true

        viewModelScope.launch {
            try {
                fetchCurrentWeatherUseCase()
            } finally {
                isFetching = false
            }
        }
    }
}
