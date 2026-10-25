package io.github.taetae98coding.diary.core.network.impl.checklist.entity

import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChecklistPullResponseRemoteEntity(
    @SerialName("checklistList") val checklistList: List<ChecklistPullRemoteEntity>,
)
