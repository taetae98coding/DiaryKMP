package io.github.taetae98coding.diary.core.database.impl.qr.transaction

import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.transaction.AccountQrSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.qr.entity.AccountQrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.clearPendingEach
import io.github.taetae98coding.diary.core.database.impl.sync.transaction.upsertPulled
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
        database.clearPendingEach(qrList) { qr ->
            database.accountQrSyncDao().clearPending(
                accountId = accountId,
                qrId = qr.id,
                updatedAt = qr.updatedAt,
            )
        }
    }

    override suspend fun upsert(
        accountId: Uuid,
        qrList: List<QrLocalEntity>,
        cursor: Long,
    ) {
        database.upsertPulled(
            accountId = accountId,
            kind = SyncKindLocalEntity.QR,
            cursor = cursor,
            pulledList = qrList,
            keyOf = { qr -> qr.id },
            updatedAtOf = { qr -> qr.updatedAt },
            readLocalUpdatedAtMap = { pulledList -> database.qrDao().findUpdatedAt(pulledList.map { qr -> qr.id }) },
            upsert = { upsertList -> database.qrDao().upsert(upsertList) },
            insertIgnoreAccount = { pulledList ->
                database.accountQrSyncDao().insertIgnore(
                    pulledList.map { qr ->
                        AccountQrLocalEntity(
                            accountId = accountId,
                            qrId = qr.id,
                            isDirty = false,
                        )
                    },
                )
            },
        )
    }
}
