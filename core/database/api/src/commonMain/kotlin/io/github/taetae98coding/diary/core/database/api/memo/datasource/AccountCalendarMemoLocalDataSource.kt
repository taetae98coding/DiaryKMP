package io.github.taetae98coding.diary.core.database.api.memo.datasource

import io.github.taetae98coding.diary.core.database.api.memo.entity.CalendarMemoLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange
import kotlin.uuid.Uuid

public interface AccountCalendarMemoLocalDataSource {
    public fun get(
        accountId: Uuid,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarMemoLocalEntity>>
}
