package io.github.taetae98coding.diary.core.google.network.api.datasource

import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceLocationBias
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceRemoteEntity

public interface GooglePlaceRemoteDataSource {
    public suspend fun search(
        query: String,
        locationBias: GooglePlaceLocationBias? = null,
    ): List<GooglePlaceRemoteEntity>
}
