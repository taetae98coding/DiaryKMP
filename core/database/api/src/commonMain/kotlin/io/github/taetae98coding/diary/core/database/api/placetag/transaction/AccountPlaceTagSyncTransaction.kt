package io.github.taetae98coding.diary.core.database.api.placetag.transaction

import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import kotlin.uuid.Uuid

public interface AccountPlaceTagSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        placeTagList: List<PlaceTagLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        placeTagList: List<PlaceTagLocalEntity>,
        cursor: Long,
    )
}
