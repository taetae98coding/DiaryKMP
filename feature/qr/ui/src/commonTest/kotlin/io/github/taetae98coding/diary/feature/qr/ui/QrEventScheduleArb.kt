package io.github.taetae98coding.diary.feature.qr.ui

import io.github.taetae98coding.diary.domain.qr.content.QrEventSchedule
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice

internal fun Arb.Companion.qrEventSchedule(): Arb<QrEventSchedule> =
    Arb.choice(
        Arb.qrEventScheduleAllDay(),
        Arb.qrEventScheduleDateTime(),
    )

internal fun Arb.Companion.qrEventScheduleAllDay(): Arb<QrEventSchedule.AllDay> =
    arbitrary {
        QrEventSchedule.AllDay(
            start = Arb.kotlinxLocalDate().bind(),
            endInclusive = Arb.kotlinxLocalDate().bind(),
        )
    }

internal fun Arb.Companion.qrEventScheduleDateTime(): Arb<QrEventSchedule.DateTime> =
    arbitrary {
        QrEventSchedule.DateTime(
            start = Arb.secondPrecisionInstant().bind(),
            end = Arb.secondPrecisionInstant().bind(),
        )
    }
