package io.github.taetae98coding.diary.core.network.impl.entity

import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ContactPushRequestRemoteEntity(
    @SerialName("contactList") val contactList: List<ContactRemoteEntity>,
)
