package io.github.taetae98coding.diary.domain.search.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.RoutineCurrentCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import kotlinx.coroutines.flow.Flow

public interface AccountSearchRoutineRepository {
    public fun page(
        account: Account,
        query: String,
        currentCycle: RoutineCurrentCycle,
    ): Flow<PagingData<RoutineSummary>>
}
