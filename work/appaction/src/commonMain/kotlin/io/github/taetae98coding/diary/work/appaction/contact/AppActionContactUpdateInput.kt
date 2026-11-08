package io.github.taetae98coding.diary.work.appaction.contact

import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.measure.Length
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument
import io.github.taetae98coding.diary.work.appaction.core.toLocalDateArgumentOrNull

public data class AppActionContactUpdateInput(
    val contactId: String,
    val name: String? = null,
    val description: String? = null,
    val heightCm: String? = null,
    val clearHeight: Boolean? = null,
    val footSizeMm: Int? = null,
    val clearFootSize: Boolean? = null,
    val birthday: String? = null,
    val birthdayCalendar: String? = null,
    val clearBirthday: Boolean? = null,
    val birthYear: Int? = null,
    val clearBirthYear: Boolean? = null,
    val hometown: String? = null,
    val howWeMet: String? = null,
    val phoneNumbers: List<String>? = null,
)

internal fun AppActionContactUpdateInput.toContactDetail(current: ContactDetail): ContactDetail {
    val updatedBirthday = updatedBirthday(current = current.birthday)

    return ContactDetail(
        name = name ?: current.name,
        description = description ?: current.description,
        height = updatedHeight(current = current.height),
        footSize = updatedFootSize(current = current.footSize),
        birthday = updatedBirthday,
        birthYear = updatedBirthYear(current = current, birthday = updatedBirthday),
        hometown = hometown ?: current.hometown,
        howWeMet = howWeMet ?: current.howWeMet,
        phoneNumberList = phoneNumbers?.toContactPhoneNumberList() ?: current.phoneNumberList,
    )
}

private fun AppActionContactUpdateInput.updatedHeight(current: Length?): Length? =
    when {
        clearHeight == true -> null
        heightCm != null -> heightCm.toContactHeightArgumentOrNull()
        else -> current
    }

private fun AppActionContactUpdateInput.updatedFootSize(current: Length?): Length? =
    when {
        clearFootSize == true -> null
        footSizeMm != null -> footSizeMm.toContactFootSizeArgumentOrNull()
        else -> current
    }

private fun AppActionContactUpdateInput.updatedBirthYear(
    current: ContactDetail,
    birthday: ContactBirthday?,
): Int? =
    when {
        clearBirthYear == true -> null
        birthYear != null -> birthYear.toContactBirthYearArgumentOrNull()
        current.birthday != null && birthday == null -> null
        else -> current.birthYear
    }

private fun AppActionContactUpdateInput.updatedBirthday(current: ContactBirthday?): ContactBirthday? {
    if (clearBirthday == true) return null

    val date = birthday.toLocalDateArgumentOrNull(name = CONTACT_BIRTHDAY)
    val calendar = birthdayCalendar.toContactBirthdayCalendarArgumentOrNull()

    return if (date == null && calendar == null) {
        current
    } else {
        ContactBirthday(
            date = date ?: current?.date ?: invalidArgument(message = "'$CONTACT_BIRTHDAY_CALENDAR' needs '$CONTACT_BIRTHDAY' when the contact has no birthday."),
            calendar = calendar ?: current?.calendar ?: ContactBirthdayCalendar.SOLAR,
        )
    }
}
