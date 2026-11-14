package io.github.taetae98coding.diary.core.database.impl.checklisttag.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.checklisttag.entity.AccountChecklistTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagDao
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountChecklistTagDao : AccountEntityTagDao<AccountChecklistTagLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        INNER JOIN checklist_tag
            ON checklist_tag.tag_id = tag.id
                AND checklist_tag.checklist_id = :entityId
                AND checklist_tag.is_deleted = 0
        INNER JOIN account_checklist_tag
            ON account_checklist_tag.checklist_id = checklist_tag.checklist_id
                AND account_checklist_tag.tag_id = checklist_tag.tag_id
                AND account_checklist_tag.account_id = :accountId
        INNER JOIN checklist
            ON checklist.id = checklist_tag.checklist_id
        INNER JOIN account_checklist
            ON account_checklist.checklist_id = checklist.id AND account_checklist.account_id = :accountId
        WHERE tag.is_deleted = 0
        ORDER BY tag.title ASC
        """,
    )
    override fun getTagList(
        accountId: Uuid,
        entityId: Uuid,
    ): Flow<List<TagLocalEntity>>

    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE tag.is_deleted = 0
            AND (
                tag.is_finished = 0
                OR EXISTS(
                    SELECT 1
                    FROM checklist_tag
                    INNER JOIN account_checklist_tag
                        ON account_checklist_tag.checklist_id = checklist_tag.checklist_id
                            AND account_checklist_tag.tag_id = checklist_tag.tag_id
                            AND account_checklist_tag.account_id = :accountId
                    INNER JOIN account_checklist
                        ON account_checklist.checklist_id = checklist_tag.checklist_id AND account_checklist.account_id = :accountId
                    WHERE checklist_tag.tag_id = tag.id
                        AND checklist_tag.checklist_id = :entityId
                        AND checklist_tag.is_deleted = 0
                )
            )
            AND (
                :query = ''
                OR INSTR(LOWER(tag.emoji), LOWER(:query)) > 0
                OR INSTR(LOWER(tag.title), LOWER(:query)) > 0
                OR INSTR(LOWER(tag.description), LOWER(:query)) > 0
            )
        ORDER BY tag.title ASC
        """,
    )
    override fun pageSelectableTag(
        accountId: Uuid,
        entityId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity>

    @Query(
        """
        UPDATE account_checklist_tag
        SET is_dirty = 1
        WHERE account_id = :accountId AND checklist_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun markPending(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
    ): Int
}
