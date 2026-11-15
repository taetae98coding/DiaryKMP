package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.routinerecord.datasource.AccountRoutineRecordSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.routinerecord.transaction.AccountRoutineRecordSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.routinerecord.datasource.RoutineRecordRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class RoutineRecordSyncWork(
    private val accountRoutineRecordSyncLocalDataSource: AccountRoutineRecordSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountRoutineRecordSyncTransaction: AccountRoutineRecordSyncTransaction,
    private val routineRecordRemoteDataSource: RoutineRecordRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountRoutineRecordSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> routineRecordRemoteDataSource.push(routineRecordList = chunk.map { record -> record.toRemote() }) },
            clearPending = { chunk -> accountRoutineRecordSyncTransaction.clearPending(accountId = accountId, recordList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.ROUTINE_RECORD,
            pull = { cursor -> routineRecordRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountRoutineRecordSyncTransaction.upsert(
                    accountId = accountId,
                    recordList = pullList.map { pull -> pull.routineRecord.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
