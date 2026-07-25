package io.github.taetae98coding.diary.core.naver.network.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NaverPlaceRemoteEntity(
    @SerialName("title") val title: String,
    @SerialName("link") val link: String,
    @SerialName("category") val category: String,
    @SerialName("description") val description: String,
    @SerialName("address") val address: String,
    @SerialName("roadAddress") val roadAddress: String,
    @SerialName("mapx") val mapx: String,
    @SerialName("mapy") val mapy: String,
)
