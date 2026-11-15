package io.github.taetae98coding.diary.core.network.api.routinerecord.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class RoutineRecordPullRemoteEntity(
    @SerialName("routineRecord") val routineRecord: RoutineRecordRemoteEntity,
    @SerialName("usn") val usn: Long,
)
