package io.github.taetae98coding.diary.feature.contact.ui

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewContact(
    name: String,
    phoneNumberList: List<String>,
): Contact =
    Contact(
        id = Uuid.random(),
        detail =
            ContactDetail(
                name = name,
                description = "",
                height = null,
                footSize = null,
                birthday = null,
                phoneNumberList = phoneNumberList.map { number -> ContactPhoneNumber(number = number) },
            ),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )
