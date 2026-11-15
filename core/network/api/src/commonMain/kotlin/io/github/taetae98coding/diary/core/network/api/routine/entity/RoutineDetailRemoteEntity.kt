package io.github.taetae98coding.diary.core.network.api.routine.entity

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class RoutineDetailRemoteEntity(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("color") val color: Long,
    @SerialName("startDate") val startDate: LocalDate,
    @SerialName("cycle") val cycle: RoutineCycleRemoteEntity,
    @SerialName("unit") val unit: String,
    @SerialName("goalAmount") val goalAmount: Int?,
    @SerialName("amountPerRecord") val amountPerRecord: Int,
)
