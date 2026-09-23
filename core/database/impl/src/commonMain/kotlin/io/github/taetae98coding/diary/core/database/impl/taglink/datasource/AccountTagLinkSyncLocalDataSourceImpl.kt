package io.github.taetae98coding.diary.core.database.impl.taglink.datasource

import io.github.taetae98coding.diary.core.database.api.taglink.datasource.AccountTagLinkSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagLinkSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagLinkSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<TagLinkLocalEntity> = database.accountTagLinkSyncDao().findPending(accountId = accountId)
}
