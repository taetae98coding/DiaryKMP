package io.github.taetae98coding.diary.domain.dday.usecase

import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.dday.repository.AccountCalendarDDayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory

@Factory
public class GetCalendarDDayUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountCalendarDDayRepository: AccountCalendarDDayRepository,
) : FlowUseCase<LocalDateRange, List<CalendarDDay>>() {
    override fun execute(parameter: LocalDateRange): Flow<Result<List<CalendarDDay>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountCalendarDDayRepository
                .get(
                    account = account,
                    dateRange = parameter,
                ).map { ddayList -> Result.success(ddayList) }
        }
}
