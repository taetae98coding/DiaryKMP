package io.github.taetae98coding.diary.core.database.impl.routinetag.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagDao
import io.github.taetae98coding.diary.core.database.impl.routinetag.entity.AccountRoutineTagLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountRoutineTagDao : AccountEntityTagDao<AccountRoutineTagLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        INNER JOIN routine_tag
            ON routine_tag.tag_id = tag.id
                AND routine_tag.routine_id = :entityId
                AND routine_tag.is_deleted = 0
        INNER JOIN account_routine_tag
            ON account_routine_tag.routine_id = routine_tag.routine_id
                AND account_routine_tag.tag_id = routine_tag.tag_id
                AND account_routine_tag.account_id = :accountId
        INNER JOIN routine
            ON routine.id = routine_tag.routine_id
        INNER JOIN account_routine
            ON account_routine.routine_id = routine.id AND account_routine.account_id = :accountId
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
                    FROM routine_tag
                    INNER JOIN account_routine_tag
                        ON account_routine_tag.routine_id = routine_tag.routine_id
                            AND account_routine_tag.tag_id = routine_tag.tag_id
                            AND account_routine_tag.account_id = :accountId
                    INNER JOIN account_routine
                        ON account_routine.routine_id = routine_tag.routine_id AND account_routine.account_id = :accountId
                    WHERE routine_tag.tag_id = tag.id
                        AND routine_tag.routine_id = :entityId
                        AND routine_tag.is_deleted = 0
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
        UPDATE account_routine_tag
        SET is_dirty = 1
        WHERE account_id = :accountId AND routine_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun markPending(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
    ): Int
}
