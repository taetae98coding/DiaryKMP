package io.github.taetae98coding.diary.feature.tag.ui

import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.playlist.Music
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.testing.checklist.checklist
import io.github.taetae98coding.diary.core.testing.checklist.checklistDetail
import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.core.testing.contact.contactDetail
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.dday.ddayDetail
import io.github.taetae98coding.diary.core.testing.playlist.music
import io.github.taetae98coding.diary.core.testing.playlist.musicDetail
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
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

internal fun tagChecklist(title: String): ChecklistSummary =
    ChecklistSummary(
        checklist = Arb.checklist(isDeleted = false, detail = Arb.checklistDetail().next().copy(title = title)).next(),
        checkedCount = 1,
        totalCount = 2,
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
