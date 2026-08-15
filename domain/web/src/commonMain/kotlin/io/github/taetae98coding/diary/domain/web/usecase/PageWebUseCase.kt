@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.web.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.web.repository.AccountWebRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class PageWebUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountWebRepository: AccountWebRepository,
) : FlowUseCase<ListSort, PagingData<Web>>() {
    override fun execute(parameter: ListSort): Flow<Result<PagingData<Web>>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountWebRepository
                        .page(
                            account = account,
                            sort = parameter,
                        ).map { pagingData -> Result.success(pagingData) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
}
