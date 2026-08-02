@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.tag.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagFilterRepository
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class PageTagHomeUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagRepository: AccountTagRepository,
    private val accountTagFilterRepository: AccountTagFilterRepository,
) : FlowUseCase<ListSort, PagingData<Tag>>() {
    override fun execute(parameter: ListSort): Flow<Result<PagingData<Tag>>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountTagFilterRepository
                        .getTopLevelOnly(account = account)
                        .flatMapLatest { isTopLevelOnly ->
                            if (isTopLevelOnly) {
                                accountTagRepository.pageTopLevel(account = account, sort = parameter)
                            } else {
                                accountTagRepository.page(account = account, query = "", sort = parameter)
                            }
                        }.map { pagingData -> Result.success(pagingData) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
}
