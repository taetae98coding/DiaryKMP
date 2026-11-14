package io.github.taetae98coding.diary.feature.tag.ui

import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.core.testing.contact.contactDetail
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.dday.ddayDetail
import io.github.taetae98coding.diary.core.testing.playlist.music
import io.github.taetae98coding.diary.core.testing.playlist.musicDetail
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate

internal fun tagContact(name: String): ContactListItem =
    ContactListItem(
        contact =
            Arb
                .contact()
                .next()
                .copy(
                    detail = Arb.contactDetail().next().copy(name = name, phoneNumberList = emptyList(), birthday = null, birthYear = null),
                    isFavorite = false,
                    isDeleted = false,
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                ),
        nameQualifier = "",
    )

internal fun tagMusic(title: String): Music =
    Arb
        .music()
        .next()
        .copy(
            detail = Arb.musicDetail().next().copy(title = title, link = ""),
            isDeleted = false,
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )

internal fun tagQr(title: String): Qr = Arb.qr(isDeleted = false, detail = Arb.qrDetail().next().copy(title = title)).next()

internal fun tagDDay(
    title: String,
    baseDate: LocalDate? = null,
    countsBaseDay: Boolean? = null,
): DDay {
    val detail = Arb.ddayDetail().next()

    return Arb
        .dday()
        .next()
        .copy(
            detail =
                detail.copy(
                    title = title,
                    baseDate = baseDate ?: detail.baseDate,
                    countsBaseDay = countsBaseDay ?: detail.countsBaseDay,
                    repeats = if (baseDate == null) detail.repeats else emptySet(),
                    endDate = if (baseDate == null) detail.endDate else null,
                ),
            isDeleted = false,
        )
}

internal fun tagRoutine(
    title: String,
    startDate: LocalDate = LocalDate(year = 2000, month = 1, day = 1),
    isFinished: Boolean = false,
    cycle: RoutineCycle = RoutineCycle.DAILY,
    unit: String = "회",
    goalAmount: Int? = null,
    cycleAmount: Long = 0,
): RoutineSummary =
    RoutineSummary(
        routine =
            Arb
                .routine()
                .next()
                .copy(
                    detail =
                        Arb.routineDetail().next().copy(
                            title = title,
                            startDate = startDate,
                            cycle = cycle,
                            unit = unit,
                            goalAmount = goalAmount,
                        ),
                    isFinished = isFinished,
                    isDeleted = false,
                ),
        cycleAmount = cycleAmount,
    )
