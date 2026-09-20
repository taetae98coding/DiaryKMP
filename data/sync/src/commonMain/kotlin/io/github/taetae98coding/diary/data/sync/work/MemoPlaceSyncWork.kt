package io.github.taetae98coding.diary.data.sync.work

import io.github.taetae98coding.diary.core.database.api.memoplace.datasource.AccountMemoPlaceSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memoplace.transaction.AccountMemoPlaceSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.memoplace.datasource.MemoPlaceRemoteDataSource
import io.github.taetae98coding.diary.data.sync.mapper.toLocal
import io.github.taetae98coding.diary.data.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class MemoPlaceSyncWork(
    private val accountMemoPlaceSyncLocalDataSource: AccountMemoPlaceSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountMemoPlaceSyncTransaction: AccountMemoPlaceSyncTransaction,
    private val memoPlaceRemoteDataSource: MemoPlaceRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        val memoPlaceList = accountMemoPlaceSyncLocalDataSource.findPending(accountId = accountId)

        memoPlaceList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            memoPlaceRemoteDataSource.push(memoPlaceList = chunk.map { memoPlace -> memoPlace.toRemote() })
            accountMemoPlaceSyncTransaction.clearPending(accountId = accountId, memoPlaceList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.MEMO_PLACE,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = memoPlaceRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountMemoPlaceSyncTransaction.save(
                    accountId = accountId,
                    memoPlaceList = pullList.map { pull -> pull.memoPlace.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
