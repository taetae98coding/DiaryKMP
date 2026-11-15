package io.github.taetae98coding.diary.core.network.impl.routinerecord.entity

import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RoutineRecordPushRequestRemoteEntity(
    @SerialName("routineRecordList") val routineRecordList: List<RoutineRecordRemoteEntity>,
)
