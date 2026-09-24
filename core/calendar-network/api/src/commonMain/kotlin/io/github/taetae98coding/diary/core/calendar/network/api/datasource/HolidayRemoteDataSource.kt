package io.github.taetae98coding.diary.core.calendar.network.api.datasource

import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayCountryRemoteEntity
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayRemoteEntity

public interface HolidayRemoteDataSource {
    public suspend fun get(
        country: HolidayCountryRemoteEntity,
        year: Int,
    ): List<HolidayRemoteEntity>
}
