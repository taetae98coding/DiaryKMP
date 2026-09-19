@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.MoveMemoUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.YearMonth
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class CalendarHomeMemoViewModel(
    getCalendarFilterUseCase: GetCalendarFilterUseCase,
    private val getCalendarMemoUseCase: GetCalendarMemoUseCase,
    private val moveMemoUseCase: MoveMemoUseCase,
) : ViewModel() {
    private val yearMonth = MutableStateFlow<YearMonth?>(null)

    val filterUiState: StateFlow<CalendarHomeScaffoldFilterUiState> =
        getCalendarFilterUseCase(parameter = Unit)
            .map { result ->
                CalendarHomeScaffoldFilterUiState(
                    isApplied = result.getOrDefault(emptyList()).isNotEmpty(),
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = CalendarHomeScaffoldFilterUiState(),
            )

    val memoList: StateFlow<List<CalendarMemo>> =
        yearMonth
            .map { yearMonth -> yearMonth?.calendarHomeFetchDateRange() }
            .distinctUntilChanged()
            .flatMapLatest { dateRange ->
                if (dateRange == null) {
                    flowOf(emptyList())
                } else {
                    getCalendarMemoUseCase(parameter = dateRange)
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

    fun move(
        id: Uuid,
        fromDateTime: MemoDateTime,
        toDateRange: LocalDateRange,
    ) {
        viewModelScope.launch {
            moveMemoUseCase(
                parameter =
                    MoveMemoUseCase.Parameter(
                        id = id,
                        fromDateTime = fromDateTime,
                        toDateRange = toDateRange,
                    ),
            )
        }
    }
}
