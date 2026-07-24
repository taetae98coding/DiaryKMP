package io.github.taetae98coding.diary.core.network.api.memoweb.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MemoWebPullRemoteEntity(
    @SerialName("memoWeb") val memoWeb: MemoWebRemoteEntity,
    @SerialName("usn") val usn: Long,
)
