package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.memocontact.datasource.AccountMemoContactSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memocontact.transaction.AccountMemoContactSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.memocontact.datasource.MemoContactRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class MemoContactSyncWork(
    private val accountMemoContactSyncLocalDataSource: AccountMemoContactSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountMemoContactSyncTransaction: AccountMemoContactSyncTransaction,
    private val memoContactRemoteDataSource: MemoContactRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        val memoContactList = accountMemoContactSyncLocalDataSource.findPending(accountId = accountId)

        memoContactList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            memoContactRemoteDataSource.push(memoContactList = chunk.map { memoContact -> memoContact.toRemote() })
            accountMemoContactSyncTransaction.clearPending(accountId = accountId, memoContactList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.MEMO_CONTACT,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = memoContactRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountMemoContactSyncTransaction.save(
                    accountId = accountId,
                    memoContactList = pullList.map { pull -> pull.memoContact.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
