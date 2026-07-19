package io.github.taetae98coding.diary.core.database.api.place.datasource

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import kotlin.uuid.Uuid

public interface AccountPlaceSyncLocalDataSource {
    public suspend fun findPending(accountId: Uuid): List<PlaceLocalEntity>
}
