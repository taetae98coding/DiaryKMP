package io.github.taetae98coding.diary.core.network.api.routine.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class RoutineCycleRemoteEntity {
    @SerialName("FREQ=DAILY")
    DAILY,

    @SerialName("FREQ=DAILY;BYDAY=MO,TU,WE,TH,FR")
    WEEKDAYS,

    @SerialName("FREQ=DAILY;BYDAY=SA,SU")
    WEEKENDS,

    @SerialName("FREQ=WEEKLY;WKST=SU")
    WEEKLY,

    @SerialName("FREQ=MONTHLY")
    MONTHLY,
}
