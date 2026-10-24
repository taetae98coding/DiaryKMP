package io.github.taetae98coding.diary.core.database.impl.checklistitem.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklistitem.entity.AccountChecklistItemLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountChecklistItemDao : RoomDao<AccountChecklistItemLocalEntity> {
    @Query(
        """
        SELECT checklist_item.*
        FROM checklist_item
        INNER JOIN account_checklist_item
            ON account_checklist_item.checklist_item_id = checklist_item.id AND account_checklist_item.account_id = :accountId
        WHERE checklist_item.checklist_id = :checklistId
            AND checklist_item.is_deleted = 0
        ORDER BY
            CASE WHEN :sort = 'unchecked_first' THEN checklist_item.is_checked END ASC,
            checklist_item.created_at ASC
        """,
    )
    fun get(
        accountId: Uuid,
        checklistId: Uuid,
        sort: String,
    ): Flow<List<ChecklistItemLocalEntity>>

    @Query(
        """
        SELECT checklist_item.id
        FROM checklist_item
        INNER JOIN account_checklist_item
            ON account_checklist_item.checklist_item_id = checklist_item.id AND account_checklist_item.account_id = :accountId
        WHERE checklist_item.checklist_id = :checklistId
            AND checklist_item.is_deleted = 0
            AND checklist_item.is_checked != :isChecked
        """,
    )
    suspend fun findIdListToChange(
        accountId: Uuid,
        checklistId: Uuid,
        isChecked: Boolean,
    ): List<Uuid>

    @Query(
        """
        UPDATE checklist_item
        SET is_checked = :isChecked, updated_at = :updatedAt
        WHERE id = :itemId
            AND EXISTS(
                SELECT 1
                FROM account_checklist_item
                WHERE account_checklist_item.checklist_item_id = checklist_item.id
                    AND account_checklist_item.account_id = :accountId
            )
        """,
    )
    suspend fun updateChecked(
        accountId: Uuid,
        itemId: Uuid,
        isChecked: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE checklist_item
        SET is_checked = :isChecked, updated_at = :updatedAt
        WHERE id IN (:itemIdList)
        """,
    )
    suspend fun updateChecked(
        itemIdList: List<Uuid>,
        isChecked: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE checklist_item
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE id = :itemId
            AND EXISTS(
                SELECT 1
                FROM account_checklist_item
                WHERE account_checklist_item.checklist_item_id = checklist_item.id
                    AND account_checklist_item.account_id = :accountId
            )
        """,
    )
    suspend fun updateDeleted(
        accountId: Uuid,
        itemId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE account_checklist_item
        SET is_dirty = 1
        WHERE account_id = :accountId AND checklist_item_id IN (:itemIdList)
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        itemIdList: List<Uuid>,
    ): Int
}
