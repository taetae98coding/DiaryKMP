package io.github.taetae98coding.diary.core.network.api.routine.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class RoutinePullRemoteEntity(
    @SerialName("routine") val routine: RoutineRemoteEntity,
    @SerialName("usn") val usn: Long,
)
