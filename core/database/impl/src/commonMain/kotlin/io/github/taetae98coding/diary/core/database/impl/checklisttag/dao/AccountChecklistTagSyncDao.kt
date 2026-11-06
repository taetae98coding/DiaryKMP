package io.github.taetae98coding.diary.core.database.impl.checklisttag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.checklisttag.entity.AccountChecklistTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklisttag.entity.ChecklistTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagSyncDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountChecklistTagSyncDao : AccountEntityTagSyncDao<ChecklistTagLocalEntity, AccountChecklistTagLocalEntity> {
    @Query(
        """
        SELECT checklist_tag.*
        FROM checklist_tag
        INNER JOIN account_checklist_tag
            ON account_checklist_tag.checklist_id = checklist_tag.checklist_id
                AND account_checklist_tag.tag_id = checklist_tag.tag_id
                AND account_checklist_tag.account_id = :accountId
        WHERE account_checklist_tag.is_dirty = 1
        """,
    )
    override suspend fun findPending(accountId: Uuid): List<ChecklistTagLocalEntity>

    @Query(
        """
        UPDATE account_checklist_tag
        SET is_dirty = 0
        WHERE account_id = :accountId AND checklist_id = :entityId AND tag_id = :tagId
            AND EXISTS(
                SELECT 1
                FROM checklist_tag
                WHERE checklist_tag.checklist_id = :entityId AND checklist_tag.tag_id = :tagId AND checklist_tag.updated_at = :updatedAt
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
