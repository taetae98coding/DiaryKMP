package io.github.taetae98coding.diary.core.google.network.impl.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GooglePlaceCircleRequestEntity(
    @SerialName("center") val center: GooglePlaceLatLngRequestEntity,
    @SerialName("radius") val radius: Double,
)
