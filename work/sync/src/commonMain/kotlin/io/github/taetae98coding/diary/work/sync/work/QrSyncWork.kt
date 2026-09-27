package io.github.taetae98coding.diary.work.sync.work

import io.github.taetae98coding.diary.core.database.api.qr.datasource.AccountQrSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.qr.transaction.AccountQrSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
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
        val qrList = accountQrSyncLocalDataSource.findPending(accountId = accountId)

        qrList.chunked(PUSH_CHUNK_SIZE).forEach { chunk ->
            qrRemoteDataSource.push(qrList = chunk.map { qr -> qr.toRemote() })
            accountQrSyncTransaction.clearPending(accountId = accountId, qrList = chunk)
        }
    }

    suspend fun pull(accountId: Uuid) {
        var cursor =
            syncCursorLocalDataSource.find(
                accountId = accountId,
                kind = SyncKind.QR,
            )
        var hasNext = true

        while (hasNext) {
            val pullList = qrRemoteDataSource.pull(usn = cursor)
            val nextCursor = pullList.maxOfOrNull { pull -> pull.usn }

            if (nextCursor == null || nextCursor <= cursor) {
                hasNext = false
            } else {
                accountQrSyncTransaction.save(
                    accountId = accountId,
                    qrList = pullList.map { pull -> pull.qr.toLocal() },
                    cursor = nextCursor,
                )
                cursor = nextCursor
            }
        }
    }
}
