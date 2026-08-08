package io.github.taetae98coding.diary.core.holiday.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow

@Dao
internal interface HolidayDao : RoomDao<HolidayLocalEntity> {
    @Query(
        """
        SELECT *
        FROM holiday
        ORDER BY name ASC
        """,
    )
    fun get(): Flow<List<HolidayLocalEntity>>

    @Query(
        """
        SELECT *
        FROM holiday
        WHERE year = :year
        ORDER BY start ASC, end_inclusive ASC, name ASC
        """,
    )
    fun get(year: Int): Flow<List<HolidayLocalEntity>>

    @Query("DELETE FROM holiday WHERE year = :year")
    suspend fun delete(year: Int)
}
