package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.memoplace.datasource.AccountMemoPlaceSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memoplace.transaction.AccountMemoPlaceSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.memoplace.datasource.MemoPlaceRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
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
        pushPending(
            pendingList = accountMemoPlaceSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> memoPlaceRemoteDataSource.push(memoPlaceList = chunk.map { memoPlace -> memoPlace.toRemote() }) },
            clearPending = { chunk -> accountMemoPlaceSyncTransaction.clearPending(accountId = accountId, memoPlaceList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.MEMO_PLACE,
            pull = { cursor -> memoPlaceRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountMemoPlaceSyncTransaction.upsert(
                    accountId = accountId,
                    memoPlaceList = pullList.map { pull -> pull.memoPlace.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
