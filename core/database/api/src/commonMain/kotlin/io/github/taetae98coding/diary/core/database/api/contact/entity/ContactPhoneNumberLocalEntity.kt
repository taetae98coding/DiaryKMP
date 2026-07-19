package io.github.taetae98coding.diary.core.database.api.contact.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ContactPhoneNumberLocalEntity(
    @SerialName("number") val number: String,
)
