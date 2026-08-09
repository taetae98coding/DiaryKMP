@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.place.usecase

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.place.repository.AccountTagPlaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class PageTagPlaceUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagPlaceRepository: AccountTagPlaceRepository,
) : FlowUseCase<PageTagPlaceUseCase.Parameter, PagingData<Place>>() {
    override fun execute(parameter: Parameter): Flow<Result<PagingData<Place>>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountTagPlaceRepository
                        .page(
                            account = account,
                            tagId = parameter.tagId,
                            scope = parameter.scope,
                            sort = parameter.sort,
                        ).map { pagingData -> Result.success(pagingData) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }

    public data class Parameter(
        val tagId: Uuid,
        val scope: TagScope,
        val sort: ListSort,
    )
}
