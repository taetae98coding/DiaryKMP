package io.github.taetae98coding.diary.feature.qr.ui.add.form

import io.github.taetae98coding.diary.domain.qr.content.QrFormat

internal enum class QrTextField {
    TEXT,
    URL,
    CONTACT_NAME,
    CONTACT_PHONE_NUMBER,
    CONTACT_EMAIL,
    CONTACT_COMPANY,
    CONTACT_ADDRESS,
    CONTACT_WEBSITE,
    WIFI_SSID,
    WIFI_PASSWORD,
    LATITUDE,
    LONGITUDE,
    EMAIL_TO,
    EMAIL_SUBJECT,
    EMAIL_BODY,
    PHONE_NUMBER,
    SMS_PHONE_NUMBER,
    SMS_MESSAGE,
    EVENT_TITLE,
    EVENT_LOCATION,
    EVENT_DESCRIPTION,
}

internal val QrFormat.firstTextField: QrTextField
    get() =
        when (this) {
            QrFormat.TEXT -> QrTextField.TEXT
            QrFormat.URL -> QrTextField.URL
            QrFormat.CONTACT -> QrTextField.CONTACT_NAME
            QrFormat.WIFI -> QrTextField.WIFI_SSID
            QrFormat.LOCATION -> QrTextField.LATITUDE
            QrFormat.EMAIL -> QrTextField.EMAIL_TO
            QrFormat.PHONE -> QrTextField.PHONE_NUMBER
            QrFormat.SMS -> QrTextField.SMS_PHONE_NUMBER
            QrFormat.EVENT -> QrTextField.EVENT_TITLE
        }
