package io.github.taetae98coding.diary.core.database.api.search.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCurrentCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import kotlin.uuid.Uuid

public interface SearchRoutineLocalDataSource {
    public fun page(
        accountId: Uuid,
        query: String,
        currentCycle: RoutineCurrentCycleLocalEntity,
    ): PagingSource<Int, RoutineSummaryLocalEntity>
}
