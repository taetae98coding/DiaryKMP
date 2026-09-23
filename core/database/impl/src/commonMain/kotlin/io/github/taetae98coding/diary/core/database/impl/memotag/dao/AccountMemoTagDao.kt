package io.github.taetae98coding.diary.core.database.impl.memotag.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memotag.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
// 검색어의 `%`, `_`가 와일드카드로 해석되지 않도록 LIKE 대신 INSTR로 부분 일치를 판정한다.
internal interface AccountMemoTagDao : RoomDao<AccountMemoTagLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        INNER JOIN memo_tag
            ON memo_tag.tag_id = tag.id
                AND memo_tag.memo_id = :memoId
                AND memo_tag.is_deleted = 0
        INNER JOIN account_memo_tag
            ON account_memo_tag.memo_id = memo_tag.memo_id
                AND account_memo_tag.tag_id = memo_tag.tag_id
                AND account_memo_tag.account_id = :accountId
        INNER JOIN memo
            ON memo.id = memo_tag.memo_id
        WHERE tag.is_deleted = 0
        ORDER BY tag.title ASC
        """,
    )
    fun getTagList(
        accountId: Uuid,
        memoId: Uuid,
    ): Flow<List<TagLocalEntity>>

    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE tag.is_deleted = 0
            AND (
                tag.is_finished = 0
                OR EXISTS(
                    SELECT 1
                    FROM memo_tag
                    INNER JOIN account_memo_tag
                        ON account_memo_tag.memo_id = memo_tag.memo_id
                            AND account_memo_tag.tag_id = memo_tag.tag_id
                            AND account_memo_tag.account_id = :accountId
                    WHERE memo_tag.tag_id = tag.id
                        AND memo_tag.memo_id = :memoId
                        AND memo_tag.is_deleted = 0
                )
            )
            AND (
                :query = ''
                OR INSTR(LOWER(tag.emoji), LOWER(:query)) > 0
                OR INSTR(LOWER(tag.title), LOWER(:query)) > 0
                OR INSTR(LOWER(tag.description), LOWER(:query)) > 0
            )
        ORDER BY tag.title ASC
        """,
    )
    fun pageSelectableTag(
        accountId: Uuid,
        memoId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity>

    @Query(
        """
        SELECT memo_tag.tag_id
        FROM memo_tag
        INNER JOIN account_tag
            ON account_tag.tag_id = memo_tag.tag_id AND account_tag.account_id = :accountId
        INNER JOIN account_memo_tag
            ON account_memo_tag.memo_id = memo_tag.memo_id
                AND account_memo_tag.tag_id = memo_tag.tag_id
                AND account_memo_tag.account_id = :accountId
        WHERE memo_tag.memo_id = :memoId AND memo_tag.is_deleted = 0
        """,
    )
    suspend fun findTagIdList(
        accountId: Uuid,
        memoId: Uuid,
    ): List<Uuid>

    @Query(
        """
        UPDATE account_memo_tag
        SET is_dirty = 1
        WHERE account_id = :accountId AND memo_id = :memoId AND tag_id = :tagId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        memoId: Uuid,
        tagId: Uuid,
    ): Int
}
