package io.github.taetae98coding.diary.domain.share

import kotlinx.datetime.TimeZone

public fun ShareContent.toShareTarget(timeZone: TimeZone): ShareTarget =
    when (this) {
        is ShareContent.Text -> toShareTarget()
        is ShareContent.Contact -> readSharedContact(vCard)
        is ShareContent.Event -> readSharedEvent(iCalendar = iCalendar, timeZone = timeZone)
    }

private fun ShareContent.Text.toShareTarget(): ShareTarget {
    val trimmedText = text.trim()
    val trimmedSubject = subject.trim()

    return if (trimmedText.isSingleLink()) {
        ShareTarget.Web(title = trimmedSubject, url = trimmedText)
    } else if (trimmedSubject.isNotEmpty()) {
        ShareTarget.Memo(title = trimmedSubject, description = text)
    } else {
        val lineBreak = LINE_BREAK_REGEX.find(trimmedText)
        ShareTarget.Memo(
            title = trimmedText.substring(0, lineBreak?.range?.first ?: trimmedText.length).trim(),
            description = lineBreak?.let { trimmedText.substring(it.range.last + 1) }.orEmpty(),
        )
    }
}

private fun String.isSingleLink(): Boolean = LINK_PREFIX_LIST.any { prefix -> startsWith(prefix, ignoreCase = true) } && none(Char::isWhitespace)

private val LINK_PREFIX_LIST = listOf("http://", "https://")

private val LINE_BREAK_REGEX = Regex("\r\n|\r|\n")
