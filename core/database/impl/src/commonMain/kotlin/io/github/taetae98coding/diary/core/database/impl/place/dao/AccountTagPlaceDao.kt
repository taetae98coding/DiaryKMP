package io.github.taetae98coding.diary.core.database.impl.place.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
// 표시 범위 안 태그는 재귀 CTE로 모은다. UNION이 이미 담은 태그를 걸러 내므로 연결이 순환해도 조회가 끝난다.
internal interface AccountTagPlaceDao {
    @Query(
        """
        WITH RECURSIVE scoped_tag(tag_id) AS (
            SELECT :tagId
            UNION
            SELECT tag_link.to_tag_id
            FROM scoped_tag
            INNER JOIN tag_link
                ON tag_link.from_tag_id = scoped_tag.tag_id
                    AND tag_link.is_deleted = 0
            INNER JOIN account_tag_link
                ON account_tag_link.from_tag_id = tag_link.from_tag_id
                    AND account_tag_link.to_tag_id = tag_link.to_tag_id
                    AND account_tag_link.account_id = :accountId
            INNER JOIN account_tag AS from_account_tag
                ON from_account_tag.tag_id = tag_link.from_tag_id
                    AND from_account_tag.account_id = :accountId
            INNER JOIN tag AS to_tag
                ON to_tag.id = tag_link.to_tag_id
                    AND to_tag.is_deleted = 0
            INNER JOIN account_tag AS to_account_tag
                ON to_account_tag.tag_id = tag_link.to_tag_id
                    AND to_account_tag.account_id = :accountId
            WHERE :scope = 'descendant'
                OR (:scope = 'child' AND scoped_tag.tag_id = :tagId)
        )
        SELECT place.*
        FROM place
        INNER JOIN account_place
            ON account_place.place_id = place.id AND account_place.account_id = :accountId
        WHERE place.is_deleted = 0
            AND EXISTS(
                SELECT 1
                FROM place_tag
                INNER JOIN scoped_tag
                    ON scoped_tag.tag_id = place_tag.tag_id
                INNER JOIN account_place_tag
                    ON account_place_tag.place_id = place_tag.place_id
                        AND account_place_tag.tag_id = place_tag.tag_id
                        AND account_place_tag.account_id = :accountId
                INNER JOIN tag
                    ON tag.id = place_tag.tag_id
                INNER JOIN account_tag
                    ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
                WHERE place_tag.place_id = place.id
                    AND place_tag.is_deleted = 0
            )
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN place.updated_at END DESC,
            place.title ASC
        """,
    )
    fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: String,
        sort: String,
    ): PagingSource<Int, PlaceLocalEntity>

    // 서쪽 경도가 동쪽 경도보다 크면 날짜변경선을 걸친 영역이므로 양쪽 범위를 모두 영역 안으로 다룬다.
    @Query(
        """
        WITH RECURSIVE scoped_tag(tag_id) AS (
            SELECT :tagId
            UNION
            SELECT tag_link.to_tag_id
            FROM scoped_tag
            INNER JOIN tag_link
                ON tag_link.from_tag_id = scoped_tag.tag_id
                    AND tag_link.is_deleted = 0
            INNER JOIN account_tag_link
                ON account_tag_link.from_tag_id = tag_link.from_tag_id
                    AND account_tag_link.to_tag_id = tag_link.to_tag_id
                    AND account_tag_link.account_id = :accountId
            INNER JOIN account_tag AS from_account_tag
                ON from_account_tag.tag_id = tag_link.from_tag_id
                    AND from_account_tag.account_id = :accountId
            INNER JOIN tag AS to_tag
                ON to_tag.id = tag_link.to_tag_id
                    AND to_tag.is_deleted = 0
            INNER JOIN account_tag AS to_account_tag
                ON to_account_tag.tag_id = tag_link.to_tag_id
                    AND to_account_tag.account_id = :accountId
            WHERE :scope = 'descendant'
                OR (:scope = 'child' AND scoped_tag.tag_id = :tagId)
        )
        SELECT place.*
        FROM place
        INNER JOIN account_place
            ON account_place.place_id = place.id AND account_place.account_id = :accountId
        WHERE place.is_deleted = 0
            AND place.latitude BETWEEN :south AND :north
            AND (
                (:west <= :east AND place.longitude BETWEEN :west AND :east)
                OR (:west > :east AND (place.longitude >= :west OR place.longitude <= :east))
            )
            AND EXISTS(
                SELECT 1
                FROM place_tag
                INNER JOIN scoped_tag
                    ON scoped_tag.tag_id = place_tag.tag_id
                INNER JOIN account_place_tag
                    ON account_place_tag.place_id = place_tag.place_id
                        AND account_place_tag.tag_id = place_tag.tag_id
                        AND account_place_tag.account_id = :accountId
                INNER JOIN tag
                    ON tag.id = place_tag.tag_id
                INNER JOIN account_tag
                    ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
                WHERE place_tag.place_id = place.id
                    AND place_tag.is_deleted = 0
            )
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN place.updated_at END DESC,
            place.title ASC
        """,
    )
    fun get(
        accountId: Uuid,
        tagId: Uuid,
        scope: String,
        south: Double,
        north: Double,
        west: Double,
        east: Double,
        sort: String,
    ): Flow<List<PlaceLocalEntity>>
}
