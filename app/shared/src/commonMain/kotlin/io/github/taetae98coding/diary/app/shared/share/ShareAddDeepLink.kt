package io.github.taetae98coding.diary.app.shared.share

import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.domain.share.ShareContent
import io.github.taetae98coding.diary.domain.share.SharePeriod
import io.github.taetae98coding.diary.domain.share.ShareTarget
import io.github.taetae98coding.diary.domain.share.toShareTarget
import io.github.taetae98coding.diary.library.kotlin.text.decodeUriComponent
import io.github.taetae98coding.diary.library.kotlin.text.encodeUriComponent
import kotlinx.datetime.TimeZone

public fun shareAddDeepLinkOf(
    content: ShareContent,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String =
    when (val target = content.toShareTarget(timeZone = timeZone)) {
        is ShareTarget.Web -> addressOf(ScreenDeepLink.WEB_ADD, "initialTitle" to target.title, "initialUrl" to target.url)

        is ShareTarget.Memo -> target.toAddress()

        is ShareTarget.Contact ->
            addressOf(
                ScreenDeepLink.CONTACT_ADD,
                "initialName" to target.name,
                "initialDescription" to target.description,
                "initialBirthday" to target.birthday?.toString().orEmpty(),
                *target.phoneNumberList.map { number -> "initialPhoneNumberList" to number }.toTypedArray(),
            )
    }

public fun shareContentOf(deepLink: String): ShareContent? {
    val queryStart = deepLink.indexOf('?')
    val head = if (queryStart < 0) deepLink else deepLink.substring(0, queryStart)
    if (!head.equals("diary://${ScreenDeepLink.SHARE_HOST}", ignoreCase = true)) return null

    val query =
        deepLink
            .substring(startIndex = if (queryStart < 0) deepLink.length else queryStart + 1)
            .split('&')
            .filter(String::isNotEmpty)
            .associate { pair -> pair.substringBefore('=').decodeUriComponent() to pair.substringAfter('=', missingDelimiterValue = "").decodeUriComponent() }
    val text = query[ScreenDeepLink.SHARE_TEXT].orEmpty()

    return when (query[ScreenDeepLink.SHARE_KIND]) {
        ScreenDeepLink.SHARE_KIND_TEXT -> ShareContent.Text(text = text, subject = query[ScreenDeepLink.SHARE_SUBJECT].orEmpty())
        ScreenDeepLink.SHARE_KIND_CONTACT -> ShareContent.Contact(vCard = text)
        ScreenDeepLink.SHARE_KIND_EVENT -> ShareContent.Event(iCalendar = text)
        else -> null
    }
}

private fun ShareTarget.Memo.toAddress(): String {
    val allDay = period as? SharePeriod.AllDay
    val dateTime = period as? SharePeriod.DateTime

    return addressOf(
        ScreenDeepLink.MEMO_ADD,
        "initialTitle" to title,
        "initialDescription" to description,
        "initialStartDate" to allDay?.start?.toString().orEmpty(),
        "initialEndDate" to allDay?.endInclusive?.toString().orEmpty(),
        "initialStartDateTime" to dateTime?.start?.toString().orEmpty(),
        "initialEndDateTime" to dateTime?.endInclusive?.toString().orEmpty(),
    )
}

// 주소 형태에서 쿼리 앞부분만 쓰고, 값이 있는 이름만 담는다.
private fun addressOf(
    pattern: String,
    vararg queryList: Pair<String, String>,
): String {
    val query =
        queryList
            .filter { (_, value) -> value.isNotEmpty() }
            .joinToString(separator = "&") { (name, value) -> "$name=${value.encodeUriComponent()}" }

    return pattern.substringBefore('?') + if (query.isEmpty()) "" else "?$query"
}
