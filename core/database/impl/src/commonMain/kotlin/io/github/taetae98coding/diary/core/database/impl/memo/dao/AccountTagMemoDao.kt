package io.github.taetae98coding.diary.core.database.impl.memo.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
// 표시 범위 안 태그는 재귀 CTE로 모은다. UNION이 이미 담은 태그를 걸러 내므로 연결이 순환해도 조회가 끝난다.
internal interface AccountTagMemoDao {
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
