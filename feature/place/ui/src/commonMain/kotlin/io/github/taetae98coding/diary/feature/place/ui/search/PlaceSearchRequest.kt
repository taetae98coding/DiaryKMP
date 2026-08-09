package io.github.taetae98coding.diary.feature.place.ui.search

import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.place.usecase.FetchSearchedPlaceUseCase

internal data class PlaceSearchRequest(
    val query: String,
    val provider: MapProvider,
    val bounds: CoordinateBounds? = null,
)

internal fun PlaceSearchRequest.toParameter(): FetchSearchedPlaceUseCase.Parameter =
    FetchSearchedPlaceUseCase.Parameter(
        query = query,
        provider = provider,
        bounds = bounds,
    )
