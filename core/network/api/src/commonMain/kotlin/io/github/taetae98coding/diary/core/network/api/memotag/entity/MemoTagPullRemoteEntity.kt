package io.github.taetae98coding.diary.core.network.api.memotag.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MemoTagPullRemoteEntity(
    @SerialName("memoTag") val memoTag: MemoTagRemoteEntity,
    @SerialName("usn") val usn: Long,
)
