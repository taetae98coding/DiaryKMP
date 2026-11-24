package io.github.taetae98coding.diary.core.calendar.database.impl.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.calendar.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.impl.CalendarDatabase
import kotlinx.coroutines.flow.Flow

@Inject
@ContributesBinding(AppScope::class)
internal class HolidayLocalDataSourceImpl(
    private val database: CalendarDatabase,
) : HolidayLocalDataSource {
    override fun get(countrySet: Set<HolidayCountryLocalEntity>): Flow<List<HolidayLocalEntity>> = database.holidayDao().get(countryList = countrySet.toList())

    override fun get(
        countrySet: Set<HolidayCountryLocalEntity>,
        year: Int,
    ): Flow<List<HolidayLocalEntity>> = database.holidayDao().get(countryList = countrySet.toList(), year = year)
}
