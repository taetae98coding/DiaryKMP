package io.github.taetae98coding.diary.core.database.impl.taglink.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.taglink.entity.AccountTagLinkLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
// 검색어의 `%`, `_`가 와일드카드로 해석되지 않도록 LIKE 대신 INSTR로 부분 일치를 판정한다.
internal interface AccountTagLinkDao : RoomDao<AccountTagLinkLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        INNER JOIN tag_link
            ON tag_link.to_tag_id = tag.id
                AND tag_link.from_tag_id = :fromTagId
                AND tag_link.is_deleted = 0
        INNER JOIN account_tag_link
            ON account_tag_link.from_tag_id = tag_link.from_tag_id
                AND account_tag_link.to_tag_id = tag_link.to_tag_id
                AND account_tag_link.account_id = :accountId
        INNER JOIN account_tag AS from_account_tag
            ON from_account_tag.tag_id = tag_link.from_tag_id AND from_account_tag.account_id = :accountId
        WHERE tag.is_deleted = 0
        ORDER BY tag.title ASC
        """,
    )
    fun getTagList(
        accountId: Uuid,
        fromTagId: Uuid,
    ): Flow<List<TagLocalEntity>>

    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE tag.is_deleted = 0
            AND tag.id != :fromTagId
            AND (
                tag.is_finished = 0
                OR EXISTS(
                    SELECT 1
                    FROM tag_link
                    INNER JOIN account_tag_link
                        ON account_tag_link.from_tag_id = tag_link.from_tag_id
                            AND account_tag_link.to_tag_id = tag_link.to_tag_id
                            AND account_tag_link.account_id = :accountId
                    WHERE tag_link.from_tag_id = :fromTagId
                        AND tag_link.to_tag_id = tag.id
                        AND tag_link.is_deleted = 0
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
        fromTagId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity>

    @Query(
        """
        UPDATE account_tag_link
        SET is_dirty = 1
        WHERE account_id = :accountId AND from_tag_id = :fromTagId AND to_tag_id = :toTagId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        fromTagId: Uuid,
        toTagId: Uuid,
    ): Int
}
