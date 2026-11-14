package io.github.taetae98coding.diary.domain.share

import io.github.taetae98coding.diary.library.kotlin.text.StructuredLines
import io.github.taetae98coding.diary.library.kotlin.text.firstStructuredComponent
import io.github.taetae98coding.diary.library.kotlin.text.joinStructuredComponents
import io.github.taetae98coding.diary.library.kotlin.text.unescapeStructuredText
import kotlinx.datetime.LocalDate

internal fun readSharedContact(vCard: String): ShareTarget.Contact {
    val propertyList = StructuredLines.parse(vCard).firstBlockPropertyList(component = "VCARD")

    fun textList(name: String): List<String> = propertyList.valueList(name).map(String::unescapeStructuredText).filter(String::isNotBlank)

    val birthday =
        propertyList
            .first("BDAY")
            ?.value
            ?.trim()
            .orEmpty()
    val birthdayDate = birthday.toBirthdayDate()

    val descriptionLineList =
        buildList {
            addAll(textList("NOTE"))
            textList("EMAIL").forEach { email -> add("$EMAIL_LABEL$email") }
            propertyList.valueList("ORG").mapNotNull(String::companyOrNull).forEach { company -> add("$COMPANY_LABEL$company") }
            propertyList
                .valueList("ADR")
                .map(String::joinStructuredComponents)
                .filter(String::isNotBlank)
                .forEach { address -> add("$ADDRESS_LABEL$address") }
            textList("URL").forEach { website -> add("$WEBSITE_LABEL$website") }
            if (birthday.isNotEmpty() && birthdayDate == null) add("$BIRTHDAY_LABEL${birthday.toBirthdayText()}")
        }

    return ShareTarget.Contact(
        name =
            propertyList
                .first("FN")
                ?.value
                ?.unescapeStructuredText()
                ?.takeIf(String::isNotBlank)
                ?: propertyList
                    .first("N")
                    ?.value
                    ?.joinStructuredComponents()
                    .orEmpty(),
        description = descriptionLineList.joinToString(separator = "\n"),
        phoneNumberList = propertyList.valueList("TEL").map { number -> number.unescapeStructuredText().trim() }.filter(String::isNotEmpty),
        birthday = birthdayDate,
    )
}

private fun String.companyOrNull(): String? = firstStructuredComponent().takeIf(String::isNotBlank)

// vCard 생일은 날짜 뒤에 시각이 붙을 수 있으므로 날짜 부분만 읽는다.
private fun String.toBirthdayDate(): LocalDate? {
    val match = FULL_DATE_REGEX.matchEntire(substringBefore('T')) ?: return null
    val (year, month, day) = match.destructured

    return runCatching { LocalDate(year = year.toInt(), month = month.toInt(), day = day.toInt()) }.getOrNull()
}

private fun String.toBirthdayText(): String {
    val match = MONTH_DAY_REGEX.matchEntire(substringBefore('T')) ?: return this
    val (month, day) = match.destructured
    val isValid = runCatching { LocalDate(year = LEAP_YEAR, month = month.toInt(), day = day.toInt()) }.isSuccess

    return if (isValid) "${month.toInt()}월 ${day.toInt()}일" else this
}

private val FULL_DATE_REGEX = Regex("""(\d{4})-?(\d{2})-?(\d{2})""")
private val MONTH_DAY_REGEX = Regex("""--(\d{2})-?(\d{2})""")

// 연도 없는 생일이 2월 29일이어도 날짜로 맞는지 확인할 수 있도록 윤년을 쓴다.
private const val LEAP_YEAR = 2000

private const val EMAIL_LABEL = "이메일: "
private const val COMPANY_LABEL = "회사: "
private const val ADDRESS_LABEL = "주소: "
private const val WEBSITE_LABEL = "웹사이트: "
private const val BIRTHDAY_LABEL = "생일: "
