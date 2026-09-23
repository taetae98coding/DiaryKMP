package io.github.taetae98coding.diary.core.database.impl.place.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.place.entity.AccountPlaceLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
// 검색어의 `%`, `_`가 와일드카드로 해석되지 않도록 LIKE 대신 INSTR로 부분 일치를 판정한다.
internal interface AccountPlaceDao : RoomDao<AccountPlaceLocalEntity> {
    @Query(
        """
        SELECT place.*
        FROM place
        INNER JOIN account_place
            ON account_place.place_id = place.id AND account_place.account_id = :accountId
        WHERE place.is_deleted = 0
            AND (
                :query = ''
                OR INSTR(LOWER(place.title), LOWER(:query)) > 0
                OR INSTR(LOWER(place.description), LOWER(:query)) > 0
                OR INSTR(LOWER(place.address), LOWER(:query)) > 0
            )
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN place.updated_at END DESC,
            place.title ASC
        """,
    )
    fun page(
        accountId: Uuid,
        query: String,
        sort: String,
    ): PagingSource<Int, PlaceLocalEntity>

    @Query(
        """
        SELECT place.*
        FROM place
        INNER JOIN account_place
            ON account_place.place_id = place.id AND account_place.account_id = :accountId
        WHERE place.is_deleted = 0 AND place.id IN (:placeIdSet)
        ORDER BY place.title ASC
        """,
    )
    fun get(
        accountId: Uuid,
        placeIdSet: Set<Uuid>,
    ): Flow<List<PlaceLocalEntity>>

    // 서쪽 경도가 동쪽 경도보다 크면 날짜변경선을 걸친 영역이므로 양쪽 범위를 모두 영역 안으로 다룬다.
    @Query(
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
        ORDER BY
            CASE WHEN :sort = 'recently_updated' THEN place.updated_at END DESC,
            place.title ASC
        """,
    )
    fun get(
        accountId: Uuid,
        south: Double,
        north: Double,
        west: Double,
        east: Double,
        sort: String,
    ): Flow<List<PlaceLocalEntity>>

    @Query(
        """
        SELECT place.*
        FROM place
        INNER JOIN account_place
            ON account_place.place_id = place.id AND account_place.account_id = :accountId
        WHERE place.id = :placeId
        """,
    )
    fun find(
        accountId: Uuid,
        placeId: Uuid,
    ): Flow<PlaceLocalEntity?>

    @Query(
        """
        UPDATE place
        SET title = :title, description = :description, color = :color,
            latitude = :latitude, longitude = :longitude, address = :address, updated_at = :updatedAt
        WHERE id = :placeId
            AND EXISTS(
                SELECT 1
                FROM account_place
                WHERE account_place.place_id = place.id AND account_place.account_id = :accountId
            )
        """,
    )
    suspend fun updateDetail(
        accountId: Uuid,
        placeId: Uuid,
        title: String,
        description: String,
        color: Long,
        latitude: Double,
        longitude: Double,
        address: String,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE place
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE id = :placeId
            AND EXISTS(
                SELECT 1
                FROM account_place
                WHERE account_place.place_id = place.id AND account_place.account_id = :accountId
            )
        """,
    )
    suspend fun updateDeleted(
        accountId: Uuid,
        placeId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int

    @Query(
        """
        UPDATE account_place
        SET is_dirty = 1
        WHERE account_id = :accountId AND place_id = :placeId
        """,
    )
    suspend fun markPending(
        accountId: Uuid,
        placeId: Uuid,
    ): Int
}
