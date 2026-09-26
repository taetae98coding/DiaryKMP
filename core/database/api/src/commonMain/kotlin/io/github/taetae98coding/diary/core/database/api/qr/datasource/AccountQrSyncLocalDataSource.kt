package io.github.taetae98coding.diary.core.database.api.qr.datasource

import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import kotlin.uuid.Uuid

public interface AccountQrSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<QrLocalEntity>
}
