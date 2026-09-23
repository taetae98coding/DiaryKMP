package io.github.taetae98coding.diary.core.database.impl.place.datasource

import io.github.taetae98coding.diary.core.database.api.place.datasource.AccountPlaceSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountPlaceSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountPlaceSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<PlaceLocalEntity> = database.accountPlaceSyncDao().findPending(accountId = accountId)
}
