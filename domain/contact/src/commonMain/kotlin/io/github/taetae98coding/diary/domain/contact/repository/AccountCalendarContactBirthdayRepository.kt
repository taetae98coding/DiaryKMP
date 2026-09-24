package io.github.taetae98coding.diary.domain.contact.repository

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.contact.LunarContactBirthday
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateRange

public interface AccountCalendarContactBirthdayRepository {
    public fun get(
        account: Account,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarContactBirthday>>

    public fun getLunar(account: Account): Flow<List<LunarContactBirthday>>
}
