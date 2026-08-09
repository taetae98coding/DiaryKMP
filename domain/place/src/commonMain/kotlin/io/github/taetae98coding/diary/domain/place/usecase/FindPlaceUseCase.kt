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
public class FindPlaceUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val accountPlaceRepository: AccountPlaceRepository,
) : FlowUseCase<Uuid, Place?>() {
    override fun execute(parameter: Uuid): Flow<Result<Place?>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = { account ->
                    accountPlaceRepository
                        .find(
                            account = account,
                            placeId = parameter,
                        ).map { place -> Result.success(place) }
                },
                onFailure = { throwable ->
                    flowOf(Result.failure(throwable))
                },
            )
        }
}
