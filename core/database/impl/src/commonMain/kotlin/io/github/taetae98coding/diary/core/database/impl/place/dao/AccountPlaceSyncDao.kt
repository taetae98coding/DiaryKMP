package io.github.taetae98coding.diary.core.database.impl.place.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountPlaceSyncDao : RoomDao<AccountPlaceLocalEntity> {
    @Query(
        """
        SELECT place.*
        FROM place
        INNER JOIN account_place
            ON account_place.place_id = place.id AND account_place.account_id = :accountId
        WHERE account_place.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<PlaceLocalEntity>

    @Query(
        """
        UPDATE account_place
        SET is_dirty = 0
        WHERE account_id = :accountId AND place_id = :placeId
            AND EXISTS(
                SELECT 1
                FROM place
                WHERE place.id = :placeId AND place.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        placeId: Uuid,
        updatedAt: Instant,
    ): Int
}
