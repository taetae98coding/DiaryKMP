package io.github.taetae98coding.diary.core.network.impl.placetag.entity

import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PlaceTagPushRequestRemoteEntity(
    @SerialName("placeTagList") val placeTagList: List<PlaceTagRemoteEntity>,
)
