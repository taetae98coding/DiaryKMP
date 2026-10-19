package io.github.taetae98coding.diary.core.network.impl.checklistitem.entity

import io.github.taetae98coding.diary.core.network.api.checklistitem.entity.ChecklistItemRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChecklistItemPushRequestRemoteEntity(
    @SerialName("checklistItemList") val checklistItemList: List<ChecklistItemRemoteEntity>,
)
