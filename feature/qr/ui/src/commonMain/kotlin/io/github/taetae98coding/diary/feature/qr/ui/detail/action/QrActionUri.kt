package io.github.taetae98coding.diary.feature.qr.ui.detail.action

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.toPlaceCoordinateText
import io.github.taetae98coding.diary.domain.qr.content.QrAction
import io.github.taetae98coding.diary.library.kotlin.text.encodeUriComponent

private const val GOOGLE_MAP_WEB_SEARCH_URI = "https://www.google.com/maps/search/?api=1&query="

// Google Maps URLs는 좌표를 구분하는 쉼표를 인코딩해 넘기도록 정하고 있다.
private const val ENCODED_COMMA = "%2C"

internal fun QrAction.Call.telUri(): String = "tel:${phoneNumber.toUriPhoneNumber()}"

// RFC 6068은 메일 본문의 줄바꿈을 CR LF로 담도록 정한다.
internal fun QrAction.SendEmail.mailtoUri(): String {
    val query =
        listOf("subject" to subject, "body" to body.replace("\r\n", "\n").replace("\n", "\r\n"))
            .filter { (_, value) -> value.isNotEmpty() }
            .joinToString(separator = "&") { (name, value) -> "$name=${value.encodeUriComponent()}" }

    return if (query.isEmpty()) "mailto:$to" else "mailto:$to?$query"
}

internal fun QrAction.SendSms.smsUri(bodySeparator: Char): String {
    val uri = "sms:${phoneNumber.toUriPhoneNumber()}"

    return if (message.isEmpty()) uri else "$uri${bodySeparator}body=${message.encodeUriComponent()}"
}

internal fun Coordinate.googleMapWebUri(): String = "$GOOGLE_MAP_WEB_SEARCH_URI$latitudeText$ENCODED_COMMA$longitudeText"

internal val Coordinate.latitudeText: String
    get() = latitude.toPlaceCoordinateText()

internal val Coordinate.longitudeText: String
    get() = longitude.toPlaceCoordinateText()

// 전화번호의 '#'은 주소의 조각 시작으로 읽히므로 인코딩하고, 공백은 번호의 일부가 아니므로 뺀다.
private fun String.toUriPhoneNumber(): String = filterNot(Char::isWhitespace).replace("#", "%23")
