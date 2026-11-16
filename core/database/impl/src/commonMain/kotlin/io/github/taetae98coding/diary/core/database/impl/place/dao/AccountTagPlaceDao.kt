package io.github.taetae98coding.diary.core.database.impl.place.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.dao.SCOPED_TAG_CTE
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface AccountTagPlaceDao {
    @Query(
        SCOPED_TAG_CTE +
            """
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
        SCOPED_TAG_CTE +
            """
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
