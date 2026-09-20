package io.github.taetae98coding.diary.data.memo.repository

import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountCalendarMemoLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.data.memo.mapper.toDomain
import io.github.taetae98coding.diary.domain.memo.repository.AccountCalendarMemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory

@Factory
internal class AccountCalendarMemoRepositoryImpl(
    private val accountCalendarMemoLocalDataSource: AccountCalendarMemoLocalDataSource,
) : AccountCalendarMemoRepository {
    override fun get(
        account: Account,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarMemo>> =
        accountCalendarMemoLocalDataSource
            .get(
                accountId = account.id,
                dateRange = dateRange,
            ).map { localList -> localList.map { local -> local.toDomain() } }
}
