package io.github.taetae98coding.diary.feature.memo.ui

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDraft
import io.github.taetae98coding.diary.core.model.memo.MemoDraftProgress
import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewContact(
    name: String,
    phoneNumber: String,
    nameQualifier: String = "",
): ContactListItem =
    ContactListItem(
        contact =
            Contact(
                id = Uuid.random(),
                detail =
                    ContactDetail(
                        name = name,
                        description = "",
                        height = null,
                        footSize = null,
                        birthday = null,
                        birthYear = null,
                        hometown = "",
                        howWeMet = "",
                        phoneNumberList = listOf(ContactPhoneNumber(number = phoneNumber)),
                    ),
                isFavorite = false,
                isDeleted = false,
                updatedAt = Instant.DISTANT_PAST,
                createdAt = Instant.DISTANT_PAST,
            ),
        nameQualifier = nameQualifier,
    )

internal fun previewMemoDraftProgress(): MemoDraftProgress =
    MemoDraftProgress(
        title = "주간 회고 정리",
        description = "## 이번 주\n- 한 일",
    )

internal fun previewMemoDraft(): MemoDraft =
    MemoDraft(
        title = "주간 회고 정리",
        description = "## 이번 주\n- 한 일\n- 배운 것",
        dateTime = MemoDateTime.AllDay(dateRange = LocalDate(year = 2026, month = 9, day = 21)..LocalDate(year = 2026, month = 9, day = 22)),
    )
