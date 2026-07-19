package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountWebTagLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
// 검색어의 `%`, `_`가 와일드카드로 해석되지 않도록 LIKE 대신 INSTR로 부분 일치를 판정한다.
internal interface AccountWebTagDao : RoomDao<AccountWebTagLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        INNER JOIN web_tag
            ON web_tag.tag_id = tag.id
                AND web_tag.web_id = :webId
                AND web_tag.is_deleted = 0
        INNER JOIN account_web_tag
            ON account_web_tag.web_id = web_tag.web_id
                AND account_web_tag.tag_id = web_tag.tag_id
                AND account_web_tag.account_id = :accountId
        INNER JOIN web
            ON web.id = web_tag.web_id
        WHERE tag.is_deleted = 0
        ORDER BY tag.title ASC
        """,
    )
    fun getTagList(
        accountId: Uuid,
        webId: Uuid,
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
                    FROM web_tag
                    INNER JOIN account_web_tag
                        ON account_web_tag.web_id = web_tag.web_id
                            AND account_web_tag.tag_id = web_tag.tag_id
                            AND account_web_tag.account_id = :accountId
                    WHERE web_tag.tag_id = tag.id
                        AND web_tag.web_id = :webId
                        AND web_tag.is_deleted = 0
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
        webId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity>

    @Query(
        """
        UPDATE account_web_tag
        SET is_dirty = 1
        WHERE account_id = :accountId AND web_id = :webId AND tag_id = :tagId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        webId: Uuid,
        tagId: Uuid,
    ): Int
}
