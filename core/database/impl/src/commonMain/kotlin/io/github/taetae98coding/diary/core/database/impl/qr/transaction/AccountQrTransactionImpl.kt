package io.github.taetae98coding.diary.core.database.impl.qr.transaction

import androidx.room3.withWriteTransaction
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.transaction.AccountQrTransaction
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.qr.entity.AccountQrLocalEntity
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountQrTransactionImpl(
    private val database: DiaryDatabase,
) : AccountQrTransaction {
    override suspend fun upsert(
        accountId: Uuid,
        qrList: List<QrLocalEntity>,
    ) {
        database.withWriteTransaction {
            database.qrDao().upsert(qrList)
            database.accountQrDao().upsert(
                qrList.map { qr ->
                    AccountQrLocalEntity(
                        accountId = accountId,
                        qrId = qr.id,
                        isDirty = true,
                    )
                },
            )
        }
    }

    override suspend fun updateDeleted(
        accountId: Uuid,
        qrId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int =
        database.withWriteTransaction {
            val updatedCount =
                database.accountQrDao().updateDeleted(
                    accountId = accountId,
                    qrId = qrId,
                    isDeleted = isDeleted,
                    updatedAt = updatedAt,
                )

            if (updatedCount > 0) {
                database.accountQrDao().markPending(accountId = accountId, qrId = qrId)
            }

            updatedCount
        }
}
