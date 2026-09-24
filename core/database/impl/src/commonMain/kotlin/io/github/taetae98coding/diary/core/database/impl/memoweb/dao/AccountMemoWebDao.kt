package io.github.taetae98coding.diary.core.database.impl.memoweb.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoweb.entity.AccountMemoWebLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
// 검색어의 `%`, `_`가 와일드카드로 해석되지 않도록 LIKE 대신 INSTR로 부분 일치를 판정한다.
internal interface AccountMemoWebDao : RoomDao<AccountMemoWebLocalEntity> {
    @Query(
        """
        SELECT web.*
        FROM web
        INNER JOIN account_web
            ON account_web.web_id = web.id AND account_web.account_id = :accountId
        INNER JOIN memo_web
            ON memo_web.web_id = web.id
                AND memo_web.memo_id = :memoId
                AND memo_web.is_deleted = 0
        INNER JOIN account_memo_web
            ON account_memo_web.memo_id = memo_web.memo_id
                AND account_memo_web.web_id = memo_web.web_id
                AND account_memo_web.account_id = :accountId
        INNER JOIN memo
            ON memo.id = memo_web.memo_id
        WHERE web.is_deleted = 0
        ORDER BY web.title ASC
        """,
    )
    fun getWebList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<WebLocalEntity>>

    @Query(
        """
        SELECT web.*
        FROM web
        INNER JOIN account_web
            ON account_web.web_id = web.id AND account_web.account_id = :accountId
        WHERE web.is_deleted = 0
            AND (
                :query = ''
                OR INSTR(LOWER(web.title), LOWER(:query)) > 0
                OR INSTR(LOWER(web.description), LOWER(:query)) > 0
            )
        ORDER BY web.title ASC
        """,
    )
    fun pageSelectableWeb(
        accountId: Uuid,
        query: String,
    ): PagingSource<Int, WebLocalEntity>

    @Query(
        """
        SELECT memo_web.web_id
        FROM memo_web
        INNER JOIN account_memo_web
            ON account_memo_web.memo_id = memo_web.memo_id
                AND account_memo_web.web_id = memo_web.web_id
                AND account_memo_web.account_id = :accountId
        WHERE memo_web.memo_id = :memoId AND memo_web.is_deleted = 0
        """,
    )
    suspend fun findWebIdList(
        accountId: Uuid,
        memoId: Uuid,
    ): List<Uuid>

    @Query(
        """
        UPDATE account_memo_web
        SET is_dirty = 1
        WHERE account_id = :accountId AND memo_id = :memoId AND web_id = :webId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        memoId: Uuid,
        webId: Uuid,
    ): Int
}
