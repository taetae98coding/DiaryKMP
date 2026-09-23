@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.home.holiday

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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class CalendarHomeHolidayViewModel(
    private val fetchHolidayUseCase: FetchHolidayUseCase,
    private val getCalendarHolidayUseCase: GetCalendarHolidayUseCase,
) : ViewModel() {
    private val yearMonth = MutableStateFlow<YearMonth?>(null)

    val isFetching: StateFlow<Boolean>
        field = MutableStateFlow(false)

    val holidayList: StateFlow<List<Holiday>> =
        yearMonth
            .map { yearMonth -> yearMonth?.holidayYearList().orEmpty() }
            .distinctUntilChanged()
            .flatMapLatest { yearList -> getHolidayList(yearList = yearList) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = emptyList(),
            )

    fun fetch(yearMonth: YearMonth) {
        this.yearMonth.value = yearMonth

        if (isFetching.value) return

        isFetching.value = true
        viewModelScope.launch {
            try {
                yearMonth.holidayYearList().forEach { year ->
                    fetchHolidayUseCase(parameter = year)
                }
            } finally {
                isFetching.value = false
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

private fun YearMonth.holidayYearList(): List<Int> =
    when (month) {
        Month.JANUARY, Month.FEBRUARY -> listOf(year - 1, year)
        Month.NOVEMBER, Month.DECEMBER -> listOf(year, year + 1)
        else -> listOf(year)
    }
