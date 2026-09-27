package io.github.taetae98coding.diary.domain.qr.content

internal fun QrContent.Contact.encodeContact(): String =
    buildList {
        add("BEGIN:VCARD")
        add("VERSION:3.0")
        add("N:${name.trim().escapeVCardText()}")
        add("FN:${name.trim().escapeVCardText()}")
        addIfPresent(prefix = "ORG:", value = company)
        addIfPresent(prefix = "TEL:", value = phoneNumber)
        addIfPresent(prefix = "EMAIL:", value = email)
        address.trim().takeIf(String::isNotEmpty)?.let { value -> add("ADR:;;${value.escapeVCardText()};;;;") }
        addIfPresent(prefix = "URL:", value = website)
        add("END:VCARD")
    }.joinToString(separator = LINE_BREAK)

internal fun QrContent.Wifi.encodeWifi(): String =
    buildString {
        append("WIFI:T:")
        append(security.wifiValue)
        append(";S:")
        append(ssid.escapeWifiText())
        append(';')
        if (security != QrWifiSecurity.NONE && password.isNotEmpty()) {
            append("P:")
            append(password.escapeWifiText())
            append(';')
        }
        if (isHidden) {
            append("H:true;")
        }
        append(';')
    }

internal fun QrContent.Email.encodeEmail(): String {
    val query =
        listOfNotNull(
            subject.trim().takeIf(String::isNotEmpty)?.let { value -> "subject=${value.percentEncode()}" },
            body.trim().takeIf(String::isNotEmpty)?.let { value -> "body=${value.normalizeLineBreak().percentEncode()}" },
        )

    return buildString {
        append("mailto:")
        append(to.trim())
        if (query.isNotEmpty()) {
            append('?')
            append(query.joinToString(separator = "&"))
        }
    }
}

internal fun QrContent.Sms.encodeSms(): String {
    val message = message.trim()

    return if (message.isEmpty()) {
        "SMSTO:${phoneNumber.trim()}"
    } else {
        "SMSTO:${phoneNumber.trim()}:$message"
    }
}

internal fun QrContent.Event.encodeEvent(): String =
    buildList {
        add("BEGIN:VEVENT")
        add("SUMMARY:${title.trim().escapeICalendarText()}")
        period?.iCalendarLines()?.forEach(::add)
        location.trim().takeIf(String::isNotEmpty)?.let { value -> add("LOCATION:${value.escapeICalendarText()}") }
        description.trim().takeIf(String::isNotEmpty)?.let { value -> add("DESCRIPTION:${value.escapeICalendarText()}") }
        add("END:VEVENT")
    }.joinToString(separator = LINE_BREAK)

private fun MutableList<String>.addIfPresent(
    prefix: String,
    value: String,
) {
    value.trim().takeIf(String::isNotEmpty)?.let { trimmed -> add("$prefix${trimmed.escapeVCardText()}") }
}
