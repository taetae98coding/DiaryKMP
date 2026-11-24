package io.github.taetae98coding.diary.data.routine.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.routine.datasource.AccountCalendarRoutineRecordLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.data.routine.mapper.toDomain
import io.github.taetae98coding.diary.domain.routine.repository.AccountCalendarRoutineRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateRange

@Inject
@ContributesBinding(AppScope::class)
internal class AccountCalendarRoutineRecordRepositoryImpl(
    private val accountCalendarRoutineRecordLocalDataSource: AccountCalendarRoutineRecordLocalDataSource,
) : AccountCalendarRoutineRecordRepository {
    override fun get(
        account: Account,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarRoutineRecord>> =
        accountCalendarRoutineRecordLocalDataSource
            .get(
                accountId = account.id,
                dateRange = dateRange,
            ).map { localList -> localList.map { local -> local.toDomain() } }
}
