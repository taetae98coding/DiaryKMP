package io.github.taetae98coding.diary.core.database.impl.web.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.web.datasource.AccountWebSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountWebSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountWebSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<WebLocalEntity> = database.accountWebSyncDao().findPending(accountId = accountId)
}
