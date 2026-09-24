package io.github.taetae98coding.diary.core.calendar.database.api.transaction

import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayLocalEntity

public interface HolidayTransaction {
    public suspend fun upsert(
        country: HolidayCountryLocalEntity,
        year: Int,
        holidayList: List<HolidayLocalEntity>,
    )
}
