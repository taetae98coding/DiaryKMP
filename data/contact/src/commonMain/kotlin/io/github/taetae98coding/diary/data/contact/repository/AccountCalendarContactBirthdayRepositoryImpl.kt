package io.github.taetae98coding.diary.data.contact.repository

import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountCalendarContactBirthdayLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.contact.LunarContactBirthday
import io.github.taetae98coding.diary.data.contact.mapper.toDomain
import io.github.taetae98coding.diary.domain.contact.repository.AccountCalendarContactBirthdayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory

@Factory
internal class AccountCalendarContactBirthdayRepositoryImpl(
    private val accountCalendarContactBirthdayLocalDataSource: AccountCalendarContactBirthdayLocalDataSource,
) : AccountCalendarContactBirthdayRepository {
    override fun get(
        account: Account,
        dateRange: LocalDateRange,
    ): Flow<List<CalendarContactBirthday>> =
        accountCalendarContactBirthdayLocalDataSource
            .get(
                accountId = account.id,
                dateRange = dateRange,
            ).map { localList -> localList.map { local -> local.toDomain() } }

    override fun getLunar(account: Account): Flow<List<LunarContactBirthday>> =
        accountCalendarContactBirthdayLocalDataSource
            .getLunar(accountId = account.id)
            .map { localList -> localList.map { local -> local.toDomain() } }
}
