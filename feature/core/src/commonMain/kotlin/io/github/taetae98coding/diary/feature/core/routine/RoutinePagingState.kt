@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.core.routine

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.core.paging.LOADED_EMPTY_LOAD_STATES
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

public class RoutinePagingState(
    pageRoutine: (today: LocalDate) -> Flow<Result<PagingData<RoutineSummary>>>,
) {
    private val today = MutableStateFlow<LocalDate?>(null)

    public val routinePagingData: Flow<PagingData<RoutineSummary>> =
        today.flatMapLatest { today ->
            if (today == null) {
                flowOf(PagingData.empty())
            } else {
                pageRoutine(today)
                    .map { result -> result.getOrElse { PagingData.empty(sourceLoadStates = LOADED_EMPTY_LOAD_STATES) } }
            }
        }

    public fun updateToday(today: LocalDate) {
        this.today.value = today
    }
}
