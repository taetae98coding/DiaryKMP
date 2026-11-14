package io.github.taetae98coding.diary.core.database.api.routine.datasource

import io.github.taetae98coding.diary.core.database.api.routine.entity.CalendarRoutineRecordLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange
import kotlin.uuid.Uuid

public interface AccountCalendarRoutineRecordLocalDataSource {
    public fun get(
        accountId: Uuid,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarRoutineRecordLocalEntity>>
}
