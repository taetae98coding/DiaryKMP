@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.contact.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.repository.AccountCalendarContactBirthdayRepository
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.lunar.repository.LunarRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory

@Factory
public class GetCalendarContactBirthdayUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountCalendarContactBirthdayRepository: AccountCalendarContactBirthdayRepository,
    private val lunarRepository: LunarRepository,
) : FlowUseCase<LocalDateRange, List<CalendarContactBirthday>>() {
    override fun execute(parameter: LocalDateRange): Flow<Result<List<CalendarContactBirthday>>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account -> birthdayListFlow(account = account, dateRange = parameter) },
                onFailure = { throwable -> flowOf(Result.failure(throwable)) },
            )
        }

    private fun birthdayListFlow(
        account: Account,
        dateRange: LocalDateRange,
    ): Flow<Result<List<CalendarContactBirthday>>> =
        combine(
            accountCalendarContactBirthdayRepository.get(account = account, dateRange = dateRange),
            accountCalendarContactBirthdayRepository.getLunar(account = account),
            lunarRepository.get(dateRange = dateRange).catch { emit(emptyList()) },
        ) { solarBirthdayList, lunarBirthdayList, lunarDateList ->
            val birthdayList = solarBirthdayList + lunarBirthdayList.toCalendarContactBirthdayList(lunarDateList = lunarDateList)

            Result.success(birthdayList.sortedForCalendar())
        }
}
