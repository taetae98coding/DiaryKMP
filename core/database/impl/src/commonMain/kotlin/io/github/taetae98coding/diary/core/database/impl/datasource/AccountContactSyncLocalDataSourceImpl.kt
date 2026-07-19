package io.github.taetae98coding.diary.core.database.impl.datasource

import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountContactSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountContactSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountContactSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<ContactLocalEntity> = database.accountContactSyncDao().findPending(accountId = accountId)
}
