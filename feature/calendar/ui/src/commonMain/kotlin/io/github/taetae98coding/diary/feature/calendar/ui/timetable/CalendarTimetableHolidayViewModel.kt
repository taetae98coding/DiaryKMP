@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetCalendarHolidayUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class CalendarTimetableHolidayViewModel(
    private val fetchHolidayUseCase: FetchHolidayUseCase,
    private val getCalendarHolidayUseCase: GetCalendarHolidayUseCase,
) : ViewModel() {
    private val yearList = MutableStateFlow<List<Int>>(emptyList())
    private var isFetching = false

    val holidayList: StateFlow<List<Holiday>> =
        yearList
            .flatMapLatest { yearList -> getHolidayList(yearList = yearList) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = emptyList(),
            )

    fun fetch(dateRange: LocalDateRange) {
        val yearList = dateRange.calendarTimetableHolidayYearList()

        this.yearList.value = yearList

        if (isFetching) return

        isFetching = true
        viewModelScope.launch {
            try {
                yearList.forEach { year -> fetchHolidayUseCase(parameter = year) }
            } finally {
                isFetching = false
            }
        }
    }

    private fun getHolidayList(yearList: List<Int>): Flow<List<Holiday>> {
        if (yearList.isEmpty()) return flowOf(emptyList())

        return combine(yearList.map { year -> getCalendarHolidayUseCase(parameter = year) }) { resultList ->
            resultList.flatMap { result -> result.getOrDefault(emptyList()) }
        }
    }
}
