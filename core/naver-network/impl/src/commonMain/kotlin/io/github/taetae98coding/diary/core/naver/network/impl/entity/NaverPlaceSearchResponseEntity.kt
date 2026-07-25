package io.github.taetae98coding.diary.core.naver.network.impl.entity

import io.github.taetae98coding.diary.core.naver.network.api.entity.NaverPlaceRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NaverPlaceSearchResponseEntity(
    @SerialName("items") val items: List<NaverPlaceRemoteEntity>,
)
