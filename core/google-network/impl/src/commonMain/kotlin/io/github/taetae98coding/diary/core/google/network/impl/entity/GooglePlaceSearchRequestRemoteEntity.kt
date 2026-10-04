package io.github.taetae98coding.diary.core.google.network.impl.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GooglePlaceSearchRequestRemoteEntity(
    @SerialName("textQuery") val textQuery: String,
    @SerialName("languageCode") val languageCode: String,
    @SerialName("pageSize") val pageSize: Int,
    @SerialName("locationBias") val locationBias: GooglePlaceLocationBiasRequestRemoteEntity? = null,
)
