package io.github.taetae98coding.diary.core.network.api.dday.entity

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class DDayDetailRemoteEntity(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("color") val color: Long,
    @SerialName("baseDate") val baseDate: LocalDate,
    @SerialName("countsBaseDay") val countsBaseDay: Boolean,
    @SerialName("repeats") val repeats: List<DDayRepeatRemoteEntity>,
    @SerialName("endDate") val endDate: LocalDate?,
    @SerialName("overflowPolicy") val overflowPolicy: DDayOverflowPolicyRemoteEntity,
)
