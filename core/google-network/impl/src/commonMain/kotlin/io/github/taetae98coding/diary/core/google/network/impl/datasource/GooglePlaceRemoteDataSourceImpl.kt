package io.github.taetae98coding.diary.core.google.network.impl.datasource

import io.github.taetae98coding.diary.core.google.network.api.datasource.GooglePlaceRemoteDataSource
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceLocationBias
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceRemoteEntity
import io.github.taetae98coding.diary.core.google.network.impl.di.GoogleHttpClient
import io.github.taetae98coding.diary.core.google.network.impl.entity.GooglePlaceCircleRequestEntity
import io.github.taetae98coding.diary.core.google.network.impl.entity.GooglePlaceLatLngRequestEntity
import io.github.taetae98coding.diary.core.google.network.impl.entity.GooglePlaceLocationBiasRequestEntity
import io.github.taetae98coding.diary.core.google.network.impl.entity.GooglePlaceSearchRequestEntity
import io.github.taetae98coding.diary.core.google.network.impl.entity.GooglePlaceSearchResponseEntity
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
        locationBias: GooglePlaceLocationBias?,
    ): List<GooglePlaceRemoteEntity> =
        httpClient
            .post(SEARCH_TEXT_PATH) {
                header(FIELD_MASK_HEADER, FIELD_MASK)
                setBody(
                    GooglePlaceSearchRequestEntity(
                        textQuery = query,
                        languageCode = LANGUAGE_CODE,
                        pageSize = MAX_PAGE_SIZE,
                        locationBias = locationBias?.toRequestEntity(),
                    ),
                )
            }.body<GooglePlaceSearchResponseEntity>()
            .places

    private fun GooglePlaceLocationBias.toRequestEntity(): GooglePlaceLocationBiasRequestEntity =
        GooglePlaceLocationBiasRequestEntity(
            circle =
                GooglePlaceCircleRequestEntity(
                    center =
                        GooglePlaceLatLngRequestEntity(
                            latitude = latitude,
                            longitude = longitude,
                        ),
                    // Google은 상한을 넘는 반경의 요청을 거절하므로 상한까지만 보낸다.
                    radius = radiusMeters.coerceAtMost(MAX_RADIUS_METERS),
                ),
        )

    companion object {
        private const val SEARCH_TEXT_PATH = "v1/places:searchText"
        private const val MAX_PAGE_SIZE = 20
        private const val MAX_RADIUS_METERS = 50_000.0
        private const val LANGUAGE_CODE = "ko"
        private const val FIELD_MASK_HEADER = "X-Goog-FieldMask"

        // 과금 등급이 Pro를 넘지 않는 항목만 요청한다.
        private const val FIELD_MASK =
            "places.id,places.displayName,places.location,places.types,places.formattedAddress,places.shortFormattedAddress,places.googleMapsUri"
    }
}
