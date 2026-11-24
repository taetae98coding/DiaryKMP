package io.github.taetae98coding.diary.core.database.impl.taglink.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.taglink.datasource.AccountTagLinkSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountTagLinkSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagLinkSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<TagLinkLocalEntity> = database.accountTagLinkSyncDao().findPending(accountId = accountId)
}
