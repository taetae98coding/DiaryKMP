package io.github.taetae98coding.diary.domain.holiday.repository

import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import kotlinx.coroutines.flow.Flow

public interface HolidayRepository {
    public suspend fun fetch(
        country: HolidayCountry,
        year: Int,
    ): List<Holiday>

    public fun get(countrySet: Set<HolidayCountry>): Flow<List<Holiday>>

    public fun get(
        countrySet: Set<HolidayCountry>,
        year: Int,
    ): Flow<List<Holiday>>
}
