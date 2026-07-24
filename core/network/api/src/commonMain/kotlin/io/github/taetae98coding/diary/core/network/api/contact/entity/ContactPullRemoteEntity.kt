package io.github.taetae98coding.diary.core.network.api.contact.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ContactPullRemoteEntity(
    @SerialName("contact") val contact: ContactRemoteEntity,
    @SerialName("usn") val usn: Long,
)
