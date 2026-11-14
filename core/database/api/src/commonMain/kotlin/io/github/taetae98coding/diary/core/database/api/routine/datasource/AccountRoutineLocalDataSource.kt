package io.github.taetae98coding.diary.core.database.api.routine.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCurrentCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountRoutineLocalDataSource {
    public fun page(
        accountId: Uuid,
        currentCycle: RoutineCurrentCycleLocalEntity,
    ): PagingSource<Int, RoutineSummaryLocalEntity>

    public fun pageStarted(
        accountId: Uuid,
        currentCycle: RoutineCurrentCycleLocalEntity,
        cycleList: List<RoutineCycleLocalEntity>,
    ): PagingSource<Int, RoutineSummaryLocalEntity>

    public fun pageFinished(
        accountId: Uuid,
        currentCycle: RoutineCurrentCycleLocalEntity,
    ): PagingSource<Int, RoutineSummaryLocalEntity>

    public fun getUnfinishedExists(accountId: Uuid): Flow<Boolean>

    public fun find(
        accountId: Uuid,
        routineId: Uuid,
    ): Flow<RoutineLocalEntity?>

    public fun findSummary(
        accountId: Uuid,
        routineId: Uuid,
        currentCycle: RoutineCurrentCycleLocalEntity,
    ): Flow<RoutineSummaryLocalEntity?>
}
