package io.github.taetae98coding.diary.core.network.api.routinerecord.entity

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
public data class RoutineRecordRemoteEntity(
    @SerialName("id") val id: Uuid,
    @SerialName("routineId") val routineId: Uuid,
    @SerialName("date") val date: LocalDate,
    @SerialName("amount") val amount: Int,
    @SerialName("isDeleted") val isDeleted: Boolean,
    @SerialName("updatedAt") val updatedAt: Instant,
    @SerialName("createdAt") val createdAt: Instant,
)
