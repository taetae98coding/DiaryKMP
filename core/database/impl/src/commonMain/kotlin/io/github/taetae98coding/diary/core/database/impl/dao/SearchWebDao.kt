package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import kotlin.uuid.Uuid

// 질의의 `%`, `_`가 와일드카드로 해석되지 않도록 LIKE 대신 INSTR로 부분 일치를 판정한다.
@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface SearchWebDao {
    @Query(
        """
        SELECT web.*
        FROM web
        INNER JOIN account_web
            ON account_web.web_id = web.id AND account_web.account_id = :accountId
        WHERE web.is_deleted = 0
            AND (
                INSTR(LOWER(web.title), LOWER(:query)) > 0
                OR INSTR(LOWER(web.description), LOWER(:query)) > 0
                OR INSTR(LOWER(web.url), LOWER(:query)) > 0
            )
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN web.updated_at END DESC,
            web.title ASC
        """,
    )
    fun page(
        accountId: Uuid,
        query: String,
        sort: String,
    ): PagingSource<Int, WebLocalEntity>
}
