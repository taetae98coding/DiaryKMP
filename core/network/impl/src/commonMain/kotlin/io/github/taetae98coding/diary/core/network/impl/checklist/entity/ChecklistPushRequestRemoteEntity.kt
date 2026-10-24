package io.github.taetae98coding.diary.core.network.impl.checklist.entity

import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChecklistPushRequestRemoteEntity(
    @SerialName("checklistList") val checklistList: List<ChecklistRemoteEntity>,
)
