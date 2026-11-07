package io.github.taetae98coding.diary.feature.contact.ui.detail

import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.measure.Length
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string

@Suppress("LongParameterList")
internal fun testContactDetail(
    name: String = "이름-${Arb.string().next()}",
    description: String = "설명-${Arb.string().next()}",
    height: Length? = null,
    footSize: Length? = null,
    birthday: ContactBirthday? = null,
    birthYear: Int? = birthday?.date?.year,
    hometown: String = "",
    howWeMet: String = "",
    phoneNumberList: List<String> = emptyList(),
): ContactDetail =
    ContactDetail(
        name = name,
        description = description,
        height = height,
        footSize = footSize,
        birthday = birthday,
        birthYear = birthYear,
        hometown = hometown,
        howWeMet = howWeMet,
        phoneNumberList = phoneNumberList.map { number -> ContactPhoneNumber(number = number) },
    )
