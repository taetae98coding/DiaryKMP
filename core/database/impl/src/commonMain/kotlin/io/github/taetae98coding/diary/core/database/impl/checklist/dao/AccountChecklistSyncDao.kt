package io.github.taetae98coding.diary.core.database.impl.checklist.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklist.entity.AccountChecklistLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountChecklistSyncDao : RoomDao<AccountChecklistLocalEntity> {
    @Query(
        """
        SELECT checklist.*
        FROM checklist
        INNER JOIN account_checklist
            ON account_checklist.checklist_id = checklist.id AND account_checklist.account_id = :accountId
        WHERE account_checklist.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<ChecklistLocalEntity>

    @Query(
        """
        UPDATE account_checklist
        SET is_dirty = 0
        WHERE account_id = :accountId AND checklist_id = :checklistId
            AND EXISTS(
                SELECT 1
                FROM checklist
                WHERE checklist.id = :checklistId AND checklist.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        checklistId: Uuid,
        updatedAt: Instant,
    ): Int
}
