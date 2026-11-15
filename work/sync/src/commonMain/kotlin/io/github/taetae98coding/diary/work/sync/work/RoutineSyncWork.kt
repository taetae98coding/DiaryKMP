package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.routine.datasource.AccountRoutineSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routine.transaction.AccountRoutineSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.routine.datasource.RoutineRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class RoutineSyncWork(
    private val accountRoutineSyncLocalDataSource: AccountRoutineSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountRoutineSyncTransaction: AccountRoutineSyncTransaction,
    private val routineRemoteDataSource: RoutineRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountRoutineSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> routineRemoteDataSource.push(routineList = chunk.map { routine -> routine.toRemote() }) },
            clearPending = { chunk -> accountRoutineSyncTransaction.clearPending(accountId = accountId, routineList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.ROUTINE,
            pull = { cursor -> routineRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountRoutineSyncTransaction.upsert(
                    accountId = accountId,
                    routineList = pullList.map { pull -> pull.routine.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
