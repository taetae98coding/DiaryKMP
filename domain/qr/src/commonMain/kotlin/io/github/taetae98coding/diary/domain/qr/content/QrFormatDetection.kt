package io.github.taetae98coding.diary.domain.qr.content

public fun QrFormat.recognizes(raw: String): Boolean =
    when (this) {
        QrFormat.TEXT -> true
        QrFormat.URL -> raw.isUrl()
        QrFormat.CONTACT -> raw.startsWithIgnoreCase("BEGIN:VCARD")
        QrFormat.WIFI -> raw.startsWithIgnoreCase("WIFI:")
        QrFormat.LOCATION -> raw.startsWithIgnoreCase("geo:")
        QrFormat.EMAIL -> raw.startsWithIgnoreCase("mailto:")
        QrFormat.PHONE -> raw.startsWithIgnoreCase("tel:")
        QrFormat.SMS -> raw.startsWithIgnoreCase(SMSTO_PREFIX) || raw.startsWithIgnoreCase(SMS_PREFIX)
        QrFormat.EVENT -> raw.isEvent()
    }

private fun String.isUrl(): Boolean = (startsWithIgnoreCase("http://") || startsWithIgnoreCase("https://")) && none { char -> char == '\n' || char == '\r' }

private fun String.isEvent(): Boolean =
    (startsWithIgnoreCase("BEGIN:VEVENT") || startsWithIgnoreCase("BEGIN:VCALENDAR")) &&
        StructuredLines.parse(this).entries.any { entry -> entry.text.equals("BEGIN:VEVENT", ignoreCase = true) }

public fun detectQrFormat(raw: String): QrFormat = qrFormatDetectionOrder.firstOrNull { format -> format.recognizes(raw) } ?: QrFormat.TEXT

private val qrFormatDetectionOrder: List<QrFormat> =
    listOf(
        QrFormat.CONTACT,
        QrFormat.EVENT,
        QrFormat.WIFI,
        QrFormat.LOCATION,
        QrFormat.EMAIL,
        QrFormat.PHONE,
        QrFormat.SMS,
        QrFormat.URL,
    )

internal const val SMSTO_PREFIX = "SMSTO:"

internal const val SMS_PREFIX = "sms:"

internal fun String.startsWithIgnoreCase(prefix: String): Boolean = startsWith(prefix, ignoreCase = true)
