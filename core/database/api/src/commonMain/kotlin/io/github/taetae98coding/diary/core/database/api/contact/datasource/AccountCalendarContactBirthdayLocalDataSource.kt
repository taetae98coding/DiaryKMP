package io.github.taetae98coding.diary.core.database.api.contact.datasource

import io.github.taetae98coding.diary.core.database.api.contact.entity.CalendarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.LunarContactBirthdayLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange
import kotlin.uuid.Uuid

public interface AccountCalendarContactBirthdayLocalDataSource {
    public fun get(
        accountId: Uuid,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarContactBirthdayLocalEntity>>

    public fun getLunar(accountId: Uuid): Flow<List<LunarContactBirthdayLocalEntity>>
}
