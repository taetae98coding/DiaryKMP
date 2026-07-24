package io.github.taetae98coding.diary.core.network.api.contact.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ContactPhoneNumberRemoteEntity(
    @SerialName("number") val number: String,
)
