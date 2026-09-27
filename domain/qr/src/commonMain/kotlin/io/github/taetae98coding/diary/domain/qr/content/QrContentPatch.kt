package io.github.taetae98coding.diary.domain.qr.content

public fun QrContent.patch(
    raw: String,
    previous: QrContent,
): String {
    if (!format.recognizes(raw) || previous.format != format) return encode()

    return when (this) {
        is QrContent.Text -> text
        is QrContent.Url -> url.trim()
        is QrContent.Contact -> patchContact(raw, previous as QrContent.Contact)
        is QrContent.Wifi -> patchWifi(raw, previous as QrContent.Wifi)
        is QrContent.Location -> patchLocation(raw, previous as QrContent.Location)
        is QrContent.Email -> patchEmail(raw, previous as QrContent.Email)
        is QrContent.Phone -> raw.take(TEL_PREFIX_LENGTH) + phoneNumber.trim()
        is QrContent.Sms -> patchSms(raw)
        is QrContent.Event -> patchEvent(raw, previous as QrContent.Event)
    }
}

private fun QrContent.Contact.patchContact(
    raw: String,
    previous: QrContent.Contact,
): String {
    val lines = StructuredLines.parse(raw)

    if (name != previous.name) {
        listOf("N", "FN").forEach { property -> lines.setVCardValue(property, name.trim().escapeVCardText(), keepWhenEmpty = true) }
    }
    if (phoneNumber != previous.phoneNumber) lines.setVCardValue("TEL", phoneNumber.trim().escapeVCardText())
    if (email != previous.email) lines.setVCardValue("EMAIL", email.trim().escapeVCardText())
    if (company != previous.company) lines.setVCardValue("ORG", company.trim().escapeVCardText())
    if (address != previous.address) {
        val value = address.trim()
        lines.setVCardValue("ADR", if (value.isEmpty()) "" else ";;${value.escapeVCardText()};;;;")
    }
    if (website != previous.website) lines.setVCardValue("URL", website.trim().escapeVCardText())

    return lines.toString()
}

private fun StructuredLines.setVCardValue(
    property: String,
    value: String,
    keepWhenEmpty: Boolean = false,
) {
    val entry = find(property)
    when {
        entry != null && value.isEmpty() && !keepWhenEmpty -> remove(entry)
        entry != null -> replace(entry, "${checkNotNull(entry.property).head}:$value")
        value.isNotEmpty() -> insert(indexOfLine("END:VCARD") ?: size, "$property:$value")
    }
}

private fun QrContent.Wifi.patchWifi(
    raw: String,
    previous: QrContent.Wifi,
): String {
    val entries = WifiEntries.parse(raw)

    fun set(
        key: String,
        value: String?,
    ) {
        val index = entries.items.indexOfFirst { (name, _) -> name.equals(key, ignoreCase = true) }
        when {
            value == null && index >= 0 -> entries.items.removeAt(index)
            value == null -> Unit
            index >= 0 -> entries.items[index] = entries.items[index].first to value
            else -> entries.items += key to value
        }
    }
    val passwordValue = password.takeIf { security != QrWifiSecurity.NONE && it.isNotEmpty() }?.escapeWifiText()

    if (ssid != previous.ssid) set("S", ssid.escapeWifiText())
    if (security != previous.security) {
        set("T", security.wifiValue)
        set("P", passwordValue)
    }
    if (password != previous.password && security != QrWifiSecurity.NONE) set("P", passwordValue)
    if (isHidden != previous.isHidden) set("H", "true".takeIf { isHidden })

    return entries.prefix + entries.items.joinToString(separator = "") { (key, value) -> "$key:$value;" } + ";"
}

private fun QrContent.Location.patchLocation(
    raw: String,
    previous: QrContent.Location,
): String {
    if (latitude == previous.latitude && longitude == previous.longitude) return raw

    val parts = GeoParts.parse(raw)
    val (latitudeText, longitudeText) = coordinateTexts()
    while (parts.coordinates.size < 2) parts.coordinates += ""
    parts.coordinates[0] = latitudeText
    parts.coordinates[1] = longitudeText

    return parts.toString()
}

private fun QrContent.Email.patchEmail(
    raw: String,
    previous: QrContent.Email,
): String {
    val prefix = raw.take(MAILTO_PREFIX_LENGTH)
    val body = raw.drop(MAILTO_PREFIX_LENGTH)
    val recipient = if (to != previous.to) to.trim() else body.substringBefore('?')
    val query = QrQuery.parse(body.substringAfter('?', missingDelimiterValue = ""))

    if (subject != previous.subject) query.set("subject", subject.trim())
    if (this.body != previous.body) query.set("body", this.body.trim().normalizeLineBreak())

    return if (query.isEmpty()) "$prefix$recipient" else "$prefix$recipient?$query"
}

private fun QrContent.Sms.patchSms(raw: String): String {
    val number = phoneNumber.trim()
    val text = message.trim()

    return if (raw.startsWithIgnoreCase(SMSTO_PREFIX)) {
        val prefix = raw.take(SMSTO_PREFIX.length)
        if (text.isEmpty()) "$prefix$number" else "$prefix$number:$text"
    } else {
        val prefix = raw.take(SMS_PREFIX.length)
        val query = QrQuery.parse(raw.drop(SMS_PREFIX.length).substringAfter('?', missingDelimiterValue = ""))
        query.set("body", text)
        if (query.isEmpty()) "$prefix$number" else "$prefix$number?$query"
    }
}

internal val QrWifiSecurity.wifiValue: String
    get() =
        when (this) {
            QrWifiSecurity.WPA -> "WPA"
            QrWifiSecurity.WEP -> "WEP"
            QrWifiSecurity.NONE -> "nopass"
        }
