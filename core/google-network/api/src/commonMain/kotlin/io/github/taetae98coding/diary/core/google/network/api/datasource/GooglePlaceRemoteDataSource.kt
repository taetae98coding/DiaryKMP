package io.github.taetae98coding.diary.core.google.network.api.datasource

import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceLocationBiasRemoteEntity
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceRemoteEntity

public interface GooglePlaceRemoteDataSource {
    public suspend fun search(
        query: String,
        locationBias: GooglePlaceLocationBiasRemoteEntity? = null,
    ): List<GooglePlaceRemoteEntity>
}
