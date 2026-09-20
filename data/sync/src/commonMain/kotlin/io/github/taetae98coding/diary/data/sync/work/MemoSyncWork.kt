package io.github.taetae98coding.diary.data.sync.work

import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountMemoSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.transaction.AccountMemoSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.memo.datasource.MemoRemoteDataSource
import io.github.taetae98coding.diary.data.sync.mapper.toLocal
import io.github.taetae98coding.diary.data.sync.mapper.toRemote
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
        val memoList = accountMemoSyncLocalDataSource.findPending(accountId = accountId)

        memoList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            memoRemoteDataSource.push(memoList = chunk.map { memo -> memo.toRemote() })
            accountMemoSyncTransaction.clearPending(accountId = accountId, memoList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.MEMO,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = memoRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountMemoSyncTransaction.save(
                    accountId = accountId,
                    memoList = pullList.map { pull -> pull.memo.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
