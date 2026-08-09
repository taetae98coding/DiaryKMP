package io.github.taetae98coding.diary.domain.location.usecase

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.location.repository.LocationRepository
import org.koin.core.annotation.Factory

@Factory
public class FetchCurrentLocationUseCase internal constructor(
    private val locationRepository: LocationRepository,
) : UseCase<Unit, Coordinate>() {
    override suspend fun execute(parameter: Unit): Coordinate = locationRepository.fetch()
}
