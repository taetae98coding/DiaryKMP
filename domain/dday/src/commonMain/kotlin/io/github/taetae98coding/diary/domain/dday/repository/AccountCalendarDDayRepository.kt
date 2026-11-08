package io.github.taetae98coding.diary.domain.dday.repository

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange

public interface AccountCalendarDDayRepository {
    public fun get(
        account: Account,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarDDay>>
}
