package io.github.taetae98coding.diary.core.network.api.checklistitem.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ChecklistItemPullRemoteEntity(
    @SerialName("checklistItem") val checklistItem: ChecklistItemRemoteEntity,
    @SerialName("usn") val usn: Long,
)
