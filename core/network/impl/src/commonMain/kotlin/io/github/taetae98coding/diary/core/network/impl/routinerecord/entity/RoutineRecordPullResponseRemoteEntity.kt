package io.github.taetae98coding.diary.core.network.impl.routinerecord.entity

import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RoutineRecordPullResponseRemoteEntity(
    @SerialName("routineRecordList") val routineRecordList: List<RoutineRecordPullRemoteEntity>,
)
