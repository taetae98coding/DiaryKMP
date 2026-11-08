package io.github.taetae98coding.diary.core.database.impl.ddaytag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.ddaytag.entity.AccountDDayTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.ddaytag.entity.DDayTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagSyncDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountDDayTagSyncDao : AccountEntityTagSyncDao<DDayTagLocalEntity, AccountDDayTagLocalEntity> {
    @Query(
        """
        SELECT dday_tag.*
        FROM dday_tag
        INNER JOIN account_dday_tag
            ON account_dday_tag.dday_id = dday_tag.dday_id
                AND account_dday_tag.tag_id = dday_tag.tag_id
                AND account_dday_tag.account_id = :accountId
        WHERE account_dday_tag.is_dirty = 1
        """,
    )
    override suspend fun findPending(accountId: Uuid): List<DDayTagLocalEntity>

    @Query(
        """
        UPDATE account_dday_tag
        SET is_dirty = 0
        WHERE account_id = :accountId AND dday_id = :entityId AND tag_id = :tagId
            AND EXISTS(
                SELECT 1
                FROM dday_tag
                WHERE dday_tag.dday_id = :entityId AND dday_tag.tag_id = :tagId AND dday_tag.updated_at = :updatedAt
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
