package io.github.taetae98coding.diary.domain.place.usecase

import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.place.repository.GooglePlaceSearchRepository
import io.github.taetae98coding.diary.domain.place.repository.NaverPlaceSearchRepository
import io.github.taetae98coding.diary.domain.place.toPlaceSearchBias
import org.koin.core.annotation.Factory

@Factory
public class FetchSearchedPlaceUseCase internal constructor(
    private val naverPlaceSearchRepository: NaverPlaceSearchRepository,
    private val googlePlaceSearchRepository: GooglePlaceSearchRepository,
) : UseCase<FetchSearchedPlaceUseCase.Parameter, List<SearchedPlace>>() {
    override suspend fun execute(parameter: Parameter): List<SearchedPlace> {
        val query = parameter.query.trim()

        return when (parameter.provider) {
            MapProvider.NAVER -> naverPlaceSearchRepository.fetch(query = query)

            MapProvider.GOOGLE ->
                googlePlaceSearchRepository.fetch(
                    query = query,
                    bias = parameter.bounds?.toPlaceSearchBias(),
                )
        }
    }

    public data class Parameter(
        val query: String,
        val provider: MapProvider,
        val bounds: CoordinateBounds? = null,
    )
}
