package io.github.taetae98coding.diary.data.sync.work

import io.github.taetae98coding.diary.core.database.api.memoweb.datasource.AccountMemoWebSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memoweb.transaction.AccountMemoWebSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.memoweb.datasource.MemoWebRemoteDataSource
import io.github.taetae98coding.diary.data.sync.mapper.toLocal
import io.github.taetae98coding.diary.data.sync.mapper.toRemote
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
        val memoWebList = accountMemoWebSyncLocalDataSource.findPending(accountId = accountId)

        memoWebList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            memoWebRemoteDataSource.push(memoWebList = chunk.map { memoWeb -> memoWeb.toRemote() })
            accountMemoWebSyncTransaction.clearPending(accountId = accountId, memoWebList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.MEMO_WEB,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = memoWebRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountMemoWebSyncTransaction.save(
                    accountId = accountId,
                    memoWebList = pullList.map { pull -> pull.memoWeb.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
