package io.github.taetae98coding.diary.data.dday.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.dday.datasource.AccountCalendarDDayLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.data.dday.mapper.toDomain
import io.github.taetae98coding.diary.domain.dday.repository.AccountCalendarDDayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateRange

@Inject
@ContributesBinding(AppScope::class)
internal class AccountCalendarDDayRepositoryImpl(
    private val accountCalendarDDayLocalDataSource: AccountCalendarDDayLocalDataSource,
) : AccountCalendarDDayRepository {
    override fun get(
        account: Account,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarDDay>> =
        accountCalendarDDayLocalDataSource
            .get(
                accountId = account.id,
                dateRange = dateRange,
            ).map { localList -> localList.map { local -> local.toDomain() } }
}
