@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountCalendarFilterRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetCalendarFilterUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountCalendarFilterRepository: AccountCalendarFilterRepository,
) : FlowUseCase<Unit, List<Tag>>() {
    override fun execute(parameter: Unit): Flow<Result<List<Tag>>> =
        getAccountUseCase(parameter = parameter).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountCalendarFilterRepository.getTagList(account = account).map { tagList ->
                        Result.success(tagList)
                    }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
}
