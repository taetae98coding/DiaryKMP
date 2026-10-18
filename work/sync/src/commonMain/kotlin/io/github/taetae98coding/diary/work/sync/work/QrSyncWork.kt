package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.qr.datasource.AccountQrSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.qr.transaction.AccountQrSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.network.api.qr.datasource.QrRemoteDataSource
import io.github.taetae98coding.diary.work.sync.mapper.toLocal
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class QrSyncWork(
    private val accountQrSyncLocalDataSource: AccountQrSyncLocalDataSource,
    private val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    private val accountQrSyncTransaction: AccountQrSyncTransaction,
    private val qrRemoteDataSource: QrRemoteDataSource,
) {
    suspend fun push(accountId: Uuid) {
        pushPending(
            pendingList = accountQrSyncLocalDataSource.readPendingList(accountId = accountId),
            push = { chunk -> qrRemoteDataSource.push(qrList = chunk.map { qr -> qr.toRemote() }) },
            clearPending = { chunk -> accountQrSyncTransaction.clearPending(accountId = accountId, qrList = chunk) },
        )
    }

    suspend fun pull(accountId: Uuid) {
        syncCursorLocalDataSource.pullUntilExhausted(
            accountId = accountId,
            kind = SyncKindLocalEntity.QR,
            pull = { cursor -> qrRemoteDataSource.pull(usn = cursor) },
            usn = { pull -> pull.usn },
            upsert = { pullList, cursor ->
                accountQrSyncTransaction.upsert(
                    accountId = accountId,
                    qrList = pullList.map { pull -> pull.qr.toLocal() },
                    cursor = cursor,
                )
            },
        )
    }
}
