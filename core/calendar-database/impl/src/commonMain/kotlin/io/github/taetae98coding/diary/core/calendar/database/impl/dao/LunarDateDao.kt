package io.github.taetae98coding.diary.core.calendar.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.calendar.database.api.entity.LunarDateLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
internal interface LunarDateDao : RoomDao<LunarDateLocalEntity> {
    @Query(
        """
        SELECT *
        FROM lunar_date
        WHERE solar >= :start AND solar <= :endInclusive
        ORDER BY solar ASC
        """,
    )
    fun get(
        start: LocalDate,
        endInclusive: LocalDate,
    ): Flow<List<LunarDateLocalEntity>>

    @Query("DELETE FROM lunar_date WHERE solar_year = :solarYear")
    suspend fun delete(solarYear: Int)
}
