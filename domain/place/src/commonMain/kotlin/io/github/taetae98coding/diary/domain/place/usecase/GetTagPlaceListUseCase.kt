@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.place.usecase

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
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
public class GetTagPlaceListUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountTagPlaceRepository: AccountTagPlaceRepository,
) : FlowUseCase<GetTagPlaceListUseCase.Parameter, List<Place>>() {
    override fun execute(parameter: Parameter): Flow<Result<List<Place>>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountTagPlaceRepository
                        .get(
                            account = account,
                            tagId = parameter.tagId,
                            scope = parameter.scope,
                            bounds = parameter.bounds,
                            sort = parameter.sort,
                        ).map { placeList -> Result.success(placeList) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }

    public data class Parameter(
        val tagId: Uuid,
        val scope: TagScope,
        val bounds: CoordinateBounds,
        val sort: ListSort,
    )
}
