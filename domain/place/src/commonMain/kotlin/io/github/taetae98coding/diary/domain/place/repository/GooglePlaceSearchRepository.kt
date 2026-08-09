package io.github.taetae98coding.diary.domain.place.repository

import io.github.taetae98coding.diary.core.model.location.CoordinateCircle
import io.github.taetae98coding.diary.core.model.place.SearchedPlace

public interface GooglePlaceSearchRepository {
    public suspend fun fetch(
        query: String,
        bias: CoordinateCircle?,
    ): List<SearchedPlace>
}
