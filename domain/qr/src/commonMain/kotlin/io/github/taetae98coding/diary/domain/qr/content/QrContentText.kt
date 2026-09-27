package io.github.taetae98coding.diary.domain.qr.content

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

internal const val LINE_BREAK = "\r\n"

internal fun String.escapeVCardText(): String = escapeStructuredText()

internal fun String.escapeICalendarText(): String = escapeStructuredText()

// vCard와 iCalendar는 글자 값 안의 같은 글자를 같은 방식으로 바꿔 담는다.
private fun String.escapeStructuredText(): String =
    normalizeLineBreak()
        .split(LINE_BREAK)
        .joinToString(separator = "\\n") { line ->
            buildString {
                line.forEach { char ->
                    if (char in STRUCTURED_TEXT_ESCAPED_CHARACTERS) append('\\')
                    append(char)
                }
            }
        }

private val STRUCTURED_TEXT_ESCAPED_CHARACTERS = setOf('\\', ',', ';')

internal fun String.escapeWifiText(): String =
    buildString {
        this@escapeWifiText.forEach { char ->
            if (char in WIFI_ESCAPED_CHARACTERS) append('\\')
            append(char)
        }
    }

private val WIFI_ESCAPED_CHARACTERS = setOf('\\', ';', ',', ':', '"')

internal fun String.normalizeLineBreak(): String = replace(LINE_BREAK_REGEX, LINE_BREAK)

private val LINE_BREAK_REGEX = Regex("\r\n|\r|\n")

internal fun String.percentEncode(): String =
    buildString {
        this@percentEncode.encodeToByteArray().forEach { byte ->
            val char = byte.toInt().toChar()
            if (byte >= 0 && char in PERCENT_UNRESERVED_CHARACTERS) {
                append(char)
            } else {
                append('%')
                append(HEX_DIGITS[(byte.toInt() shr HALF_BYTE_BITS) and HALF_BYTE_MASK])
                append(HEX_DIGITS[byte.toInt() and HALF_BYTE_MASK])
            }
        }
    }

private val PERCENT_UNRESERVED_CHARACTERS: Set<Char> = (('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf('-', '.', '_', '~')).toSet()

private const val HEX_DIGITS = "0123456789ABCDEF"

private const val HALF_BYTE_BITS = 4

private const val HALF_BYTE_MASK = 0x0F

internal fun LocalDate.toICalendarText(): String =
    buildString {
        append(year.toString().padStart(length = 4, padChar = '0'))
        append(month.number.toString().padStart(length = 2, padChar = '0'))
        append(day.toString().padStart(length = 2, padChar = '0'))
    }

internal fun LocalDateTime.toICalendarText(): String =
    buildString {
        append(date.toICalendarText())
        append('T')
        append(hour.toString().padStart(length = 2, padChar = '0'))
        append(minute.toString().padStart(length = 2, padChar = '0'))
        append("00")
    }
