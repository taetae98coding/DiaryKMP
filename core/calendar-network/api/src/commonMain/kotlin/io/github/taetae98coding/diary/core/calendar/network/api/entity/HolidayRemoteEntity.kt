package io.github.taetae98coding.diary.core.calendar.network.api.entity

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class HolidayRemoteEntity(
    @SerialName("name") val name: String,
    @SerialName("isHoliday") val isHoliday: Boolean,
    @SerialName("start") val start: LocalDate,
    @SerialName("endInclusive") val endInclusive: LocalDate,
)
