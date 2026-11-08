@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.timetable.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.dday.usecase.GetCalendarDDayUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class CalendarTimetableDDayViewModel(
    private val getCalendarDDayUseCase: GetCalendarDDayUseCase,
) : ViewModel() {
    private val fetchDateRange = MutableStateFlow<LocalDateRange?>(null)

    val uiState: StateFlow<CalendarTimetableDDayUiState> =
        fetchDateRange
            .flatMapLatest { dateRange ->
                if (dateRange == null) {
                    flowOf(emptyList())
                } else {
                    getCalendarDDayUseCase(parameter = dateRange)
                        .map { result -> result.getOrDefault(emptyList()) }
                }
            }.map { ddayList -> CalendarTimetableDDayUiState(ddayList = ddayList) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = CalendarTimetableDDayUiState(),
            )

    fun fetch(dateRange: LocalDateRange) {
        fetchDateRange.value = dateRange.calendarTimetableFetchDateRange()
    }
}
