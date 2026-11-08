package io.github.taetae98coding.diary.core.network.api.dday.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class DDayPullRemoteEntity(
    @SerialName("dday") val dday: DDayRemoteEntity,
    @SerialName("usn") val usn: Long,
)
