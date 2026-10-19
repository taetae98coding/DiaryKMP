package io.github.taetae98coding.diary.domain.checklist.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.checklist.repository.AccountChecklistRepository
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class PageChecklistUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountChecklistRepository: AccountChecklistRepository,
) : FlowUseCase<Unit, PagingData<ChecklistSummary>>() {
    override fun execute(parameter: Unit): Flow<Result<PagingData<ChecklistSummary>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountChecklistRepository
                .page(account = account)
                .map { pagingData -> Result.success(pagingData) }
        }
}
