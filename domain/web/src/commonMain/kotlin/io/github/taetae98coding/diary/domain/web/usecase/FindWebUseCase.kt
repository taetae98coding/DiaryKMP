@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.web.usecase

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
import kotlin.uuid.Uuid

@Factory
public class FindWebUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountWebRepository: AccountWebRepository,
) : FlowUseCase<Uuid, Web?>() {
    override fun execute(parameter: Uuid): Flow<Result<Web?>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountWebRepository
                        .find(
                            account = account,
                            webId = parameter,
                        ).map { web -> Result.success(web) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
}
