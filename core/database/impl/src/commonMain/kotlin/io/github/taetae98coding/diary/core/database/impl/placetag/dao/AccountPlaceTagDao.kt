package io.github.taetae98coding.diary.core.database.impl.placetag.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.AccountEntityTagDao
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.AccountPlaceTagLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
// 검색어의 `%`, `_`가 와일드카드로 해석되지 않도록 LIKE 대신 INSTR로 부분 일치를 판정한다.
internal interface AccountPlaceTagDao : AccountEntityTagDao<AccountPlaceTagLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        INNER JOIN place_tag
            ON place_tag.tag_id = tag.id
                AND place_tag.place_id = :entityId
                AND place_tag.is_deleted = 0
        INNER JOIN account_place_tag
            ON account_place_tag.place_id = place_tag.place_id
                AND account_place_tag.tag_id = place_tag.tag_id
                AND account_place_tag.account_id = :accountId
        INNER JOIN place
            ON place.id = place_tag.place_id
        INNER JOIN account_place
            ON account_place.place_id = place.id AND account_place.account_id = :accountId
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
                    FROM place_tag
                    INNER JOIN account_place_tag
                        ON account_place_tag.place_id = place_tag.place_id
                            AND account_place_tag.tag_id = place_tag.tag_id
                            AND account_place_tag.account_id = :accountId
                    INNER JOIN account_place
                        ON account_place.place_id = place_tag.place_id AND account_place.account_id = :accountId
                    WHERE place_tag.tag_id = tag.id
                        AND place_tag.place_id = :entityId
                        AND place_tag.is_deleted = 0
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
        UPDATE account_place_tag
        SET is_dirty = 1
        WHERE account_id = :accountId AND place_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun markPending(
        accountId: Uuid,
        entityId: Uuid,
        tagId: Uuid,
    ): Int
}
