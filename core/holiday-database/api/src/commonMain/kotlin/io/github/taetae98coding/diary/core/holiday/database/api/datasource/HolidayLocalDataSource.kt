package io.github.taetae98coding.diary.core.holiday.database.api.datasource

import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity
import kotlinx.coroutines.flow.Flow

public interface HolidayLocalDataSource {
    public fun get(countrySet: Set<HolidayCountryLocalEntity>): Flow<List<HolidayLocalEntity>>

    public fun get(
        countrySet: Set<HolidayCountryLocalEntity>,
        year: Int,
    ): Flow<List<HolidayLocalEntity>>
}
