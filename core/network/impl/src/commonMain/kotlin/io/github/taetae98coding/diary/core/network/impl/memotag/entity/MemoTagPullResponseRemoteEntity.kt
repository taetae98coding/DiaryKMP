package io.github.taetae98coding.diary.core.network.impl.memotag.entity

import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemoTagPullResponseRemoteEntity(
    @SerialName("memoTagList") val memoTagList: List<MemoTagPullRemoteEntity>,
)
