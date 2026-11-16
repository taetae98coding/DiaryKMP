package io.github.taetae98coding.diary.core.database.impl.web.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.dao.SCOPED_TAG_CTE
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountTagWebDao {
    @Query(
        SCOPED_TAG_CTE +
            """
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
