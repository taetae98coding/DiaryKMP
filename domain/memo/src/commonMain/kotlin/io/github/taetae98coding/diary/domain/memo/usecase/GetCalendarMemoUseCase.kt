package io.github.taetae98coding.diary.domain.memo.usecase

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountCalendarMemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateRange

@Inject
public class GetCalendarMemoUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountCalendarMemoRepository: AccountCalendarMemoRepository,
) : FlowUseCase<LocalDateRange, List<CalendarMemo>>() {
    override fun execute(parameter: LocalDateRange): Flow<Result<List<CalendarMemo>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountCalendarMemoRepository
                .get(
                    account = account,
                    dateRange = parameter,
                ).map { memoList -> Result.success(memoList) }
        }
}
