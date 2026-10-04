package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.memocontact.datasource.AccountMemoContactSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memocontact.transaction.AccountMemoContactSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
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
        pushPending(
            pendingList = accountMemoContactSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> memoContactRemoteDataSource.push(memoContactList = chunk.map { memoContact -> memoContact.toRemote() }) },
            clearPending = { chunk -> accountMemoContactSyncTransaction.clearPending(accountId = accountId, memoContactList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.MEMO_CONTACT,
            pull = { cursor -> memoContactRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountMemoContactSyncTransaction.upsert(
                    accountId = accountId,
                    memoContactList = pullList.map { pull -> pull.memoContact.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
