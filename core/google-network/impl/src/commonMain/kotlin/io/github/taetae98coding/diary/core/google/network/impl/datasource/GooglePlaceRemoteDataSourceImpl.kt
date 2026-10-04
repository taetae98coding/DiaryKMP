package io.github.taetae98coding.diary.core.google.network.impl.datasource

import io.github.taetae98coding.diary.core.google.network.api.datasource.GooglePlaceRemoteDataSource
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceLocationBiasRemoteEntity
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceRemoteEntity
import io.github.taetae98coding.diary.core.google.network.impl.di.GoogleHttpClient
import io.github.taetae98coding.diary.core.google.network.impl.entity.GooglePlaceCircleRequestRemoteEntity
import io.github.taetae98coding.diary.core.google.network.impl.entity.GooglePlaceLatLngRequestRemoteEntity
import io.github.taetae98coding.diary.core.google.network.impl.entity.GooglePlaceLocationBiasRequestRemoteEntity
import io.github.taetae98coding.diary.core.google.network.impl.entity.GooglePlaceSearchRequestRemoteEntity
import io.github.taetae98coding.diary.core.google.network.impl.entity.GooglePlaceSearchResponseRemoteEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.koin.core.annotation.Factory

@Factory
internal class GooglePlaceRemoteDataSourceImpl(
    @GoogleHttpClient
    private val httpClient: HttpClient,
) : GooglePlaceRemoteDataSource {
    override suspend fun search(
        query: String,
        locationBias: GooglePlaceLocationBiasRemoteEntity?,
    ): List<GooglePlaceRemoteEntity> =
        httpClient
            .post(SEARCH_TEXT_PATH) {
                header(FIELD_MASK_HEADER, FIELD_MASK)
                setBody(
                    GooglePlaceSearchRequestRemoteEntity(
                        textQuery = query,
                        languageCode = LANGUAGE_CODE,
                        pageSize = MAX_PAGE_SIZE,
                        locationBias = locationBias?.toRequestEntity(),
                    ),
                )
            }.body<GooglePlaceSearchResponseRemoteEntity>()
            .places

    private fun GooglePlaceLocationBiasRemoteEntity.toRequestEntity(): GooglePlaceLocationBiasRequestRemoteEntity =
        GooglePlaceLocationBiasRequestRemoteEntity(
            circle =
                GooglePlaceCircleRequestRemoteEntity(
                    center =
                        GooglePlaceLatLngRequestRemoteEntity(
                            latitude = latitude,
                            longitude = longitude,
                        ),
                    radius = radiusMeters.coerceAtMost(MAX_RADIUS_METERS),
                ),
        )

    companion object {
        private const val SEARCH_TEXT_PATH = "v1/places:searchText"
        private const val MAX_PAGE_SIZE = 20
        private const val MAX_RADIUS_METERS = 50_000.0
        private const val LANGUAGE_CODE = "ko"
        private const val FIELD_MASK_HEADER = "X-Goog-FieldMask"
        private const val FIELD_MASK =
            "places.id,places.displayName,places.location,places.types,places.formattedAddress,places.shortFormattedAddress,places.googleMapsUri"
    }
}
