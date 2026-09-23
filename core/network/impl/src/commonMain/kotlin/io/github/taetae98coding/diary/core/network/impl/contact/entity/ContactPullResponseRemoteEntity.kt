package io.github.taetae98coding.diary.core.network.impl.contact.entity

import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ContactPullResponseRemoteEntity(
    @SerialName("contactList") val contactList: List<ContactPullRemoteEntity>,
)
