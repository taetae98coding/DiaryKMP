package io.github.taetae98coding.diary.core.database.impl.memo.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.dao.SCOPED_TAG_CTE
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountTagMemoDao {
    @Query(
        SCOPED_TAG_CTE +
            """
        SELECT memo.*
        FROM memo
        INNER JOIN account_memo
            ON account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
        WHERE memo.is_finished = 0
            AND memo.is_deleted = 0
            AND EXISTS(
                SELECT 1
                FROM memo_tag
                INNER JOIN scoped_tag
                    ON scoped_tag.tag_id = memo_tag.tag_id
                INNER JOIN account_memo_tag
                    ON account_memo_tag.memo_id = memo_tag.memo_id
                        AND account_memo_tag.tag_id = memo_tag.tag_id
                        AND account_memo_tag.account_id = :accountId
                INNER JOIN tag
                    ON tag.id = memo_tag.tag_id
                INNER JOIN account_tag
                    ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
                WHERE memo_tag.memo_id = memo.id
                    AND memo_tag.is_deleted = 0
            )
        ORDER BY
            CASE WHEN :sort = 'default' THEN memo.start IS NULL END DESC,
            CASE WHEN :sort = 'default' THEN date(memo.start) END ASC,
            CASE WHEN :sort = 'default' THEN memo.is_all_day END DESC,
            CASE WHEN :sort = 'default' THEN memo.start END ASC,
            CASE WHEN :sort = 'default' THEN memo.end_inclusive END ASC,
            CASE WHEN :sort = 'recently_updated' THEN memo.updated_at END DESC,
            memo.title ASC
        """,
    )
    fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: String,
        sort: String,
    ): PagingSource<Int, MemoLocalEntity>

    @Query(
        """
        SELECT memo.*
        FROM memo
        INNER JOIN account_memo
            ON account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
        INNER JOIN memo_tag
            ON memo_tag.memo_id = memo.id
                AND memo_tag.tag_id = :tagId
                AND memo_tag.is_deleted = 0
        INNER JOIN account_memo_tag
            ON account_memo_tag.memo_id = memo_tag.memo_id
                AND account_memo_tag.tag_id = memo_tag.tag_id
                AND account_memo_tag.account_id = :accountId
        INNER JOIN tag
            ON tag.id = memo_tag.tag_id
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE memo.is_finished = 1
            AND memo.is_deleted = 0
        ORDER BY
            CASE WHEN :sort = 'default' THEN memo.start IS NULL END DESC,
            CASE WHEN :sort = 'default' THEN date(memo.start) END ASC,
            CASE WHEN :sort = 'default' THEN memo.is_all_day END DESC,
            CASE WHEN :sort = 'default' THEN memo.start END ASC,
            CASE WHEN :sort = 'default' THEN memo.end_inclusive END ASC,
            CASE WHEN :sort = 'recently_updated' THEN memo.updated_at END DESC,
            memo.title ASC
        """,
    )
    fun pageFinished(
        accountId: Uuid,
        tagId: Uuid,
        sort: String,
    ): PagingSource<Int, MemoLocalEntity>
}
