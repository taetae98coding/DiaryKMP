package io.github.taetae98coding.diary.core.database.impl.routinetag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagSyncDao
import io.github.taetae98coding.diary.core.database.impl.routinetag.entity.AccountRoutineTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.routinetag.entity.RoutineTagLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountRoutineTagSyncDao : AccountEntityTagSyncDao<RoutineTagLocalEntity, AccountRoutineTagLocalEntity> {
    @Query(
        """
        SELECT routine_tag.*
        FROM routine_tag
        INNER JOIN account_routine_tag
            ON account_routine_tag.routine_id = routine_tag.routine_id
                AND account_routine_tag.tag_id = routine_tag.tag_id
                AND account_routine_tag.account_id = :accountId
        WHERE account_routine_tag.is_dirty = 1
        """,
    )
    override suspend fun findPending(accountId: Uuid): List<RoutineTagLocalEntity>

    @Query(
        """
        UPDATE account_routine_tag
        SET is_dirty = 0
        WHERE account_id = :accountId AND routine_id = :entityId AND tag_id = :tagId
            AND EXISTS(
                SELECT 1
                FROM routine_tag
                WHERE routine_tag.routine_id = :entityId AND routine_tag.tag_id = :tagId AND routine_tag.updated_at = :updatedAt
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
