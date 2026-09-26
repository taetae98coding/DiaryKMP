package io.github.taetae98coding.diary.core.database.impl.qr.datasource

import io.github.taetae98coding.diary.core.database.api.qr.datasource.AccountQrSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountQrSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountQrSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<QrLocalEntity> = database.accountQrSyncDao().findPending(accountId = accountId)
}
