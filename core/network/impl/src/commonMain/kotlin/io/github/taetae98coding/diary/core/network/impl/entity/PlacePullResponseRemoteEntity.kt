package io.github.taetae98coding.diary.core.network.impl.entity

import io.github.taetae98coding.diary.core.network.api.place.entity.PlacePullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PlacePullResponseRemoteEntity(
    @SerialName("placeList") val placeList: List<PlacePullRemoteEntity>,
)
