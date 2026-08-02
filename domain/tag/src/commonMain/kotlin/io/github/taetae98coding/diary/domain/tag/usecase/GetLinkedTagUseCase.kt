@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.tag.usecase

import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagLinkRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class GetLinkedTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagLinkRepository: AccountTagLinkRepository,
) : FlowUseCase<Uuid, List<Tag>>() {
    override fun execute(parameter: Uuid): Flow<Result<List<Tag>>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountTagLinkRepository
                        .getTagList(
                            account = account,
                            fromTagId = parameter,
                        ).map { tagList -> Result.success(tagList) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
}
