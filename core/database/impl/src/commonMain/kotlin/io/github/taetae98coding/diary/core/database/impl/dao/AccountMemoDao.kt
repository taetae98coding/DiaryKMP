package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountMemoDao : RoomDao<AccountMemoLocalEntity> {
    @Query(
        """
        WITH memo_filter_selected_tag AS (
            SELECT memo_filter_tag.tag_id
            FROM memo_filter_tag
            INNER JOIN tag
                ON tag.id = memo_filter_tag.tag_id
                    AND tag.is_finished = 0
                    AND tag.is_deleted = 0
            INNER JOIN account_tag
                ON account_tag.tag_id = memo_filter_tag.tag_id AND account_tag.account_id = :accountId
            WHERE memo_filter_tag.account_id = :accountId
        ),
        memo_filter_existence AS (
            SELECT has_date, has_tag, has_place
            FROM memo_existence_filter
        )
        SELECT memo.*
        FROM memo
        INNER JOIN account_memo
            ON account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
        WHERE memo.is_finished = 0 AND memo.is_deleted = 0
            AND (
                IFNULL((SELECT has_tag FROM memo_filter_existence), 1) = 0
                OR NOT EXISTS(SELECT 1 FROM memo_filter_selected_tag)
                OR EXISTS(
                    SELECT 1
                    FROM memo_tag
                    INNER JOIN account_memo_tag
                        ON account_memo_tag.memo_id = memo_tag.memo_id
                            AND account_memo_tag.tag_id = memo_tag.tag_id
                            AND account_memo_tag.account_id = :accountId
                    WHERE memo_tag.memo_id = memo.id
                        AND memo_tag.is_deleted = 0
                        AND memo_tag.tag_id IN (SELECT tag_id FROM memo_filter_selected_tag)
                )
            )
            AND (
                (SELECT has_date FROM memo_filter_existence) IS NULL
                OR (
                    memo.is_all_day IS NOT NULL
                        AND memo.start IS NOT NULL
                        AND memo.end_inclusive IS NOT NULL
                ) = (SELECT has_date FROM memo_filter_existence)
            )
            AND (
                (SELECT has_tag FROM memo_filter_existence) IS NULL
                OR EXISTS(
                    SELECT 1
                    FROM memo_tag
                    INNER JOIN account_memo_tag
                        ON account_memo_tag.memo_id = memo_tag.memo_id
                            AND account_memo_tag.tag_id = memo_tag.tag_id
                            AND account_memo_tag.account_id = :accountId
                    INNER JOIN tag
                        ON tag.id = memo_tag.tag_id AND tag.is_deleted = 0
                    INNER JOIN account_tag
                        ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
                    WHERE memo_tag.memo_id = memo.id AND memo_tag.is_deleted = 0
                ) = (SELECT has_tag FROM memo_filter_existence)
            )
            AND (
                (SELECT has_place FROM memo_filter_existence) IS NULL
                OR EXISTS(
                    SELECT 1
                    FROM memo_place
                    INNER JOIN account_memo_place
                        ON account_memo_place.memo_id = memo_place.memo_id
                            AND account_memo_place.place_id = memo_place.place_id
                            AND account_memo_place.account_id = :accountId
                    INNER JOIN place
                        ON place.id = memo_place.place_id AND place.is_deleted = 0
                    INNER JOIN account_place
                        ON account_place.place_id = place.id AND account_place.account_id = :accountId
                    WHERE memo_place.memo_id = memo.id AND memo_place.is_deleted = 0
                ) = (SELECT has_place FROM memo_filter_existence)
            )
        ORDER BY
            CASE WHEN :sort = 'default' THEN memo.start IS NULL END DESC,
            CASE WHEN :sort = 'default' THEN memo.start END ASC,
            CASE WHEN :sort = 'default' THEN memo.end_inclusive END ASC,
            CASE WHEN :sort = 'recently_updated' THEN memo.updated_at END DESC,
            memo.title ASC
        """,
    )
    fun page(
        accountId: Uuid,
        sort: String,
    ): PagingSource<Int, MemoLocalEntity>

    @Query(
        """
        SELECT memo.*
        FROM memo
        INNER JOIN account_memo
            ON account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
        WHERE memo.is_finished = 1 AND memo.is_deleted = 0
        ORDER BY
            CASE WHEN :sort = 'default' THEN memo.start IS NULL END DESC,
            CASE WHEN :sort = 'default' THEN memo.start END ASC,
            CASE WHEN :sort = 'default' THEN memo.end_inclusive END ASC,
            CASE WHEN :sort = 'recently_updated' THEN memo.updated_at END DESC,
            memo.title ASC
        """,
    )
    fun pageFinished(
        accountId: Uuid,
        sort: String,
    ): PagingSource<Int, MemoLocalEntity>

    @Query(
        """
        SELECT memo.*
        FROM memo
        INNER JOIN account_memo
            ON account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
        WHERE memo.id = :memoId
        """,
    )
    fun find(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<MemoLocalEntity?>

    @Query(
        """
        UPDATE memo
        SET is_finished = :isFinished, updated_at = :updatedAt
        WHERE id = :memoId
            AND EXISTS(
                SELECT 1
                FROM account_memo
                WHERE account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
            )
        """,
    )
    suspend fun updateFinished(
        accountId: Uuid,
        memoId: Uuid,
        isFinished: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE memo
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE id = :memoId
            AND EXISTS(
                SELECT 1
                FROM account_memo
                WHERE account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
            )
        """,
    )
    suspend fun updateDeleted(
        accountId: Uuid,
        memoId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE memo
        SET title = :title, description = :description, color = :color,
            is_all_day = :isAllDay, start = :start, end_inclusive = :endInclusive,
            updated_at = :updatedAt
        WHERE id = :memoId
            AND EXISTS(
                SELECT 1
                FROM account_memo
                WHERE account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
            )
        """,
    )
    suspend fun updateDetail(
        accountId: Uuid,
        memoId: Uuid,
        title: String,
        description: String,
        color: Long,
        isAllDay: Boolean?,
        start: LocalDateTime?,
        endInclusive: LocalDateTime?,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE memo
        SET primary_tag_id = :primaryTagId, updated_at = :updatedAt
        WHERE id = :memoId
            AND EXISTS(
                SELECT 1
                FROM account_memo
                WHERE account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
            )
        """,
    )
    suspend fun updatePrimaryTagId(
        accountId: Uuid,
        memoId: Uuid,
        primaryTagId: Uuid?,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE memo
        SET primary_tag_id = NULL, updated_at = :updatedAt
        WHERE id = :memoId
            AND primary_tag_id = :tagId
            AND EXISTS(
                SELECT 1
                FROM account_memo
                WHERE account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
            )
        """,
    )
    suspend fun clearPrimaryTagIdIfMatched(
        accountId: Uuid,
        memoId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE account_memo
        SET is_dirty = 1
        WHERE account_id = :accountId AND memo_id = :memoId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        memoId: Uuid,
    ): Int
}
