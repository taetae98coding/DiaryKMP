package io.github.taetae98coding.diary.core.network.impl.dday.entity

import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DDayPullResponseRemoteEntity(
    @SerialName("ddayList") val ddayList: List<DDayPullRemoteEntity>,
)
