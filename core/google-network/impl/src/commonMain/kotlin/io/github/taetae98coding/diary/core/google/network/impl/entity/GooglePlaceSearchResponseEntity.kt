package io.github.taetae98coding.diary.core.google.network.impl.entity

import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GooglePlaceSearchResponseEntity(
    @SerialName("places") val places: List<GooglePlaceRemoteEntity> = emptyList(),
)
