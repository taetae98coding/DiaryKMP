package io.github.taetae98coding.diary.core.database.impl.checklist.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistSummaryLocalEntity
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountTagChecklistDao {
    @Query(
        """
        WITH RECURSIVE scoped_tag(tag_id) AS (
            SELECT :tagId
            UNION
            SELECT tag_link.to_tag_id
            FROM scoped_tag
            INNER JOIN tag_link
                ON tag_link.from_tag_id = scoped_tag.tag_id
                    AND tag_link.is_deleted = 0
            INNER JOIN account_tag_link
                ON account_tag_link.from_tag_id = tag_link.from_tag_id
                    AND account_tag_link.to_tag_id = tag_link.to_tag_id
                    AND account_tag_link.account_id = :accountId
            INNER JOIN account_tag AS from_account_tag
                ON from_account_tag.tag_id = tag_link.from_tag_id
                    AND from_account_tag.account_id = :accountId
            INNER JOIN tag AS to_tag
                ON to_tag.id = tag_link.to_tag_id
                    AND to_tag.is_deleted = 0
            INNER JOIN account_tag AS to_account_tag
                ON to_account_tag.tag_id = tag_link.to_tag_id
                    AND to_account_tag.account_id = :accountId
            WHERE :scope = 'descendant'
                OR (:scope = 'child' AND scoped_tag.tag_id = :tagId)
        )
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
            AND EXISTS(
                SELECT 1
                FROM checklist_tag
                INNER JOIN scoped_tag
                    ON scoped_tag.tag_id = checklist_tag.tag_id
                INNER JOIN account_checklist_tag
                    ON account_checklist_tag.checklist_id = checklist_tag.checklist_id
                        AND account_checklist_tag.tag_id = checklist_tag.tag_id
                        AND account_checklist_tag.account_id = :accountId
                INNER JOIN tag
                    ON tag.id = checklist_tag.tag_id
                INNER JOIN account_tag
                    ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
                WHERE checklist_tag.checklist_id = checklist.id
                    AND checklist_tag.is_deleted = 0
            )
        ORDER BY checklist.title ASC
        """,
    )
    fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: String,
    ): PagingSource<Int, ChecklistSummaryLocalEntity>
}
