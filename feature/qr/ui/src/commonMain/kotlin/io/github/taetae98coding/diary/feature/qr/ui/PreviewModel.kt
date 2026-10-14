package io.github.taetae98coding.diary.feature.qr.ui

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.qr.QrDetail
import io.github.taetae98coding.diary.domain.qr.content.QrAction
import io.github.taetae98coding.diary.domain.qr.content.QrWifiSecurity
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewQr(
    title: String,
    value: String = "https://example.com",
): Qr =
    Qr(
        id = Uuid.random(),
        detail =
            QrDetail(
                title = title,
                description = "",
                value = value,
            ),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal fun previewQrActionList(): List<QrAction> =
    listOf(
        QrAction.OpenUrl(url = "https://example.com"),
        QrAction.Call(phoneNumber = "010"),
        QrAction.SendEmail(to = "a@example.com", subject = "", body = ""),
        QrAction.SendSms(phoneNumber = "010", message = ""),
        QrAction.ShowLocation(coordinate = Coordinate(latitude = 37.5665, longitude = 126.978)),
        QrAction.AddContact(name = "홍길동", phoneNumber = "", email = "", company = "", address = "", website = "", vCard = ""),
        QrAction.AddEvent(title = "회의", schedule = null, location = "", description = "", calendarFile = ""),
        QrAction.JoinWifi(ssid = "home", security = QrWifiSecurity.WPA, password = "", isHidden = false),
    )
