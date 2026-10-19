package io.github.taetae98coding.diary.core.database.impl.checklistitem.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklistitem.entity.AccountChecklistItemLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountChecklistItemSyncDao : RoomDao<AccountChecklistItemLocalEntity> {
    @Query(
        """
        SELECT checklist_item.*
        FROM checklist_item
        INNER JOIN account_checklist_item
            ON account_checklist_item.checklist_item_id = checklist_item.id AND account_checklist_item.account_id = :accountId
        WHERE account_checklist_item.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<ChecklistItemLocalEntity>

    @Query(
        """
        UPDATE account_checklist_item
        SET is_dirty = 0
        WHERE account_id = :accountId AND checklist_item_id = :itemId
            AND EXISTS(
                SELECT 1
                FROM checklist_item
                WHERE checklist_item.id = :itemId AND checklist_item.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        itemId: Uuid,
        updatedAt: Instant,
    ): Int
}
