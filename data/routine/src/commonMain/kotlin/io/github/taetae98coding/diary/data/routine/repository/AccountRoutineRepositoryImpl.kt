package io.github.taetae98coding.diary.data.routine.repository

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.database.api.routine.datasource.AccountRoutineLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routine.transaction.AccountRoutineTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCurrentCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.data.core.mapper.toDomain
import io.github.taetae98coding.diary.data.core.mapper.toEntityTagLocalList
import io.github.taetae98coding.diary.data.core.mapper.toLocal
import io.github.taetae98coding.diary.data.core.paging.pagingFlow
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountRoutineRepositoryImpl(
    private val accountRoutineLocalDataSource: AccountRoutineLocalDataSource,
    private val accountRoutineTransaction: AccountRoutineTransaction,
) : AccountRoutineRepository {
    override fun page(
        account: Account,
        currentCycle: RoutineCurrentCycle,
    ): Flow<PagingData<RoutineSummary>> =
        pagingFlow(
            pagingSourceFactory = {
                accountRoutineLocalDataSource.page(
                    accountId = account.id,
                    currentCycle = currentCycle.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )

    override fun pageStarted(
        account: Account,
        currentCycle: RoutineCurrentCycle,
        cycleSet: Set<RoutineCycle>,
    ): Flow<PagingData<RoutineSummary>> =
        pagingFlow(
            pagingSourceFactory = {
                accountRoutineLocalDataSource.pageStarted(
                    accountId = account.id,
                    currentCycle = currentCycle.toLocal(),
                    cycleList = cycleSet.map { cycle -> cycle.toLocal() },
                )
            },
            transform = { local -> local.toDomain() },
        )

    override fun pageFinished(
        account: Account,
        currentCycle: RoutineCurrentCycle,
    ): Flow<PagingData<RoutineSummary>> =
        pagingFlow(
            pagingSourceFactory = {
                accountRoutineLocalDataSource.pageFinished(
                    accountId = account.id,
                    currentCycle = currentCycle.toLocal(),
                )
            },
            transform = { local -> local.toDomain() },
        )

    override fun getUnfinishedExists(account: Account): Flow<Boolean> = accountRoutineLocalDataSource.getUnfinishedExists(accountId = account.id)

    override fun find(
        account: Account,
        routineId: Uuid,
    ): Flow<Routine?> =
        accountRoutineLocalDataSource
            .find(
                accountId = account.id,
                routineId = routineId,
            ).map { local -> local?.toDomain() }

    override fun findSummary(
        account: Account,
        routineId: Uuid,
        currentCycle: RoutineCurrentCycle,
    ): Flow<RoutineSummary?> =
        accountRoutineLocalDataSource
            .findSummary(
                accountId = account.id,
                routineId = routineId,
                currentCycle = currentCycle.toLocal(),
            ).map { local -> local?.toDomain() }

    override suspend fun upsert(
        account: Account,
        routine: Routine,
        tagIdSet: Set<Uuid>,
    ) {
        accountRoutineTransaction.upsert(
            accountId = account.id,
            routine = routine.toLocal(),
            routineTagList = tagIdSet.toEntityTagLocalList(entityId = routine.id, updatedAt = routine.updatedAt, createdAt = routine.createdAt),
        )
    }

    override suspend fun updateDetail(
        account: Account,
        routineId: Uuid,
        detail: RoutineDetail,
        updatedAt: Instant,
    ): Int =
        accountRoutineTransaction.updateDetail(
            accountId = account.id,
            routineId = routineId,
            detail = detail.toLocal(),
            updatedAt = updatedAt,
        )

    override suspend fun updateFinished(
        account: Account,
        routineId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int =
        accountRoutineTransaction.updateFinished(
            accountId = account.id,
            routineId = routineId,
            isFinished = isFinished,
            updatedAt = updatedAt,
        )

    override suspend fun updateDeleted(
        account: Account,
        routineId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        accountRoutineTransaction.updateDeleted(
            accountId = account.id,
            routineId = routineId,
            isDeleted = isDeleted,
            updatedAt = updatedAt,
        )
}
