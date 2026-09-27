package io.github.taetae98coding.diary.domain.qr.content

public fun QrFormat.readContent(raw: String): QrContent? {
    if (!recognizes(raw)) return null

    return when (this) {
        QrFormat.TEXT -> QrContent.Text(text = raw)
        QrFormat.URL -> QrContent.Url(url = raw)
        QrFormat.CONTACT -> readContact(raw)
        QrFormat.WIFI -> readWifi(raw)
        QrFormat.LOCATION -> readLocation(raw)
        QrFormat.EMAIL -> readEmail(raw)
        QrFormat.PHONE -> QrContent.Phone(phoneNumber = raw.drop(TEL_PREFIX_LENGTH))
        QrFormat.SMS -> readSms(raw)
        QrFormat.EVENT -> readEvent(raw)
    }
}

private fun readContact(raw: String): QrContent.Contact {
    val lines = StructuredLines.parse(raw)

    fun value(name: String): String? = lines.find(name)?.property?.value

    val name =
        value("FN")?.unescapeStructuredText()
            ?: value("N")?.joinComponents()
            ?: ""

    return QrContent.Contact(
        name = name,
        phoneNumber = value("TEL")?.unescapeStructuredText().orEmpty(),
        email = value("EMAIL")?.unescapeStructuredText().orEmpty(),
        company =
            value("ORG")
                ?.splitUnescaped(';')
                ?.firstOrNull()
                ?.unescapeStructuredText()
                .orEmpty(),
        address = value("ADR")?.joinComponents().orEmpty(),
        website = value("URL")?.unescapeStructuredText().orEmpty(),
    )
}

private fun String.joinComponents(): String =
    splitUnescaped(';')
        .map(String::unescapeStructuredText)
        .filter(String::isNotEmpty)
        .joinToString(separator = " ")

private fun readWifi(raw: String): QrContent.Wifi {
    val entries = WifiEntries.parse(raw)
    val type = entries.value("T").orEmpty()

    return QrContent.Wifi(
        ssid = entries.value("S").orEmpty(),
        security =
            when {
                type.equals("WEP", ignoreCase = true) -> QrWifiSecurity.WEP
                type.isEmpty() || type.equals("nopass", ignoreCase = true) -> QrWifiSecurity.NONE
                else -> QrWifiSecurity.WPA
            },
        password = entries.value("P").orEmpty(),
        isHidden = entries.value("H").equals("true", ignoreCase = true),
    )
}

private fun readLocation(raw: String): QrContent.Location {
    val parts = GeoParts.parse(raw)

    return QrContent.Location(
        latitude = parts.coordinates.getOrNull(0).orEmpty(),
        longitude = parts.coordinates.getOrNull(1).orEmpty(),
    )
}

private fun readEmail(raw: String): QrContent.Email {
    val body = raw.drop(MAILTO_PREFIX_LENGTH)
    val query = QrQuery.parse(body.substringAfter('?', missingDelimiterValue = ""))

    return QrContent.Email(
        to = body.substringBefore('?'),
        subject = query.value("subject").orEmpty(),
        body = query.value("body").orEmpty(),
    )
}

private fun readSms(raw: String): QrContent.Sms =
    if (raw.startsWithIgnoreCase(SMSTO_PREFIX)) {
        val body = raw.drop(SMSTO_PREFIX.length)
        QrContent.Sms(phoneNumber = body.substringBefore(':'), message = body.substringAfter(':', missingDelimiterValue = ""))
    } else {
        val body = raw.drop(SMS_PREFIX.length)
        QrContent.Sms(
            phoneNumber = body.substringBefore('?'),
            message = QrQuery.parse(body.substringAfter('?', missingDelimiterValue = "")).value("body").orEmpty(),
        )
    }

internal const val TEL_PREFIX_LENGTH = 4
internal const val WIFI_PREFIX_LENGTH = 5
internal const val GEO_PREFIX_LENGTH = 4
internal const val MAILTO_PREFIX_LENGTH = 7
