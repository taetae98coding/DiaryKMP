package io.github.taetae98coding.diary.data.routine.repository

import io.github.taetae98coding.diary.core.database.api.routine.datasource.AccountCalendarRoutineRecordLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.data.routine.mapper.toDomain
import io.github.taetae98coding.diary.domain.routine.repository.AccountCalendarRoutineRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory

@Factory
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
