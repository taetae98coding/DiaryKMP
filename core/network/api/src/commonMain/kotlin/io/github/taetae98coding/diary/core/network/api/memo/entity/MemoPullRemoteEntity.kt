package io.github.taetae98coding.diary.core.network.api.memo.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MemoPullRemoteEntity(
    @SerialName("memo") val memo: MemoRemoteEntity,
    @SerialName("usn") val usn: Long,
)
