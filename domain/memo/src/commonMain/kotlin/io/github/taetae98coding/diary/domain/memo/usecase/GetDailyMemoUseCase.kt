@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.core.model.memo.DailyMemo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountDailyMemoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Factory

@Factory
public class GetDailyMemoUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountDailyMemoRepository: AccountDailyMemoRepository,
) : FlowUseCase<LocalDate, List<DailyMemo>>() {
    override fun execute(parameter: LocalDate): Flow<Result<List<DailyMemo>>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountDailyMemoRepository
                        .get(
                            account = account,
                            date = parameter,
                        ).map { memoList -> Result.success(memoList) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
}
