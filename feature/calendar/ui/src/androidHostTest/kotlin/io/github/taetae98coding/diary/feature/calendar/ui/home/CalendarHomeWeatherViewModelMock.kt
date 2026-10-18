package io.github.taetae98coding.diary.feature.calendar.ui.home

import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.model.weather.WeatherCondition
import io.github.taetae98coding.diary.core.model.weather.WeatherTemperature
import io.github.taetae98coding.diary.domain.contact.usecase.GetCalendarContactBirthdayUseCase
import io.github.taetae98coding.diary.domain.lunar.usecase.FetchLunarUseCase
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.CalendarHomeBirthdayUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.CalendarHomeBirthdayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherViewModel
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

internal fun weatherViewModel(
    weatherReportFlow: StateFlow<CalendarWeatherReport> = MutableStateFlow(CalendarWeatherReport()),
    isLoadingFlow: StateFlow<Boolean> = MutableStateFlow(false),
): CalendarHomeWeatherViewModel =
    mockk<CalendarHomeWeatherViewModel>().also { viewModel ->
        every { viewModel.fetch() } returns Unit
        every { viewModel.refreshOnLocationPermissionGranted() } returns Unit
        every { viewModel.uiState } returns
            combinedStateFlow(weatherReportFlow, isLoadingFlow) { weatherReport, isLoading ->
                CalendarHomeWeatherUiState(weatherReport = weatherReport, isLoading = isLoading)
            }
    }

internal fun holidayViewModel(
    holidayListFlow: StateFlow<List<Holiday>> = MutableStateFlow(emptyList()),
    isFetchingFlow: StateFlow<Boolean> = MutableStateFlow(false),
): CalendarHomeHolidayViewModel =
    mockk<CalendarHomeHolidayViewModel>().also { viewModel ->
        every { viewModel.fetch(any()) } returns Unit
        every { viewModel.uiState } returns
            combinedStateFlow(holidayListFlow, isFetchingFlow) { holidayList, isFetching ->
                CalendarHomeHolidayUiState(holidayList = holidayList, isFetching = isFetching)
            }
    }

internal fun birthdayViewModel(birthdayListFlow: StateFlow<List<CalendarContactBirthday>> = MutableStateFlow(emptyList())): CalendarHomeBirthdayViewModel =
    mockk<CalendarHomeBirthdayViewModel>().also { viewModel ->
        every { viewModel.fetch(any()) } returns Unit
        every { viewModel.uiState } returns
            birthdayListFlow.mapState { birthdayList -> CalendarHomeBirthdayUiState(birthdayList = birthdayList) }
    }

internal fun lunarObservingBirthdayViewModel(fetchLunarUseCase: FetchLunarUseCase): CalendarHomeBirthdayViewModel {
    val getCalendarContactBirthdayUseCase = mockk<GetCalendarContactBirthdayUseCase>()
    every { getCalendarContactBirthdayUseCase(parameter = any()) } returns flowOf(Result.success(emptyList()))

    return CalendarHomeBirthdayViewModel(
        fetchLunarUseCase = fetchLunarUseCase,
        getCalendarContactBirthdayUseCase = getCalendarContactBirthdayUseCase,
    )
}

internal fun fetchLunarUseCase(): FetchLunarUseCase =
    mockk<FetchLunarUseCase>().also { useCase ->
        coEvery { useCase(parameter = any()) } returns Result.success(emptyList())
    }

internal fun syncViewModel(isRefreshingFlow: StateFlow<Boolean> = MutableStateFlow(false)): SyncRefreshViewModel =
    mockk<SyncRefreshViewModel>().also { viewModel ->
        every { viewModel.refresh() } returns Unit
        every { viewModel.uiState } returns isRefreshingFlow.mapState { isRefreshing -> SyncRefreshUiState(isRefreshing = isRefreshing) }
    }

internal fun calendarWeather(
    date: LocalDate,
    temperature: CalendarWeatherTemperature,
    descriptionList: List<String>,
    imageUrlList: List<String> = descriptionList.indices.map { index -> "https://example.com/weather/$index.png" },
): CalendarWeather =
    CalendarWeather(
        date = date,
        temperature = temperature,
        weatherList =
            descriptionList.zip(imageUrlList).map { (description, imageUrl) ->
                forecast(
                    description = description,
                    imageUrl = imageUrl,
                )
            },
    )

private fun forecast(
    description: String,
    imageUrl: String,
): Weather =
    Weather(
        dateTime = Instant.fromEpochSeconds(0),
        conditionList =
            listOf(
                WeatherCondition(
                    description = description,
                    imageUrl = imageUrl,
                ),
            ),
        temperature = WeatherTemperature(current = 0.0, min = 0.0, max = 0.0),
    )

internal fun <T, R> StateFlow<T>.mapState(transform: (T) -> R): StateFlow<R> =
    object : StateFlow<R> {
        override val value: R get() = transform(this@mapState.value)
        override val replayCache: List<R> get() = listOf(value)

        override suspend fun collect(collector: FlowCollector<R>): Nothing {
            this@mapState.map(transform).distinctUntilChanged().collect(collector)
            awaitCancellation()
        }
    }

internal fun <A, B, R> combinedStateFlow(
    first: StateFlow<A>,
    second: StateFlow<B>,
    transform: (A, B) -> R,
): StateFlow<R> =
    object : StateFlow<R> {
        override val value: R get() = transform(first.value, second.value)
        override val replayCache: List<R> get() = listOf(value)

        override suspend fun collect(collector: FlowCollector<R>): Nothing {
            combine(first, second, transform).distinctUntilChanged().collect(collector)
            awaitCancellation()
        }
    }
