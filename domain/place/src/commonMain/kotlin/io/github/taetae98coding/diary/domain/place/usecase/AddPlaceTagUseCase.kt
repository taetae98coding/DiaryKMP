package io.github.taetae98coding.diary.domain.place.usecase

import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceTagRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddPlaceTagUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountPlaceTagRepository: AccountPlaceTagRepository,
    private val clock: Clock,
) : UseCase<AddPlaceTagUseCase.Parameter, Unit>() {
    override suspend fun execute(parameter: Parameter) {
        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()

        accountPlaceTagRepository.upsert(
            account = account,
            placeId = parameter.placeId,
            tagId = parameter.tagId,
            isDeleted = false,
            updatedAt = clock.now(),
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
    }

    public data class Parameter(
        val placeId: Uuid,
        val tagId: Uuid,
    )
}
