package io.github.taetae98coding.diary.compose.contact

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import kotlin.time.Instant
import kotlin.uuid.Uuid

public fun previewContact(
    name: String,
    phoneNumberList: List<String>,
    birthday: ContactBirthday? = null,
    birthYear: Int? = birthday?.date?.year,
    hometown: String = "",
    howWeMet: String = "",
    isFavorite: Boolean = false,
    nameQualifier: String = "",
): ContactListItem =
    ContactListItem(
        contact =
            Contact(
                id = Uuid.random(),
                detail =
                    ContactDetail(
                        name = name,
                        description = "",
                        height = null,
                        footSize = null,
                        birthday = birthday,
                        birthYear = birthYear,
                        hometown = hometown,
                        howWeMet = howWeMet,
                        phoneNumberList = phoneNumberList.map { number -> ContactPhoneNumber(number = number) },
                    ),
                isFavorite = isFavorite,
                isDeleted = false,
                updatedAt = Instant.DISTANT_PAST,
                createdAt = Instant.DISTANT_PAST,
            ),
        nameQualifier = nameQualifier,
    )
