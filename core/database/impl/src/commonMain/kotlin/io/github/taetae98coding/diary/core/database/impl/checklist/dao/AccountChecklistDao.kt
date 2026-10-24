package io.github.taetae98coding.diary.core.database.impl.checklist.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklist.entity.AccountChecklistLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountChecklistDao : RoomDao<AccountChecklistLocalEntity> {
    @Query(
        """
        SELECT
            checklist.*,
            (
                SELECT COUNT(*)
                FROM checklist_item
                INNER JOIN account_checklist_item
                    ON account_checklist_item.checklist_item_id = checklist_item.id
                        AND account_checklist_item.account_id = :accountId
                WHERE checklist_item.checklist_id = checklist.id
                    AND checklist_item.is_deleted = 0
                    AND checklist_item.is_checked = 1
            ) AS checked_count,
            (
                SELECT COUNT(*)
                FROM checklist_item
                INNER JOIN account_checklist_item
                    ON account_checklist_item.checklist_item_id = checklist_item.id
                        AND account_checklist_item.account_id = :accountId
                WHERE checklist_item.checklist_id = checklist.id
                    AND checklist_item.is_deleted = 0
            ) AS total_count
        FROM checklist
        INNER JOIN account_checklist
            ON account_checklist.checklist_id = checklist.id AND account_checklist.account_id = :accountId
        WHERE checklist.is_deleted = 0
        ORDER BY checklist.title ASC
        """,
    )
    fun page(accountId: Uuid): PagingSource<Int, ChecklistSummaryLocalEntity>

    @Query(
        """
        SELECT checklist.*
        FROM checklist
        INNER JOIN account_checklist
            ON account_checklist.checklist_id = checklist.id AND account_checklist.account_id = :accountId
        WHERE checklist.id = :checklistId
        """,
    )
    fun find(
        accountId: Uuid,
        checklistId: Uuid,
    ): Flow<ChecklistLocalEntity?>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM account_checklist
            WHERE account_id = :accountId AND checklist_id = :checklistId
        )
        """,
    )
    suspend fun exists(
        accountId: Uuid,
        checklistId: Uuid,
    ): Boolean

    @Query(
        """
        UPDATE checklist
        SET title = :title, description = :description, updated_at = :updatedAt
        WHERE id = :checklistId
            AND EXISTS(
                SELECT 1
                FROM account_checklist
                WHERE account_checklist.checklist_id = checklist.id AND account_checklist.account_id = :accountId
            )
        """,
    )
    suspend fun updateDetail(
        accountId: Uuid,
        checklistId: Uuid,
        title: String,
        description: String,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE checklist
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE id = :checklistId
            AND EXISTS(
                SELECT 1
                FROM account_checklist
                WHERE account_checklist.checklist_id = checklist.id AND account_checklist.account_id = :accountId
            )
        """,
    )
    suspend fun updateDeleted(
        accountId: Uuid,
        checklistId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE account_checklist
        SET is_dirty = 1
        WHERE account_id = :accountId AND checklist_id = :checklistId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        checklistId: Uuid,
    ): Int
}
