package io.github.taetae98coding.diary.core.network.impl.place.entity

import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PlacePushRequestRemoteEntity(
    @SerialName("placeList") val placeList: List<PlaceRemoteEntity>,
)
