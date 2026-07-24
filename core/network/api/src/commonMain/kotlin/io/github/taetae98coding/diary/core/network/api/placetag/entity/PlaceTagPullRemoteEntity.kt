package io.github.taetae98coding.diary.core.network.api.placetag.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PlaceTagPullRemoteEntity(
    @SerialName("placeTag") val placeTag: PlaceTagRemoteEntity,
    @SerialName("usn") val usn: Long,
)
