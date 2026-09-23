package io.github.taetae98coding.diary.core.database.impl.tag.datasource

import io.github.taetae98coding.diary.core.database.api.tag.datasource.AccountTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<TagLocalEntity> = database.accountTagSyncDao().findPending(accountId = accountId)
}
