package io.github.taetae98coding.diary.domain.routine.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCurrentCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountRoutineRepository {
    public fun page(
        account: Account,
        currentCycle: RoutineCurrentCycle,
    ): Flow<PagingData<RoutineSummary>>

    public fun pageStarted(
        account: Account,
        currentCycle: RoutineCurrentCycle,
        cycleSet: Set<RoutineCycle>,
    ): Flow<PagingData<RoutineSummary>>

    public fun pageFinished(
        account: Account,
        currentCycle: RoutineCurrentCycle,
    ): Flow<PagingData<RoutineSummary>>

    public fun getUnfinishedExists(account: Account): Flow<Boolean>

    public fun find(
        account: Account,
        routineId: Uuid,
    ): Flow<Routine?>

    public fun findSummary(
        account: Account,
        routineId: Uuid,
        currentCycle: RoutineCurrentCycle,
    ): Flow<RoutineSummary?>

    public suspend fun upsert(
        account: Account,
        routine: Routine,
        tagIdSet: Set<Uuid>,
    )

    public suspend fun updateDetail(
        account: Account,
        routineId: Uuid,
        detail: RoutineDetail,
        updatedAt: Instant,
    ): Int

    public suspend fun updateFinished(
        account: Account,
        routineId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        account: Account,
        routineId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
