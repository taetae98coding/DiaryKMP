package io.github.taetae98coding.diary.feature.calendar.ui.home

import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.model.weather.WeatherCondition
import io.github.taetae98coding.diary.core.model.weather.WeatherTemperature
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.CalendarHomeBirthdayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

internal fun weatherViewModel(
    weatherReportFlow: StateFlow<CalendarWeatherReport> = MutableStateFlow(CalendarWeatherReport()),
    isLoadingFlow: StateFlow<Boolean> = MutableStateFlow(false),
): CalendarHomeWeatherViewModel =
    mockk<CalendarHomeWeatherViewModel>().also { viewModel ->
        every { viewModel.fetch() } returns Unit
        every { viewModel.refreshOnLocationPermissionGranted() } returns Unit
        every { viewModel.weatherReport } returns weatherReportFlow
        every { viewModel.isLoading } returns isLoadingFlow
    }

internal fun holidayViewModel(
    holidayListFlow: StateFlow<List<Holiday>> = MutableStateFlow(emptyList()),
    isFetchingFlow: StateFlow<Boolean> = MutableStateFlow(false),
): CalendarHomeHolidayViewModel =
    mockk<CalendarHomeHolidayViewModel>().also { viewModel ->
        every { viewModel.fetch(any()) } returns Unit
        every { viewModel.holidayList } returns holidayListFlow
        every { viewModel.isFetching } returns isFetchingFlow
    }

internal fun birthdayViewModel(birthdayListFlow: StateFlow<List<CalendarContactBirthday>> = MutableStateFlow(emptyList())): CalendarHomeBirthdayViewModel =
    mockk<CalendarHomeBirthdayViewModel>().also { viewModel ->
        every { viewModel.fetch(any()) } returns Unit
        every { viewModel.birthdayList } returns birthdayListFlow
    }

internal fun syncViewModel(isRefreshingFlow: StateFlow<Boolean> = MutableStateFlow(false)): CalendarHomeSyncViewModel =
    mockk<CalendarHomeSyncViewModel>().also { viewModel ->
        every { viewModel.refresh() } returns Unit
        every { viewModel.isRefreshing } returns isRefreshingFlow
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
