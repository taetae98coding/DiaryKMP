package io.github.taetae98coding.diary.feature.qr.ui.add.form

import io.github.taetae98coding.diary.domain.qr.content.QrFormat
import io.github.taetae98coding.diary.domain.qr.content.QrWifiSecurity

internal val qrFormatList: List<QrFormat> =
    listOf(
        QrFormat.TEXT,
        QrFormat.URL,
        QrFormat.CONTACT,
        QrFormat.WIFI,
        QrFormat.LOCATION,
        QrFormat.EMAIL,
        QrFormat.PHONE,
        QrFormat.SMS,
        QrFormat.EVENT,
    )

internal val qrWifiSecurityList: List<QrWifiSecurity> =
    listOf(
        QrWifiSecurity.WPA,
        QrWifiSecurity.WEP,
        QrWifiSecurity.NONE,
    )
