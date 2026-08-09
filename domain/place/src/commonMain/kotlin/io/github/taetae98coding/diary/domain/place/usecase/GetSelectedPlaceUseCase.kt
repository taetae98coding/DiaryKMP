@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.place.usecase

import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class GetSelectedPlaceUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountPlaceRepository: AccountPlaceRepository,
) : FlowUseCase<Set<Uuid>, List<Place>>() {
    override fun execute(parameter: Set<Uuid>): Flow<Result<List<Place>>> {
        if (parameter.isEmpty()) return flowOf(Result.success(emptyList()))

        return getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountPlaceRepository.get(account = account, placeIdSet = parameter).map { placeList ->
                        Result.success(placeList)
                    }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
    }
}
