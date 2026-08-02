@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.tag.usecase

import androidx.paging.PagingData
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
public class PageTagLinkSelectableTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagLinkRepository: AccountTagLinkRepository,
) : FlowUseCase<PageTagLinkSelectableTagUseCase.Parameter, PagingData<Tag>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Tag>>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountTagLinkRepository
                        .pageSelectableTag(
                            account = account,
                            fromTagId = parameter.fromTagId,
                            query = parameter.query.trim(),
                        ).map { pagingData -> Result.success(pagingData) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }

    public data class Parameter(
        val fromTagId: Uuid,
        val query: String,
    )
}
