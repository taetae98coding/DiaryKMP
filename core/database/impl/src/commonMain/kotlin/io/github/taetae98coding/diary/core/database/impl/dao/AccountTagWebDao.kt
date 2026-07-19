package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
// 표시 범위 안 태그는 재귀 CTE로 모은다. UNION이 이미 담은 태그를 걸러 내므로 연결이 순환해도 조회가 끝난다.
internal interface AccountTagWebDao {
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
        SELECT web.*
        FROM web
        INNER JOIN account_web
            ON account_web.web_id = web.id AND account_web.account_id = :accountId
        WHERE web.is_deleted = 0
            AND EXISTS(
                SELECT 1
                FROM web_tag
                INNER JOIN scoped_tag
                    ON scoped_tag.tag_id = web_tag.tag_id
                INNER JOIN account_web_tag
                    ON account_web_tag.web_id = web_tag.web_id
                        AND account_web_tag.tag_id = web_tag.tag_id
                        AND account_web_tag.account_id = :accountId
                INNER JOIN tag
                    ON tag.id = web_tag.tag_id
                INNER JOIN account_tag
                    ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
                WHERE web_tag.web_id = web.id
                    AND web_tag.is_deleted = 0
            )
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN web.updated_at END DESC,
            web.title ASC
        """,
    )
    fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: String,
        sort: String,
    ): PagingSource<Int, WebLocalEntity>
}
