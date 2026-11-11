@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.timetable.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.contact.usecase.GetCalendarContactBirthdayUseCase
import io.github.taetae98coding.diary.domain.lunar.usecase.FetchLunarUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class CalendarTimetableBirthdayViewModel(
    private val fetchLunarUseCase: FetchLunarUseCase,
    private val getCalendarContactBirthdayUseCase: GetCalendarContactBirthdayUseCase,
) : ViewModel() {
    private val fetchDateRange = MutableStateFlow<LocalDateRange?>(null)
    private val fetchingYearSet = mutableSetOf<Int>()

    val uiState: StateFlow<CalendarTimetableBirthdayUiState> =
        fetchDateRange
            .flatMapLatest { dateRange ->
                if (dateRange == null) {
                    flowOf(emptyList())
                } else {
                    getCalendarContactBirthdayUseCase(parameter = dateRange)
                        .map { result -> result.getOrDefault(emptyList()) }
                }
            }.map { birthdayList -> CalendarTimetableBirthdayUiState(birthdayList = birthdayList) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = CalendarTimetableBirthdayUiState(),
            )

    fun fetch(dateRange: LocalDateRange) {
        val fetchDateRange = dateRange.calendarTimetableFetchDateRange()

        this.fetchDateRange.value = fetchDateRange

        for (year in fetchDateRange.start.year..fetchDateRange.endInclusive.year) {
            if (!fetchingYearSet.add(year)) continue

            viewModelScope.launch {
                try {
                    fetchLunarUseCase(parameter = year)
                } finally {
                    fetchingYearSet.remove(year)
                }
            }
        }
    }
}
