package io.github.taetae98coding.diary.core.network.api.memocontact.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MemoContactPullRemoteEntity(
    @SerialName("memoContact") val memoContact: MemoContactRemoteEntity,
    @SerialName("usn") val usn: Long,
)
