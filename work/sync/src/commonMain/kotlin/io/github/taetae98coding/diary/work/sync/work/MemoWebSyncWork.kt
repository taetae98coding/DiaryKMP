package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.memoweb.datasource.AccountMemoWebSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memoweb.transaction.AccountMemoWebSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.memoweb.datasource.MemoWebRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class MemoWebSyncWork(
    private val accountMemoWebSyncLocalDataSource: AccountMemoWebSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountMemoWebSyncTransaction: AccountMemoWebSyncTransaction,
    private val memoWebRemoteDataSource: MemoWebRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountMemoWebSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> memoWebRemoteDataSource.push(memoWebList = chunk.map { memoWeb -> memoWeb.toRemote() }) },
            clearPending = { chunk -> accountMemoWebSyncTransaction.clearPending(accountId = accountId, memoWebList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.MEMO_WEB,
            pull = { cursor -> memoWebRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountMemoWebSyncTransaction.upsert(
                    accountId = accountId,
                    memoWebList = pullList.map { pull -> pull.memoWeb.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
