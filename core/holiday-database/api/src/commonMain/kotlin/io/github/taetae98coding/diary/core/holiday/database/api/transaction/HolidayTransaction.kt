package io.github.taetae98coding.diary.core.holiday.database.api.transaction

import io.github.taetae98coding.diary.core.holiday.database.api.entity.HolidayLocalEntity

public interface HolidayTransaction {
    public suspend fun upsert(
        year: Int,
        holidayList: List<HolidayLocalEntity>,
    )
}
