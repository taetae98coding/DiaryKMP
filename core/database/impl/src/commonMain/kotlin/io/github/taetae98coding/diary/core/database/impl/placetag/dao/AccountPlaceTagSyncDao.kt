package io.github.taetae98coding.diary.core.database.impl.placetag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.AccountPlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.PlaceTagLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountPlaceTagSyncDao : AccountEntityTagSyncDao<PlaceTagLocalEntity, AccountPlaceTagLocalEntity> {
    @Query(
        """
        SELECT place_tag.*
        FROM place_tag
        INNER JOIN account_place_tag
            ON account_place_tag.place_id = place_tag.place_id
                AND account_place_tag.tag_id = place_tag.tag_id
                AND account_place_tag.account_id = :accountId
        WHERE account_place_tag.is_dirty = 1
        """,
    )
    override suspend fun findPending(accountId: Uuid): List<PlaceTagLocalEntity>

    @Query(
        """
        UPDATE account_place_tag
        SET is_dirty = 0
        WHERE account_id = :accountId AND place_id = :entityId AND tag_id = :tagId
            AND EXISTS(
                SELECT 1
                FROM place_tag
                WHERE place_tag.place_id = :entityId AND place_tag.tag_id = :tagId AND place_tag.updated_at = :updatedAt
            )
        """,
    )
    override suspend fun clearPending(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ): Int
}
