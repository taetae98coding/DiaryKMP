package io.github.taetae98coding.diary.core.database.api.placetag.datasource

import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import kotlin.uuid.Uuid

public interface AccountPlaceTagSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<PlaceTagLocalEntity>
}
