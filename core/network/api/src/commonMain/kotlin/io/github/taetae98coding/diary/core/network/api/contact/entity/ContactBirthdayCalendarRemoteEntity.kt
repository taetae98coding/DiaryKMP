package io.github.taetae98coding.diary.core.network.api.contact.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ContactBirthdayCalendarRemoteEntity {
    @SerialName("solar")
    SOLAR,

    @SerialName("lunar")
    LUNAR,
}
