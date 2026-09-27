package io.github.taetae98coding.diary.domain.place.usecase

import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.place.exception.PlaceCoordinateInvalidException
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
import io.github.taetae98coding.diary.domain.place.toPlacePrecision
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class UpdatePlaceUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val findPlaceUseCase: FindPlaceUseCase,
    private val accountPlaceRepository: AccountPlaceRepository,
    private val clock: Clock,
) : UseCase<UpdatePlaceUseCase.Parameter, Int>() {
    override suspend fun execute(parameter: Parameter): Int {
        val coordinate = parameter.detail.coordinate.toPlacePrecision()
        if (!coordinate.isRepresentable) throw PlaceCoordinateInvalidException()

        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()
        val detail =
            parameter.detail.copy(
                coordinate = coordinate,
                title =
                    parameter.detail.title.ifBlank {
                        findPlaceUseCase(parameter.id)
                            .first()
                            .getOrThrow()
                            ?.detail
                            ?.title
                            .orEmpty()
                    },
            )

        val updatedCount =
            accountPlaceRepository.updateDetail(
                account = account,
                placeId = parameter.id,
                detail = detail,
                updatedAt = clock.now(),
            )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return updatedCount
    }

    public data class Parameter(
        val id: Uuid,
        val detail: PlaceDetail,
    )
}
