package io.github.taetae98coding.diary.core.network.impl.placetag.entity

import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PlaceTagPullResponseRemoteEntity(
    @SerialName("placeTagList") val placeTagList: List<PlaceTagPullRemoteEntity>,
)
