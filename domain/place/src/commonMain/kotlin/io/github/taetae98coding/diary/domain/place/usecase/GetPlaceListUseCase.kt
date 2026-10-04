package io.github.taetae98coding.diary.domain.place.usecase

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.flatMapAccount
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetPlaceListUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountPlaceRepository: AccountPlaceRepository,
) : FlowUseCase<GetPlaceListUseCase.Parameter, List<Place>>() {
    override fun execute(parameter: Parameter): Flow<Result<List<Place>>> =
        getAccountUseCase.flatMapAccount { account ->
            accountPlaceRepository
                .get(
                    account = account,
                    bounds = parameter.bounds,
                    sort = parameter.sort,
                ).map { placeList -> Result.success(placeList) }
        }

    public data class Parameter(
        val bounds: CoordinateBounds,
        val sort: ListSort,
    )
}
