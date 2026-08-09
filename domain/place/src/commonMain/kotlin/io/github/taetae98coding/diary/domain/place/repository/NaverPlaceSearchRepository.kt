package io.github.taetae98coding.diary.domain.place.repository

import io.github.taetae98coding.diary.core.model.place.SearchedPlace

public interface NaverPlaceSearchRepository {
    public suspend fun fetch(query: String): List<SearchedPlace>
}
