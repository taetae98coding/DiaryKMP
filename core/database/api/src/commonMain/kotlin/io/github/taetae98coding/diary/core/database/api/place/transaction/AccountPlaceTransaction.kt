package io.github.taetae98coding.diary.core.database.api.place.transaction

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountPlaceTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        placeList: List<PlaceLocalEntity>,
        placeTagList: List<PlaceTagLocalEntity>,
    )

    public suspend fun updateDetail(
        accountId: Uuid,
        placeId: Uuid,
        detail: PlaceDetailLocalEntity,
        updatedAt: Instant,
    ): Int

    public suspend fun updateDeleted(
        accountId: Uuid,
        placeId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
