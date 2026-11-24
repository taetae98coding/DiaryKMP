package io.github.taetae98coding.diary.core.database.impl.qr.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.qr.datasource.AccountQrSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.qr.entity.QrLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountQrSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountQrSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<QrLocalEntity> = database.accountQrSyncDao().findPending(accountId = accountId)
}
