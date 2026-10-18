package io.github.taetae98coding.diary.core.database.api.qr.transaction

import io.github.taetae98coding.diary.core.database.api.qr.entity.QrDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountQrTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        qrList: List<QrLocalEntity>,
    )

    public suspend fun updateDetail(
        accountId: Uuid,
        qrId: Uuid,
        detail: QrDetailLocalEntity,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        accountId: Uuid,
        qrId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
