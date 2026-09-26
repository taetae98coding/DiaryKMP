package io.github.taetae98coding.diary.domain.place.usecase

import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.place.exception.PlaceCoordinateInvalidException
import io.github.taetae98coding.diary.domain.place.exception.PlaceTitleBlankException
import io.github.taetae98coding.diary.domain.place.repository.AccountPlaceRepository
import io.github.taetae98coding.diary.domain.place.toPlacePrecision
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Factory
public class AddPlaceUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val requestSyncUseCase: RequestSyncUseCase,
    private val accountPlaceRepository: AccountPlaceRepository,
    private val clock: Clock,
) : UseCase<AddPlaceUseCase.Parameter, Uuid>() {
    override suspend fun execute(parameter: Parameter): Uuid {
        val detail = parameter.detail.copy(coordinate = parameter.detail.coordinate.toPlacePrecision())
        if (detail.title.isBlank()) throw PlaceTitleBlankException()
        if (!detail.coordinate.isRepresentable) throw PlaceCoordinateInvalidException()

        val account = getAccountUseCase(parameter = Unit).first().getOrThrow()
        val now = clock.now()
        val place =
            Place(
                id = Uuid.random(),
                detail = detail,
                isDeleted = false,
                updatedAt = now,
                createdAt = now,
            )

        accountPlaceRepository.upsert(
            account = account,
            place = place,
            tagIdSet = parameter.tagIdSet,
        )

        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)

        return place.id
    }

    public data class Parameter(
        val detail: PlaceDetail,
        val tagIdSet: Set<Uuid>,
    )
}
