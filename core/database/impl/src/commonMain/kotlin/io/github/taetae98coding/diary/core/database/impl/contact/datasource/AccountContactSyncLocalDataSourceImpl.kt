package io.github.taetae98coding.diary.core.database.impl.contact.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountContactSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountContactSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountContactSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<ContactLocalEntity> = database.accountContactSyncDao().findPending(accountId = accountId)
}
