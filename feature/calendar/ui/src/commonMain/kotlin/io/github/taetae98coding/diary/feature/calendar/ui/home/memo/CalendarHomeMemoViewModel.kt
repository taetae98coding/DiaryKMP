@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.home.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.MoveMemoUseCase
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeScaffoldFilterUiState
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
    private val movingIdSet = mutableSetOf<Uuid>()

    val filterUiState: StateFlow<CalendarHomeScaffoldFilterUiState> =
        getCalendarFilterUseCase(parameter = Unit)
            .map { result ->
                CalendarHomeScaffoldFilterUiState(
                    isApplied = result.getOrDefault(emptyList()).isNotEmpty(),
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = CalendarHomeScaffoldFilterUiState(),
            )

    val uiState: StateFlow<CalendarHomeMemoUiState> =
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
            }.map { memoList -> CalendarHomeMemoUiState(memoList = memoList) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = CalendarHomeMemoUiState(),
            )

    fun select(yearMonth: YearMonth) {
        this.yearMonth.value = yearMonth
    }

    fun move(
        id: Uuid,
        fromDateTime: MemoDateTime,
        toDateRange: LocalDateRange,
    ) {
        if (!movingIdSet.add(id)) return

        viewModelScope.launch {
            try {
                moveMemoUseCase(
                    parameter =
                        MoveMemoUseCase.Parameter(
                            id = id,
                            fromDateTime = fromDateTime,
                            toDateRange = toDateRange,
                        ),
                )
            } finally {
                movingIdSet.remove(id)
            }
        }
    }
}
