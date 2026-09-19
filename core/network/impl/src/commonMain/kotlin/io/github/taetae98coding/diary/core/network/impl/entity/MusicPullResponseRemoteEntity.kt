package io.github.taetae98coding.diary.core.network.impl.entity

import io.github.taetae98coding.diary.core.network.api.music.entity.MusicPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MusicPullResponseRemoteEntity(
    @SerialName("musicList") val musicList: List<MusicPullRemoteEntity>,
)
