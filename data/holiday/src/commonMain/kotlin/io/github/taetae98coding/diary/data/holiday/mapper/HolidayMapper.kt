package io.github.taetae98coding.diary.data.holiday.mapper

import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.holiday.network.api.entity.HolidayCountryRemoteEntity
import io.github.taetae98coding.diary.core.holiday.network.api.entity.HolidayRemoteEntity
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry

internal fun HolidayRemoteEntity.toLocal(
    country: HolidayCountry,
    year: Int,
): HolidayLocalEntity =
    HolidayLocalEntity(
        country = country.toLocal(),
        year = year,
        name = name,
        isHoliday = isHoliday,
        start = start,
        endInclusive = endInclusive,
    )

internal fun HolidayLocalEntity.toDomain(): Holiday =
    Holiday(
        name = name,
        isHoliday = isHoliday,
        dateRange = start..endInclusive,
    )

internal fun HolidayCountry.toLocal(): HolidayCountryLocalEntity =
    when (this) {
        HolidayCountry.KOREA -> HolidayCountryLocalEntity.KOREA
        HolidayCountry.UNITED_STATES -> HolidayCountryLocalEntity.UNITED_STATES
    }

internal fun HolidayCountry.toRemote(): HolidayCountryRemoteEntity =
    when (this) {
        HolidayCountry.KOREA -> HolidayCountryRemoteEntity.KOREA
        HolidayCountry.UNITED_STATES -> HolidayCountryRemoteEntity.UNITED_STATES
    }
