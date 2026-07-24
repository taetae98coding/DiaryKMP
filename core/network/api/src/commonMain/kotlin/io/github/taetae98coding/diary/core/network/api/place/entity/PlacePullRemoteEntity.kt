package io.github.taetae98coding.diary.core.network.api.place.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PlacePullRemoteEntity(
    @SerialName("place") val place: PlaceRemoteEntity,
    @SerialName("usn") val usn: Long,
)
