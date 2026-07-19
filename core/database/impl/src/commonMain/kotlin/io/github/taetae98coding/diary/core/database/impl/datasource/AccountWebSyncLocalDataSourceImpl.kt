package io.github.taetae98coding.diary.core.database.impl.datasource

import io.github.taetae98coding.diary.core.database.api.web.datasource.AccountWebSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountWebSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountWebSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<WebLocalEntity> = database.accountWebSyncDao().findPending(accountId = accountId)
}
