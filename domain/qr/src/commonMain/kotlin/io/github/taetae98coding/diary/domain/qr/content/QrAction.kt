package io.github.taetae98coding.diary.domain.qr.content

import io.github.taetae98coding.diary.core.model.location.Coordinate
import kotlinx.datetime.TimeZone

public sealed interface QrAction {
    public data class OpenUrl(
        val url: String,
    ) : QrAction

    public data class Call(
        val phoneNumber: String,
    ) : QrAction

    public data class SendEmail(
        val to: String,
        val subject: String,
        val body: String,
    ) : QrAction

    public data class SendSms(
        val phoneNumber: String,
        val message: String,
    ) : QrAction

    public data class ShowLocation(
        val coordinate: Coordinate,
    ) : QrAction

    public data class AddContact(
        val name: String,
        val phoneNumber: String,
        val email: String,
        val company: String,
        val address: String,
        val website: String,
        val vCard: String,
    ) : QrAction

    public data class AddEvent(
        val title: String,
        val schedule: QrEventSchedule?,
        val location: String,
        val description: String,
        val calendarFile: String,
    ) : QrAction

    public data class JoinWifi(
        val ssid: String,
        val security: QrWifiSecurity,
        val password: String,
        val isHidden: Boolean,
    ) : QrAction
}

public fun qrActionOf(
    raw: String,
    timeZone: TimeZone,
): QrAction? {
    val content = detectQrFormat(raw).readContent(raw) ?: return null

    return when (content) {
        is QrContent.Text -> null
        is QrContent.Url -> QrAction.OpenUrl(url = content.url.trim())
        is QrContent.Phone -> content.toAction()
        is QrContent.Email -> content.toAction()
        is QrContent.Sms -> content.toAction()
        is QrContent.Location -> content.coordinate?.let { coordinate -> QrAction.ShowLocation(coordinate = coordinate) }
        is QrContent.Contact -> content.toAction(raw = raw)
        is QrContent.Event -> content.toAction(raw = raw, timeZone = timeZone)
        is QrContent.Wifi -> content.toAction()
    }
}

private fun QrContent.Phone.toAction(): QrAction.Call? = phoneNumber.trim().ifEmpty { null }?.let { number -> QrAction.Call(phoneNumber = number) }

private fun QrContent.Email.toAction(): QrAction.SendEmail? =
    QrAction
        .SendEmail(to = to.trim(), subject = subject.trim(), body = body.trim())
        .takeIf { action -> listOf(action.to, action.subject, action.body).any(String::isNotEmpty) }

private fun QrContent.Sms.toAction(): QrAction.SendSms? = phoneNumber.trim().ifEmpty { null }?.let { number -> QrAction.SendSms(phoneNumber = number, message = message.trim()) }

private fun QrContent.Contact.toAction(raw: String): QrAction.AddContact? =
    QrAction
        .AddContact(
            name = name.trim(),
            phoneNumber = phoneNumber.trim(),
            email = email.trim(),
            company = company.trim(),
            address = address.trim(),
            website = website.trim(),
            vCard = raw,
        ).takeIf { action ->
            listOf(action.name, action.phoneNumber, action.email, action.company, action.address, action.website).any(String::isNotEmpty)
        }

private fun QrContent.Event.toAction(
    raw: String,
    timeZone: TimeZone,
): QrAction.AddEvent? =
    QrAction
        .AddEvent(
            title = title.trim(),
            schedule = period?.let { period -> readEventSchedule(raw = raw, period = period, deviceTimeZone = timeZone) },
            location = location.trim(),
            description = description.trim(),
            calendarFile = raw.toCalendarFile(),
        ).takeIf { action ->
            action.schedule != null || listOf(action.title, action.location, action.description).any(String::isNotEmpty)
        }

private fun QrContent.Wifi.toAction(): QrAction.JoinWifi? =
    QrAction
        .JoinWifi(ssid = ssid, security = security, password = password, isHidden = isHidden)
        .takeIf { ssid.isNotBlank() }

private fun String.toCalendarFile(): String {
    if (startsWithIgnoreCase(CALENDAR_BEGIN)) return this

    return listOf(CALENDAR_BEGIN, CALENDAR_VERSION, trimEnd('\r', '\n'), CALENDAR_END).joinToString(separator = CALENDAR_LINE_BREAK)
}

private const val CALENDAR_BEGIN = "BEGIN:VCALENDAR"
private const val CALENDAR_VERSION = "VERSION:2.0"
private const val CALENDAR_END = "END:VCALENDAR"

// iCalendar는 줄 끝을 CR LF로 정한다.
private const val CALENDAR_LINE_BREAK = "\r\n"
