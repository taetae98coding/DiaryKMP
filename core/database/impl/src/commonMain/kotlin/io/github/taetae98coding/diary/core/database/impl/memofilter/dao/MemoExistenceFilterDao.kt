package io.github.taetae98coding.diary.core.database.impl.memofilter.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.memofilter.entity.MemoExistenceFilterLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow

@Dao
internal interface MemoExistenceFilterDao : RoomDao<MemoExistenceFilterLocalEntity> {
    @Query(
        """
        SELECT *
        FROM memo_existence_filter
        WHERE id = :id
        """,
    )
    fun find(id: Int): Flow<MemoExistenceFilterLocalEntity?>

    @Query(
        """
        UPDATE memo_existence_filter
        SET has_date = :hasDate
        WHERE id = :id
        """,
    )
    suspend fun updateHasDate(
        id: Int,
        hasDate: Boolean?,
    )

    @Query(
        """
        UPDATE memo_existence_filter
        SET has_tag = :hasTag
        WHERE id = :id
        """,
    )
    suspend fun updateHasTag(
        id: Int,
        hasTag: Boolean?,
    )

    @Query(
        """
        UPDATE memo_existence_filter
        SET has_place = :hasPlace
        WHERE id = :id
        """,
    )
    suspend fun updateHasPlace(
        id: Int,
        hasPlace: Boolean?,
    )
}
