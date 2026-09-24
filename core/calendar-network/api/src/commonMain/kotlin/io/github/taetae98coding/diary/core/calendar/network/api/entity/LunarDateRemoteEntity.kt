package io.github.taetae98coding.diary.core.calendar.network.api.entity

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class LunarDateRemoteEntity(
    @SerialName("solar") val solar: LocalDate,
    @SerialName("year") val year: Int,
    @SerialName("month") val month: Int,
    @SerialName("day") val day: Int,
    @SerialName("isLeapMonth") val isLeapMonth: Boolean,
)
