@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.domain.contact.usecase.GetCalendarContactBirthdayUseCase
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
internal class CalendarHomeBirthdayViewModel(
    private val getCalendarContactBirthdayUseCase: GetCalendarContactBirthdayUseCase,
) : ViewModel() {
    private val yearMonth = MutableStateFlow<YearMonth?>(null)

    val birthdayList: StateFlow<List<CalendarContactBirthday>> =
        yearMonth
            .map { yearMonth -> yearMonth?.calendarHomeFetchDateRange() }
            .distinctUntilChanged()
            .flatMapLatest { dateRange ->
                if (dateRange == null) {
                    flowOf(emptyList())
                } else {
                    getCalendarContactBirthdayUseCase(parameter = dateRange)
                        .map { result -> result.getOrDefault(emptyList()) }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = emptyList(),
            )

    fun fetch(yearMonth: YearMonth) {
        this.yearMonth.value = yearMonth
    }
}
