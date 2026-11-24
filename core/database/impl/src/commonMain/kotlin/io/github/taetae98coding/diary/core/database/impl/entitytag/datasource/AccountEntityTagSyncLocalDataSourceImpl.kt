package io.github.taetae98coding.diary.core.database.impl.entitytag.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.entitytag.datasource.AccountEntityTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import io.github.taetae98coding.diary.core.database.impl.entitytag.entityTagTable
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountEntityTagSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountEntityTagSyncLocalDataSource {
    override suspend fun readPendingList(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
    ): List<EntityTagLocalEntity> = database.entityTagTable(kind = kind).findPending(accountId = accountId)
}
