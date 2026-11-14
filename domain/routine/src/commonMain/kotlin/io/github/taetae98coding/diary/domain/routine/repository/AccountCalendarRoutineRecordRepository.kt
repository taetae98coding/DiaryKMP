package io.github.taetae98coding.diary.domain.routine.repository

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange

public interface AccountCalendarRoutineRecordRepository {
    public fun get(
        account: Account,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarRoutineRecord>>
}
