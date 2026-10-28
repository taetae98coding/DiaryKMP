package io.github.taetae98coding.diary.feature.calendar.ui.home.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.domain.weather.usecase.FetchCurrentWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.GetCurrentCalendarWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.RefreshCurrentWeatherUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class CalendarHomeWeatherViewModel(
    private val fetchCurrentWeatherUseCase: FetchCurrentWeatherUseCase,
    private val refreshCurrentWeatherUseCase: RefreshCurrentWeatherUseCase,
    private val getCurrentCalendarWeatherUseCase: GetCurrentCalendarWeatherUseCase,
) : ViewModel() {
    private val isLoading = MutableStateFlow(false)

    val uiState: StateFlow<CalendarHomeWeatherUiState> =
        combine(
            getCurrentCalendarWeatherUseCase(parameter = Unit)
                .map { result -> result.getOrDefault(CalendarWeatherReport()) },
            isLoading,
        ) { weatherReport, isLoading ->
            CalendarHomeWeatherUiState(
                weatherReport = weatherReport,
                isLoading = isLoading,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = CalendarHomeWeatherUiState(),
        )

    private var loadJob: Job? = null
    private var isRefreshingOnLocationPermissionGranted = false

    fun fetch() {
        if (isLoading.value) return

        load { fetchCurrentWeatherUseCase(parameter = Unit) }
    }

    fun refreshOnLocationPermissionGranted() {
        if (isRefreshingOnLocationPermissionGranted) return
        isRefreshingOnLocationPermissionGranted = true
        loadJob?.cancel()

        load {
            try {
                refreshCurrentWeatherUseCase(parameter = Unit)
            } finally {
                isRefreshingOnLocationPermissionGranted = false
            }
        }
    }

    private fun load(block: suspend () -> Unit) {
        isLoading.value = true

        // 시작하기 전에 loadJob에 담아 두어야, 멈춘 이전 작업이 뒤늦게 끝나며 새 작업의 진행 상태를 내리지 않는다.
        val job =
            viewModelScope.launch(start = CoroutineStart.LAZY) {
                try {
                    block()
                } finally {
                    if (loadJob == coroutineContext.job) {
                        isLoading.value = false
                    }
                }
            }

        loadJob = job
        job.start()
    }
}
