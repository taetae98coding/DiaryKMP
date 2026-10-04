package io.github.taetae98coding.diary.core.google.network.impl.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GooglePlaceLocationBiasRequestRemoteEntity(
    @SerialName("circle") val circle: GooglePlaceCircleRequestRemoteEntity,
)
