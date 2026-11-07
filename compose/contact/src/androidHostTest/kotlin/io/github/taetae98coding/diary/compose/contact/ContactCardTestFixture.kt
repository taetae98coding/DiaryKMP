package io.github.taetae98coding.diary.compose.contact

import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.core.testing.contact.contactDetail
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

internal fun testContact(
    name: String,
    phoneNumberList: List<String> = emptyList(),
    birthday: ContactBirthday? = null,
    birthYear: Int? = birthday?.date?.year,
    isFavorite: Boolean = false,
    nameQualifier: String = "",
): ContactListItem =
    ContactListItem(
        contact =
            Arb.contact().next().copy(
                detail =
                    Arb.contactDetail().next().copy(
                        name = name,
                        height = null,
                        footSize = null,
                        birthday = birthday,
                        birthYear = birthYear,
                        hometown = "",
                        howWeMet = "",
                        phoneNumberList = phoneNumberList.map { number -> ContactPhoneNumber(number = number) },
                    ),
                isFavorite = isFavorite,
                isDeleted = false,
            ),
        nameQualifier = nameQualifier,
    )
