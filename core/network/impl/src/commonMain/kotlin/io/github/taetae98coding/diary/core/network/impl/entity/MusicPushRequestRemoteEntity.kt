package io.github.taetae98coding.diary.core.network.impl.entity

import io.github.taetae98coding.diary.core.network.api.music.entity.MusicRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MusicPushRequestRemoteEntity(
    @SerialName("musicList") val musicList: List<MusicRemoteEntity>,
)
