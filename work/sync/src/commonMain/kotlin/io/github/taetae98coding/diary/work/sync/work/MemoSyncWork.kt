package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountMemoSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.transaction.AccountMemoSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.memo.datasource.MemoRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class MemoSyncWork(
    private val accountMemoSyncLocalDataSource: AccountMemoSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountMemoSyncTransaction: AccountMemoSyncTransaction,
    private val memoRemoteDataSource: MemoRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountMemoSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> memoRemoteDataSource.push(memoList = chunk.map { memo -> memo.toRemote() }) },
            clearPending = { chunk -> accountMemoSyncTransaction.clearPending(accountId = accountId, memoList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.MEMO,
            pull = { cursor -> memoRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountMemoSyncTransaction.upsert(
                    accountId = accountId,
                    memoList = pullList.map { pull -> pull.memo.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
