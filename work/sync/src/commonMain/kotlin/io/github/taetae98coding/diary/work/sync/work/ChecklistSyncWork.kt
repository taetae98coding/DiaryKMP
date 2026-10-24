package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.checklist.datasource.AccountChecklistSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklist.transaction.AccountChecklistSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.checklist.datasource.ChecklistRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class ChecklistSyncWork(
    private val accountChecklistSyncLocalDataSource: AccountChecklistSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountChecklistSyncTransaction: AccountChecklistSyncTransaction,
    private val checklistRemoteDataSource: ChecklistRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountChecklistSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> checklistRemoteDataSource.push(checklistList = chunk.map { checklist -> checklist.toRemote() }) },
            clearPending = { chunk -> accountChecklistSyncTransaction.clearPending(accountId = accountId, checklistList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.CHECKLIST,
            pull = { cursor -> checklistRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountChecklistSyncTransaction.upsert(
                    accountId = accountId,
                    checklistList = pullList.map { pull -> pull.checklist.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
