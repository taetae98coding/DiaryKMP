package io.github.taetae98coding.diary.core.network.api.dday.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class DDayOverflowPolicyRemoteEntity {
    @SerialName("clamp_to_month_end")
    CLAMP_TO_MONTH_END,

    @SerialName("skip")
    SKIP,

    @SerialName("next_month_first_day")
    NEXT_MONTH_FIRST_DAY,
}
