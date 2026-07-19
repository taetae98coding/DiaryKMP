package io.github.taetae98coding.diary.core.database.api.web.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class WebHeaderLocalEntity(
    @SerialName("name") val name: String,
    @SerialName("value") val value: String,
)
