package io.github.taetae98coding.diary.core.google.network.impl.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GooglePlaceLatLngRequestEntity(
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
)
