package io.github.taetae98coding.diary.core.database.api.place.transaction

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import kotlin.uuid.Uuid

public interface AccountPlaceSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        placeList: List<PlaceLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        placeList: List<PlaceLocalEntity>,
        cursor: Long,
    )
}
