package io.github.taetae98coding.diary.core.google.network.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GooglePlaceLocationRemoteEntity(
    @SerialName("latitude") val latitude: Double = Double.NaN,
    @SerialName("longitude") val longitude: Double = Double.NaN,
)
