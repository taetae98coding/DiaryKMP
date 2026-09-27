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
internal interface AccountWebMemoDao {
    @Query(
        """
        SELECT memo.*
        FROM memo
        INNER JOIN account_memo
            ON account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
        INNER JOIN memo_web
            ON memo_web.memo_id = memo.id
                AND memo_web.web_id = :webId
                AND memo_web.is_deleted = 0
        INNER JOIN account_memo_web
            ON account_memo_web.memo_id = memo_web.memo_id
                AND account_memo_web.web_id = memo_web.web_id
                AND account_memo_web.account_id = :accountId
        WHERE memo.is_finished = 0
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
    fun page(
        accountId: Uuid,
        webId: Uuid,
        sort: String,
    ): PagingSource<Int, MemoLocalEntity>
}
