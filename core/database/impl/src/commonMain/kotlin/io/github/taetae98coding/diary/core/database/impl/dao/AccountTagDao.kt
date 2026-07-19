package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
// 검색어의 `%`, `_`가 와일드카드로 해석되지 않도록 LIKE 대신 INSTR로 부분 일치를 판정한다.
internal interface AccountTagDao : RoomDao<AccountTagLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE tag.is_finished = 0 AND tag.is_deleted = 0 AND tag.id IN (:tagIdSet)
        ORDER BY tag.title ASC
        """,
    )
    fun get(
        accountId: Uuid,
        tagIdSet: Set<Uuid>,
    ): Flow<List<TagLocalEntity>>

    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE tag.is_finished = 0 AND tag.is_deleted = 0
            AND (
                :query = ''
                OR INSTR(LOWER(tag.emoji), LOWER(:query)) > 0
                OR INSTR(LOWER(tag.title), LOWER(:query)) > 0
                OR INSTR(LOWER(tag.description), LOWER(:query)) > 0
            )
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN tag.updated_at END DESC,
            tag.title ASC
        """,
    )
    fun page(
        accountId: Uuid,
        query: String,
        sort: String,
    ): PagingSource<Int, TagLocalEntity>

    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE tag.is_finished = 0 AND tag.is_deleted = 0
            AND NOT EXISTS(
                SELECT 1
                FROM tag_link
                INNER JOIN account_tag_link
                    ON account_tag_link.from_tag_id = tag_link.from_tag_id
                        AND account_tag_link.to_tag_id = tag_link.to_tag_id
                        AND account_tag_link.account_id = :accountId
                INNER JOIN account_tag AS from_account_tag
                    ON from_account_tag.tag_id = tag_link.from_tag_id AND from_account_tag.account_id = :accountId
                INNER JOIN tag AS from_tag
                    ON from_tag.id = tag_link.from_tag_id
                WHERE tag_link.to_tag_id = tag.id
                    AND tag_link.is_deleted = 0
                    AND from_tag.is_deleted = 0
            )
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN tag.updated_at END DESC,
            tag.title ASC
        """,
    )
    fun pageTopLevel(
        accountId: Uuid,
        sort: String,
    ): PagingSource<Int, TagLocalEntity>

    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE tag.is_finished = 1 AND tag.is_deleted = 0
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN tag.updated_at END DESC,
            tag.title ASC
        """,
    )
    fun pageFinished(
        accountId: Uuid,
        sort: String,
    ): PagingSource<Int, TagLocalEntity>

    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE tag.id = :tagId
        """,
    )
    fun find(
        accountId: Uuid,
        tagId: Uuid,
    ): Flow<TagLocalEntity?>

    @Query(
        """
        UPDATE tag
        SET is_finished = :isFinished, updated_at = :updatedAt
        WHERE id = :tagId
            AND EXISTS(
                SELECT 1
                FROM account_tag
                WHERE account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
            )
        """,
    )
    suspend fun updateFinished(
        accountId: Uuid,
        tagId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE tag
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE id = :tagId
            AND EXISTS(
                SELECT 1
                FROM account_tag
                WHERE account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
            )
        """,
    )
    suspend fun updateDeleted(
        accountId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE tag
        SET emoji = :emoji, title = :title, description = :description, color = :color,
            updated_at = :updatedAt
        WHERE id = :tagId
            AND EXISTS(
                SELECT 1
                FROM account_tag
                WHERE account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
            )
        """,
    )
    suspend fun updateDetail(
        accountId: Uuid,
        tagId: Uuid,
        emoji: String,
        title: String,
        description: String,
        color: Long,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE account_tag
        SET is_dirty = 1
        WHERE account_id = :accountId AND tag_id = :tagId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        tagId: Uuid,
    ): Int
}
