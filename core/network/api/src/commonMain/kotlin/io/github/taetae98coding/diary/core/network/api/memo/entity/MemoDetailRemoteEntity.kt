package io.github.taetae98coding.diary.core.network.api.memo.entity

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MemoDetailRemoteEntity(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("color") val color: Long,
    @SerialName("isAllDay") val isAllDay: Boolean?,
    @SerialName("start") val start: LocalDateTime?,
    @SerialName("endInclusive") val endInclusive: LocalDateTime?,
)
