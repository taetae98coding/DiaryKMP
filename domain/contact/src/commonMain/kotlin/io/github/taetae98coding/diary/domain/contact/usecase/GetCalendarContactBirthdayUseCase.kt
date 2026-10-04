package io.github.taetae98coding.diary.domain.contact.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.contact.repository.AccountCalendarContactBirthdayRepository
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.lunar.repository.LunarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory

@Factory
public class GetCalendarContactBirthdayUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountCalendarContactBirthdayRepository: AccountCalendarContactBirthdayRepository,
    private val lunarRepository: LunarRepository,
) : FlowUseCase<LocalDateRange, List<CalendarContactBirthday>>() {
    override fun execute(parameter: LocalDateRange): Flow<Result<List<CalendarContactBirthday>>> = getAccountUseCase.flatMapAccount { account -> birthdayListFlow(account = account, dateRange = parameter) }

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
