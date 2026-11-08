package io.github.taetae98coding.diary.work.appaction.contact

import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.measure.Length
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument

internal const val CONTACT_HEIGHT = "heightCm"
internal const val CONTACT_FOOT_SIZE = "footSizeMm"
internal const val CONTACT_BIRTHDAY = "birthday"
internal const val CONTACT_BIRTHDAY_CALENDAR = "birthdayCalendar"
internal const val CONTACT_BIRTH_YEAR = "birthYear"

internal const val MAX_FOOT_SIZE_MM = 999
internal const val MAX_BIRTH_YEAR = 9999

private val heightRegex = Regex("""\d{1,3}(\.\d)?""")

public object AppActionContactBirthdayCalendarValue {
    public const val SOLAR: String = "solar"
    public const val LUNAR: String = "lunar"
}

internal val contactBirthdayCalendarList: List<ContactBirthdayCalendar> = listOf(ContactBirthdayCalendar.SOLAR, ContactBirthdayCalendar.LUNAR)

internal fun ContactBirthdayCalendar.toBirthdayCalendarValue(): String =
    when (this) {
        ContactBirthdayCalendar.SOLAR -> AppActionContactBirthdayCalendarValue.SOLAR
        ContactBirthdayCalendar.LUNAR -> AppActionContactBirthdayCalendarValue.LUNAR
    }

internal fun String?.toContactHeightArgumentOrNull(): Length? {
    val text = this ?: return null

    if (!heightRegex.matches(text)) {
        invalidArgument(message = "'$CONTACT_HEIGHT' must be centimeters with up to three digits and one decimal place, like \"172.5\".")
    }

    return text.toDouble().centimeter
}

internal fun Int?.toContactFootSizeArgumentOrNull(): Length? {
    val value = this ?: return null

    if (value !in 0..MAX_FOOT_SIZE_MM) invalidArgument(message = "'$CONTACT_FOOT_SIZE' must be between 0 and $MAX_FOOT_SIZE_MM.")

    return value.millimeter
}

internal fun Int?.toContactBirthYearArgumentOrNull(): Int? {
    val value = this ?: return null

    if (value !in 0..MAX_BIRTH_YEAR) invalidArgument(message = "'$CONTACT_BIRTH_YEAR' must be between 0 and $MAX_BIRTH_YEAR.")

    return value
}

internal fun String?.toContactBirthdayCalendarArgumentOrNull(): ContactBirthdayCalendar? {
    val value = this ?: return null

    return contactBirthdayCalendarList.firstOrNull { calendar -> calendar.toBirthdayCalendarValue() == value }
        ?: invalidArgument(message = "'$CONTACT_BIRTHDAY_CALENDAR' must be one of ${contactBirthdayCalendarList.joinToString { calendar -> calendar.toBirthdayCalendarValue() }}.")
}

internal fun List<String>.toContactPhoneNumberList(): List<ContactPhoneNumber> = map { number -> ContactPhoneNumber(number = number) }
