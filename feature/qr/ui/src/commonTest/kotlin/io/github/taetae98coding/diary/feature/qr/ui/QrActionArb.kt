package io.github.taetae98coding.diary.feature.qr.ui

import io.github.taetae98coding.diary.core.testing.location.coordinate
import io.github.taetae98coding.diary.domain.qr.content.QrAction
import io.github.taetae98coding.diary.domain.qr.content.QrWifiSecurity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.qrActionAddContact(): Arb<QrAction.AddContact> =
    arbitrary {
        QrAction.AddContact(
            name = Arb.string().bind(),
            phoneNumber = Arb.string().bind(),
            email = Arb.string().bind(),
            company = Arb.string().bind(),
            address = Arb.string().bind(),
            website = Arb.string().bind(),
            vCard = Arb.string().bind(),
        )
    }

internal fun Arb.Companion.qrActionAddEvent(): Arb<QrAction.AddEvent> =
    arbitrary {
        QrAction.AddEvent(
            title = Arb.string().bind(),
            schedule = Arb.qrEventSchedule().orNull().bind(),
            location = Arb.string().bind(),
            description = Arb.string().bind(),
            calendarFile = Arb.string().bind(),
        )
    }

internal fun Arb.Companion.qrActionCall(): Arb<QrAction.Call> =
    arbitrary {
        QrAction.Call(
            phoneNumber = Arb.string().bind(),
        )
    }

internal fun Arb.Companion.qrActionJoinWifi(): Arb<QrAction.JoinWifi> =
    arbitrary {
        QrAction.JoinWifi(
            ssid = Arb.string().bind(),
            security = Arb.enum<QrWifiSecurity>().bind(),
            password = Arb.string().bind(),
            isHidden = Arb.boolean().bind(),
        )
    }

internal fun Arb.Companion.qrActionOpenUrl(): Arb<QrAction.OpenUrl> =
    arbitrary {
        QrAction.OpenUrl(
            url = Arb.string().bind(),
        )
    }

internal fun Arb.Companion.qrActionSendEmail(): Arb<QrAction.SendEmail> =
    arbitrary {
        QrAction.SendEmail(
            to = Arb.string().bind(),
            subject = Arb.string().bind(),
            body = Arb.string().bind(),
        )
    }

internal fun Arb.Companion.qrActionSendSms(): Arb<QrAction.SendSms> =
    arbitrary {
        QrAction.SendSms(
            phoneNumber = Arb.string().bind(),
            message = Arb.string().bind(),
        )
    }

internal fun Arb.Companion.qrActionShowLocation(): Arb<QrAction.ShowLocation> =
    arbitrary {
        QrAction.ShowLocation(
            coordinate = Arb.coordinate().bind(),
        )
    }
