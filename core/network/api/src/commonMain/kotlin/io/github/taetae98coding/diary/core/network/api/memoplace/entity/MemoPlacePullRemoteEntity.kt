package io.github.taetae98coding.diary.core.network.api.memoplace.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MemoPlacePullRemoteEntity(
    @SerialName("memoPlace") val memoPlace: MemoPlaceRemoteEntity,
    @SerialName("usn") val usn: Long,
)
