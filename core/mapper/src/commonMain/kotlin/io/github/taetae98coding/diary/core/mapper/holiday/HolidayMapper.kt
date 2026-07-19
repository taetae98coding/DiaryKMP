package io.github.taetae98coding.diary.core.mapper.holiday

import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.core.holiday.network.api.entity.HolidayRemoteEntity
import io.github.taetae98coding.diary.core.model.holiday.Holiday

public fun HolidayRemoteEntity.toLocal(year: Int): HolidayLocalEntity =
    HolidayLocalEntity(
        year = year,
        name = name,
        isHoliday = isHoliday,
        start = start,
        endInclusive = endInclusive,
    )

public fun HolidayLocalEntity.toDomain(): Holiday =
    Holiday(
        name = name,
        isHoliday = isHoliday,
        dateRange = start..endInclusive,
    )
