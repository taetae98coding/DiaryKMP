package io.github.taetae98coding.diary.core.database.impl.ddaytag.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.ddaytag.entity.AccountDDayTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagDao
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
// 검색어의 `%`, `_`가 와일드카드로 해석되지 않도록 LIKE 대신 INSTR로 부분 일치를 판정한다.
internal interface AccountDDayTagDao : AccountEntityTagDao<AccountDDayTagLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        INNER JOIN dday_tag
            ON dday_tag.tag_id = tag.id
                AND dday_tag.dday_id = :entityId
                AND dday_tag.is_deleted = 0
        INNER JOIN account_dday_tag
            ON account_dday_tag.dday_id = dday_tag.dday_id
                AND account_dday_tag.tag_id = dday_tag.tag_id
                AND account_dday_tag.account_id = :accountId
        INNER JOIN dday
            ON dday.id = dday_tag.dday_id
        INNER JOIN account_dday
            ON account_dday.dday_id = dday.id AND account_dday.account_id = :accountId
        WHERE tag.is_deleted = 0
        ORDER BY tag.title ASC
        """,
    )
    override fun getTagList(
        accountId: Uuid,
        entityId: Uuid,
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
                    FROM dday_tag
                    INNER JOIN account_dday_tag
                        ON account_dday_tag.dday_id = dday_tag.dday_id
                            AND account_dday_tag.tag_id = dday_tag.tag_id
                            AND account_dday_tag.account_id = :accountId
                    INNER JOIN account_dday
                        ON account_dday.dday_id = dday_tag.dday_id AND account_dday.account_id = :accountId
                    WHERE dday_tag.tag_id = tag.id
                        AND dday_tag.dday_id = :entityId
                        AND dday_tag.is_deleted = 0
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
    override fun pageSelectableTag(
        accountId: Uuid,
        entityId: Uuid,
        query: String,
    ): PagingSource<Int, TagLocalEntity>

    @Query(
        """
        UPDATE account_dday_tag
        SET is_dirty = 1
        WHERE account_id = :accountId AND dday_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun markPending(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
    ): Int
}
