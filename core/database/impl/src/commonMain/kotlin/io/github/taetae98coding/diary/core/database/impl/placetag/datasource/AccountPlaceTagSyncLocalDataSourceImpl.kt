package io.github.taetae98coding.diary.core.database.impl.placetag.datasource

import io.github.taetae98coding.diary.core.database.api.placetag.datasource.AccountPlaceTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountPlaceTagSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountPlaceTagSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<PlaceTagLocalEntity> = database.accountPlaceTagSyncDao().findPending(accountId = accountId)
}
