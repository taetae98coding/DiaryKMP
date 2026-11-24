package io.github.taetae98coding.diary.domain.contact.usecase

import dev.zacsweers.metro.Inject
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

@Inject
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
            // 음력 자료를 읽지 못해도 생일 조회 전체를 실패로 만들지 않고, 음력 생일만 빼고 양력 생일을 전달한다.
            lunarRepository.get(dateRange = dateRange).catch { emit(emptyList()) },
        ) { solarBirthdayList, lunarBirthdayList, lunarDateList ->
            val birthdayList = solarBirthdayList + lunarBirthdayList.toCalendarContactBirthdayList(lunarDateList = lunarDateList)

            Result.success(birthdayList.sortedForCalendar())
        }
}
