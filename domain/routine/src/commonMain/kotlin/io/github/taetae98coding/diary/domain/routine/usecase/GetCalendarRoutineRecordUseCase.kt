package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.routine.repository.AccountCalendarRoutineRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory

@Factory
public class GetCalendarRoutineRecordUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountCalendarRoutineRecordRepository: AccountCalendarRoutineRecordRepository,
) : FlowUseCase<LocalDateRange, List<CalendarRoutineRecord>>() {
    override fun execute(parameter: LocalDateRange): Flow<Result<List<CalendarRoutineRecord>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountCalendarRoutineRecordRepository
                .get(
                    account = account,
                    dateRange = parameter,
                ).map { recordList -> Result.success(recordList) }
        }
}
