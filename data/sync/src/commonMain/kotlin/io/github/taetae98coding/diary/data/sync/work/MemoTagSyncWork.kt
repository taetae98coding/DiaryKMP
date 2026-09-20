package io.github.taetae98coding.diary.data.sync.work

import io.github.taetae98coding.diary.core.database.api.memotag.datasource.AccountMemoTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memotag.transaction.AccountMemoTagSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.memotag.datasource.MemoTagRemoteDataSource
import io.github.taetae98coding.diary.data.sync.mapper.toLocal
import io.github.taetae98coding.diary.data.sync.mapper.toRemote
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
        val memoTagList = accountMemoTagSyncLocalDataSource.findPending(accountId = accountId)

        memoTagList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            memoTagRemoteDataSource.push(memoTagList = chunk.map { memoTag -> memoTag.toRemote() })
            accountMemoTagSyncTransaction.clearPending(accountId = accountId, memoTagList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.MEMO_TAG,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = memoTagRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountMemoTagSyncTransaction.save(
                    accountId = accountId,
                    memoTagList = pullList.map { pull -> pull.memoTag.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
