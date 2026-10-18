package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.memotag.datasource.AccountMemoTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memotag.transaction.AccountMemoTagSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.memotag.datasource.MemoTagRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class MemoTagSyncWork(
    private val accountMemoTagSyncLocalDataSource: AccountMemoTagSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountMemoTagSyncTransaction: AccountMemoTagSyncTransaction,
    private val memoTagRemoteDataSource: MemoTagRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountMemoTagSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> memoTagRemoteDataSource.push(memoTagList = chunk.map { memoTag -> memoTag.toRemote() }) },
            clearPending = { chunk -> accountMemoTagSyncTransaction.clearPending(accountId = accountId, memoTagList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.MEMO_TAG,
            pull = { cursor -> memoTagRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountMemoTagSyncTransaction.upsert(
                    accountId = accountId,
                    memoTagList = pullList.map { pull -> pull.memoTag.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
