package io.github.taetae98coding.diary.core.database.api.qr.transaction

import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import kotlin.uuid.Uuid

public interface AccountQrSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        qrList: List<QrLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        qrList: List<QrLocalEntity>,
        cursor: Long,
    )
}
