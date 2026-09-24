package io.github.taetae98coding.diary.core.calendar.database.impl.datasource

import io.github.taetae98coding.diary.core.calendar.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.impl.CalendarDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
internal class HolidayLocalDataSourceImpl(
    private val database: CalendarDatabase,
) : HolidayLocalDataSource {
    override fun get(countrySet: Set<HolidayCountryLocalEntity>): Flow<List<HolidayLocalEntity>> = database.holidayDao().get(countryList = countrySet.toList())

    override fun get(
        countrySet: Set<HolidayCountryLocalEntity>,
        year: Int,
    ): Flow<List<HolidayLocalEntity>> = database.holidayDao().get(countryList = countrySet.toList(), year = year)
}
