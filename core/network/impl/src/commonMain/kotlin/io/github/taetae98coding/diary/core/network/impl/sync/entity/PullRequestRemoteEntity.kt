package io.github.taetae98coding.diary.core.network.impl.sync.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PullRequestRemoteEntity(
    @SerialName("usn") val usn: Long,
)
