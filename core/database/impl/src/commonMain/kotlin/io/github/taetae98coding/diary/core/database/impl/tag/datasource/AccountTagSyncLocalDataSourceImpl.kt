package io.github.taetae98coding.diary.core.database.impl.tag.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.tag.datasource.AccountTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountTagSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<TagLocalEntity> = database.accountTagSyncDao().findPending(accountId = accountId)
}
