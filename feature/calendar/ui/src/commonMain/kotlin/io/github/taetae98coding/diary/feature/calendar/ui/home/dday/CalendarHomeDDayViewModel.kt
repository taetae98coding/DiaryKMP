@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.home.dday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.dday.usecase.GetCalendarDDayUseCase
import io.github.taetae98coding.diary.feature.calendar.ui.home.calendarHomeFetchDateRange
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class CalendarHomeDDayViewModel(
    private val getCalendarDDayUseCase: GetCalendarDDayUseCase,
) : ViewModel() {
    private val yearMonth = MutableStateFlow<YearMonth?>(null)

    val uiState: StateFlow<CalendarHomeDDayUiState> =
        yearMonth
            .map { yearMonth -> yearMonth?.calendarHomeFetchDateRange() }
            .distinctUntilChanged()
            .flatMapLatest { dateRange ->
                if (dateRange == null) {
                    flowOf(emptyList())
                } else {
                    getCalendarDDayUseCase(parameter = dateRange)
                        .map { result -> result.getOrDefault(emptyList()) }
                }
            }.map { ddayList -> CalendarHomeDDayUiState(ddayList = ddayList) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = CalendarHomeDDayUiState(),
            )

    fun fetch(yearMonth: YearMonth) {
        this.yearMonth.value = yearMonth
    }
}
