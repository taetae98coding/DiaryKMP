package io.github.taetae98coding.diary.core.network.api.checklist.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ChecklistPullRemoteEntity(
    @SerialName("checklist") val checklist: ChecklistRemoteEntity,
    @SerialName("usn") val usn: Long,
)
