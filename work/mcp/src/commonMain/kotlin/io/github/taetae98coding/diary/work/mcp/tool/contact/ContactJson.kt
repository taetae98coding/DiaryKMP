package io.github.taetae98coding.diary.work.mcp.tool.contact

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.measure.Length
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.json.put
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal const val CONTACT_HEIGHT = "heightCm"
internal const val CONTACT_FOOT_SIZE = "footSizeMm"
internal const val CONTACT_BIRTHDAY = "birthday"
internal const val CONTACT_BIRTHDAY_CALENDAR = "birthdayCalendar"
internal const val CONTACT_BIRTH_YEAR = "birthYear"
internal const val CONTACT_PHONE_NUMBERS = "phoneNumbers"

private const val MAX_FOOT_SIZE_MM = 999
private const val MAX_BIRTH_YEAR = 9999

private val heightRegex = Regex("""\d{1,3}(\.\d)?""")

internal val contactBirthdayCalendarList: List<ContactBirthdayCalendar> = listOf(ContactBirthdayCalendar.SOLAR, ContactBirthdayCalendar.LUNAR)

private fun ContactBirthdayCalendar.toArgument(): String =
    when (this) {
        ContactBirthdayCalendar.SOLAR -> "solar"
        ContactBirthdayCalendar.LUNAR -> "lunar"
    }

internal val contactHeightParameter: McpToolParameter =
    McpToolParameter(
        name = CONTACT_HEIGHT,
        type = McpToolParameterType.STRING,
        description = "Height in centimeters as a string with up to three digits and at most one decimal place, like \"172.5\".",
    )

internal val contactFootSizeParameter: McpToolParameter =
    McpToolParameter(
        name = CONTACT_FOOT_SIZE,
        type = McpToolParameterType.INTEGER,
        description = "Shoe size in whole millimeters from 0 to $MAX_FOOT_SIZE_MM, like 270.",
    )

internal val contactBirthdayParameter: McpToolParameter =
    McpToolParameter(
        name = CONTACT_BIRTHDAY,
        type = McpToolParameterType.STRING,
        description =
            "Birthday as a date like 1990-12-20. The date is read in the calendar of '$CONTACT_BIRTHDAY_CALENDAR', " +
                "so a lunar birthday is the lunar year, month and day as is. It also sets the birth year to its year.",
    )

internal val contactBirthdayCalendarParameter: McpToolParameter =
    McpToolParameter(
        name = CONTACT_BIRTHDAY_CALENDAR,
        type = McpToolParameterType.STRING,
        description = "Calendar of the birthday. Only valid with a birthday.",
        allowedValueList = contactBirthdayCalendarList.map { calendar -> calendar.toArgument() },
    )

internal val contactBirthYearParameter: McpToolParameter =
    McpToolParameter(
        name = CONTACT_BIRTH_YEAR,
        type = McpToolParameterType.INTEGER,
        description = "Birth year from 0 to $MAX_BIRTH_YEAR for when only the year is known. Ignored while the contact has a birthday, whose year is used instead.",
    )

internal val contactPhoneNumbersParameter: McpToolParameter =
    McpToolParameter(
        name = CONTACT_PHONE_NUMBERS,
        type = McpToolParameterType.STRING_LIST,
        description = "Phone numbers in order. Each number must not be blank. The format is not checked.",
    )

internal fun McpToolArguments.contactHeightOrNull(): Length? {
    val text = stringOrNull(CONTACT_HEIGHT) ?: return null

    if (!heightRegex.matches(text)) {
        throw McpToolArgumentException(message = "'$CONTACT_HEIGHT' must be centimeters with up to three digits and one decimal place, like \"172.5\".")
    }

    return text.toDouble().centimeter
}

internal fun McpToolArguments.contactFootSizeOrNull(): Length? {
    val value = intOrNull(CONTACT_FOOT_SIZE) ?: return null

    if (value !in 0..MAX_FOOT_SIZE_MM) throw McpToolArgumentException(message = "'$CONTACT_FOOT_SIZE' must be between 0 and $MAX_FOOT_SIZE_MM.")

    return value.millimeter
}

internal fun McpToolArguments.contactBirthYearOrNull(): Int? {
    val value = intOrNull(CONTACT_BIRTH_YEAR) ?: return null

    if (value !in 0..MAX_BIRTH_YEAR) throw McpToolArgumentException(message = "'$CONTACT_BIRTH_YEAR' must be between 0 and $MAX_BIRTH_YEAR.")

    return value
}

internal fun McpToolArguments.contactBirthdayCalendarOrNull(): ContactBirthdayCalendar? {
    val argument = stringOrNull(CONTACT_BIRTHDAY_CALENDAR) ?: return null

    return contactBirthdayCalendarList.firstOrNull { calendar -> calendar.toArgument() == argument }
        ?: throw McpToolArgumentException(
            message = "'$CONTACT_BIRTHDAY_CALENDAR' must be one of ${contactBirthdayCalendarList.joinToString { calendar -> calendar.toArgument() }}.",
        )
}

internal fun ContactBirthday.toJson(): JsonObject =
    buildJsonObject {
        put("date", date.toString())
        put("calendar", calendar.toArgument())
    }

internal fun Contact.toDetailJson(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("name", detail.name)
        put("description", detail.description)
        put(CONTACT_HEIGHT, detail.height?.inCentimeter)
        put(CONTACT_FOOT_SIZE, detail.footSize?.inWholeMillimeter)
        put(CONTACT_BIRTHDAY, detail.birthday?.toJson() ?: JsonNull)
        put(CONTACT_BIRTH_YEAR, detail.birthYear)
        put("hometown", detail.hometown)
        put("howWeMet", detail.howWeMet)
        put(CONTACT_PHONE_NUMBERS, JsonArray(detail.phoneNumberList.map { phoneNumber -> JsonPrimitive(phoneNumber.number) }))
        put("isFavorite", isFavorite)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }
