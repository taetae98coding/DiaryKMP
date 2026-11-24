package io.github.taetae98coding.diary.domain.search.usecase

import androidx.paging.PagingData
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.search.repository.AccountSearchMemoRepository
import kotlinx.coroutines.flow.Flow

@Inject
public class SearchMemoUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountSearchMemoRepository: AccountSearchMemoRepository,
) : FlowUseCase<SearchMemoUseCase.Parameter, PagingData<Memo>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Memo>>> =
        getAccountUseCase.searchPagingFlow(query = parameter.query) { account, query ->
            accountSearchMemoRepository.page(
                account = account,
                query = query,
                sort = parameter.sort,
            )
        }

    public data class Parameter(
        val query: String,
        val sort: ListSort,
    )
}
