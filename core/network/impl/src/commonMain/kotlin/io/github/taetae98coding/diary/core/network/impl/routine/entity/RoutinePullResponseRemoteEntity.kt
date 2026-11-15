package io.github.taetae98coding.diary.core.network.impl.routine.entity

import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutinePullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RoutinePullResponseRemoteEntity(
    @SerialName("routineList") val routineList: List<RoutinePullRemoteEntity>,
)
