@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountCalendarMemoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateRange
import org.koin.core.annotation.Factory

@Factory
public class GetCalendarMemoUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountCalendarMemoRepository: AccountCalendarMemoRepository,
) : FlowUseCase<LocalDateRange, List<CalendarMemo>>() {
    override fun execute(parameter: LocalDateRange): Flow<Result<List<CalendarMemo>>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountCalendarMemoRepository
                        .get(
                            account = account,
                            dateRange = parameter,
                        ).map { memoList -> Result.success(memoList) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
}
