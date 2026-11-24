package io.github.taetae98coding.diary.core.database.impl.place.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.place.datasource.AccountPlaceSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountPlaceSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountPlaceSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<PlaceLocalEntity> = database.accountPlaceSyncDao().findPending(accountId = accountId)
}
