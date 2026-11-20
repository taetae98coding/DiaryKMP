package io.github.taetae98coding.diary.domain.search.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal fun <T : Any> GetAccountUseCase.searchPagingFlow(
    query: String,
    page: (account: Account, query: String) -> Flow<PagingData<T>>,
): Flow<Result<PagingData<T>>> {
    val trimmedQuery = query.trim()

    if (trimmedQuery.isEmpty()) return flowOf(Result.success(PagingData.empty()))

    return flatMapAccount { account ->
        page(account, trimmedQuery).map { pagingData -> Result.success(pagingData) }
    }
}
