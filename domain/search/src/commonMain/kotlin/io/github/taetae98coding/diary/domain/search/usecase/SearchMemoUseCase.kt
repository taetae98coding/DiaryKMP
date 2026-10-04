package io.github.taetae98coding.diary.domain.search.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchMemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class SearchMemoUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountSearchMemoRepository: AccountSearchMemoRepository,
) : FlowUseCase<SearchMemoUseCase.Parameter, PagingData<Memo>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Memo>>> {
        val query = parameter.query.trim()

        if (query.isEmpty()) return flowOf(Result.success(PagingData.empty()))

        return getAccountUseCase.flatMapAccount { account ->
            accountSearchMemoRepository
                .page(
                    account = account,
                    query = query,
                    sort = parameter.sort,
                ).map { pagingData -> Result.success(pagingData) }
        }
    }

    public data class Parameter(
        val query: String,
        val sort: ListSort,
    )
}
