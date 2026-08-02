@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.tag.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagFilterRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetTopLevelTagFilterUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagFilterRepository: AccountTagFilterRepository,
) : FlowUseCase<Unit, Boolean>() {
    override fun execute(parameter: Unit): Flow<Result<Boolean>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountTagFilterRepository
                        .getTopLevelOnly(account = account)
                        .map { isTopLevelOnly -> Result.success(isTopLevelOnly) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
}
