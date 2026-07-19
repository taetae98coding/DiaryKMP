package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import kotlin.uuid.Uuid

// 질의의 `%`, `_`가 와일드카드로 해석되지 않도록 LIKE 대신 INSTR로 부분 일치를 판정한다.
@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface SearchPlaceDao {
    @Query(
        """
        SELECT place.*
        FROM place
        INNER JOIN account_place
            ON account_place.place_id = place.id AND account_place.account_id = :accountId
        WHERE place.is_deleted = 0
            AND (
                INSTR(LOWER(place.title), LOWER(:query)) > 0
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
}
