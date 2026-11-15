package io.github.taetae98coding.diary.domain.contact.share

import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import kotlinx.datetime.LocalDate

public data class ContactShareFile(
    val name: String,
    val vCard: String,
)

public fun ContactDetail.toContactShareFile(storedName: String): ContactShareFile {
    val sharedName = name.trim().ifEmpty { storedName }
    val vCard =
        buildList {
            add("BEGIN:VCARD")
            add("VERSION:3.0")
            add("N:${sharedName.escapeVCardText()}")
            add("FN:${sharedName.escapeVCardText()}")
            phoneNumberList
                .map { phoneNumber -> phoneNumber.number.trim() }
                .filter(String::isNotEmpty)
                .forEach { number -> add("TEL:${number.escapeVCardText()}") }
            birthday
                ?.takeIf { birthday -> birthday.calendar == ContactBirthdayCalendar.SOLAR }
                ?.let { birthday -> add("BDAY:${birthday.date.toVCardDate()}") }
            add("END:VCARD")
        }.joinToString(separator = LINE_BREAK, postfix = LINE_BREAK)

    return ContactShareFile(name = sharedName, vCard = vCard)
}

private fun LocalDate.toVCardDate(): String = LocalDate.Formats.ISO.format(this)

private fun String.escapeVCardText(): String =
    replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
        .replace(LINE_BREAK_REGEX, "\\\\n")

private val LINE_BREAK_REGEX = Regex("\r\n|\r|\n")

private const val LINE_BREAK = "\r\n"
