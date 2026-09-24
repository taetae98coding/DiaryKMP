package io.github.taetae98coding.diary.core.holiday.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow

@Dao
internal interface HolidayDao : RoomDao<HolidayLocalEntity> {
    @Query(
        """
        SELECT *
        FROM holiday
        WHERE country IN (:countryList)
        ORDER BY name ASC, country ASC
        """,
    )
    fun get(countryList: List<HolidayCountryLocalEntity>): Flow<List<HolidayLocalEntity>>

    @Query(
        """
        SELECT *
        FROM holiday
        WHERE country IN (:countryList) AND year = :year
        ORDER BY start ASC, end_inclusive ASC, name ASC, country ASC
        """,
    )
    fun get(
        countryList: List<HolidayCountryLocalEntity>,
        year: Int,
    ): Flow<List<HolidayLocalEntity>>

    @Query("DELETE FROM holiday WHERE country = :country AND year = :year")
    suspend fun delete(
        country: HolidayCountryLocalEntity,
        year: Int,
    )
}
