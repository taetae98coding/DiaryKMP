package io.github.taetae98coding.diary.core.network.api.music.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MusicPullRemoteEntity(
    @SerialName("music") val music: MusicRemoteEntity,
    @SerialName("usn") val usn: Long,
)
