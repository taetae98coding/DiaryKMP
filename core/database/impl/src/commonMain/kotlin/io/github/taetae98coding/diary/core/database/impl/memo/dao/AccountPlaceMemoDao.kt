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
internal interface AccountPlaceMemoDao {
    @Query(
        """
        SELECT memo.*
        FROM memo
        INNER JOIN account_memo
            ON account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
        INNER JOIN memo_place
            ON memo_place.memo_id = memo.id
                AND memo_place.place_id = :placeId
                AND memo_place.is_deleted = 0
        INNER JOIN account_memo_place
            ON account_memo_place.memo_id = memo_place.memo_id
                AND account_memo_place.place_id = memo_place.place_id
                AND account_memo_place.account_id = :accountId
        WHERE memo.is_finished = 0
            AND memo.is_deleted = 0
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
        placeId: Uuid,
        sort: String,
    ): PagingSource<Int, MemoLocalEntity>
}
