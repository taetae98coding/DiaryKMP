package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.domain.weather.usecase.FetchCurrentWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.GetCurrentCalendarWeatherUseCase
import io.github.taetae98coding.diary.feature.calendar.ui.home.mapState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDateRange

internal fun timetableWeatherViewModel(
    weatherReportFlow: MutableStateFlow<CalendarWeatherReport> = MutableStateFlow(CalendarWeatherReport()),
    onFetch: () -> Unit = {},
): CalendarTimetableWeatherViewModel =
    mockk<CalendarTimetableWeatherViewModel>().also { viewModel ->
        every { viewModel.fetch() } answers { onFetch() }
        every { viewModel.uiState } returns weatherReportFlow.mapState { report -> CalendarTimetableWeatherUiState(weatherReport = report) }
    }

internal fun calendarTimetableWeatherViewModel(
    fetchResult: Result<Unit> = Result.success(Unit),
    getCurrentCalendarWeatherUseCase: GetCurrentCalendarWeatherUseCase = getCurrentCalendarWeatherUseCase(),
): CalendarTimetableWeatherViewModel =
    CalendarTimetableWeatherViewModel(
        fetchCurrentWeatherUseCase =
            mockk<FetchCurrentWeatherUseCase>().also { useCase ->
                coEvery { useCase(parameter = Unit) } returns fetchResult
            },
        getCurrentCalendarWeatherUseCase = getCurrentCalendarWeatherUseCase,
    )

internal fun getCurrentCalendarWeatherUseCase(flow: Flow<Result<CalendarWeatherReport>> = flowOf(Result.success(CalendarWeatherReport()))): GetCurrentCalendarWeatherUseCase =
    mockk<GetCurrentCalendarWeatherUseCase>().also { useCase ->
        every { useCase(parameter = Unit) } returns flow
    }

internal fun timetableBirthdayViewModel(
    birthdayListFlow: MutableStateFlow<List<CalendarContactBirthday>> = MutableStateFlow(emptyList()),
    onFetch: (LocalDateRange) -> Unit = {},
): CalendarTimetableBirthdayViewModel =
    mockk<CalendarTimetableBirthdayViewModel>().also { viewModel ->
        every { viewModel.fetch(any()) } answers { onFetch(firstArg()) }
        every { viewModel.uiState } returns birthdayListFlow.mapState { list -> CalendarTimetableBirthdayUiState(birthdayList = list) }
    }
