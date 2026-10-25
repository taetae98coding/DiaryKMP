package io.github.taetae98coding.diary.core.network.api.checklist.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ChecklistDetailRemoteEntity(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
)
