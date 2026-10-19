package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.checklistitem.datasource.AccountChecklistItemSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklistitem.transaction.AccountChecklistItemSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.checklistitem.datasource.ChecklistItemRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class ChecklistItemSyncWork(
    private val accountChecklistItemSyncLocalDataSource: AccountChecklistItemSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountChecklistItemSyncTransaction: AccountChecklistItemSyncTransaction,
    private val checklistItemRemoteDataSource: ChecklistItemRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountChecklistItemSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> checklistItemRemoteDataSource.push(checklistItemList = chunk.map { item -> item.toRemote() }) },
            clearPending = { chunk -> accountChecklistItemSyncTransaction.clearPending(accountId = accountId, itemList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.CHECKLIST_ITEM,
            pull = { cursor -> checklistItemRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountChecklistItemSyncTransaction.upsert(
                    accountId = accountId,
                    itemList = pullList.map { pull -> pull.checklistItem.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
