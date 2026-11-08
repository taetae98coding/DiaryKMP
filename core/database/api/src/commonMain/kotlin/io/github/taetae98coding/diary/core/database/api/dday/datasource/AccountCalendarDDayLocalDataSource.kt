package io.github.taetae98coding.diary.core.database.api.dday.datasource

import io.github.taetae98coding.diary.core.database.api.dday.entity.CalendarDDayLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange
import kotlin.uuid.Uuid

public interface AccountCalendarDDayLocalDataSource {
    public fun get(
        accountId: Uuid,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarDDayLocalEntity>>
}
