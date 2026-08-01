package io.github.taetae98coding.diary.domain.memo.repository

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange

public interface AccountCalendarMemoRepository {
    public fun get(
        account: Account,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarMemo>>
}
