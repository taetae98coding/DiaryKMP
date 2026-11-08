package io.github.taetae98coding.diary.domain.dday.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class PageDDayUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountDDayRepository: AccountDDayRepository,
) : FlowUseCase<Unit, PagingData<DDay>>() {
    override fun execute(parameter: Unit): Flow<Result<PagingData<DDay>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountDDayRepository
                .page(account = account)
                .map { pagingData -> Result.success(pagingData) }
        }
}
