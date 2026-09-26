package io.github.taetae98coding.diary.core.database.impl.qr.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.transaction.AccountQrSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.qr.entity.AccountQrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountQrSyncTransactionImpl(
    private val database: DiaryDatabase,
) : AccountQrSyncTransaction {
    override suspend fun clearPending(
        accountId: Uuid,
        qrList: List<QrLocalEntity>,
    ) {
        database.withWriteTransaction {
            qrList.forEach { qr ->
                database.accountQrSyncDao().clearPending(
                    accountId = accountId,
                    qrId = qr.id,
                    updatedAt = qr.updatedAt,
                )
            }
        }
    }

    override suspend fun save(
        accountId: Uuid,
        qrList: List<QrLocalEntity>,
        cursor: Long,
    ) {
        database.withWriteTransaction {
            val localUpdatedAtMap = database.qrDao().findUpdatedAt(qrList.map { qr -> qr.id })
            database.qrDao().upsert(
                qrList.filter { qr ->
                    val localUpdatedAt = localUpdatedAtMap[qr.id]
                    localUpdatedAt == null || qr.updatedAt >= localUpdatedAt
                },
            )
            database.accountQrSyncDao().insertIgnore(
                qrList.map { qr ->
                    AccountQrLocalEntity(
                        accountId = accountId,
                        qrId = qr.id,
                        isDirty = false,
                    )
                },
            )
            database.syncCursorDao().upsert(
                SyncCursorLocalEntity(
                    accountId = accountId,
                    kind = SyncCursorLocalEntity.column(kind = SyncKind.QR),
                    usn = cursor,
                ),
            )
        }
    }
}
