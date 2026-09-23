package io.github.taetae98coding.diary.core.database.impl.webtag.datasource

import io.github.taetae98coding.diary.core.database.api.webtag.datasource.AccountWebTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountWebTagSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountWebTagSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<WebTagLocalEntity> = database.accountWebTagSyncDao().findPending(accountId = accountId)
}
