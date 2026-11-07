package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactListItem
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.measure.Length
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun contactPagingDataOf(contactList: List<ContactListItem>): PagingData<ContactListItem> =
    PagingData.from(
        data = contactList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun loadingContactPagingData(): PagingData<ContactListItem> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun failedContactPagingData(): PagingData<ContactListItem> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Error(IllegalStateException("연락처 목록을 조회하지 못했습니다.")),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun testContact(
    name: String,
    phoneNumberList: List<String> = emptyList(),
    description: String = "설명-${Arb.string().next()}",
    height: Length? = null,
    footSize: Length? = null,
    birthday: ContactBirthday? = null,
    birthYear: Int? = birthday?.date?.year,
    hometown: String = "",
    howWeMet: String = "",
    isFavorite: Boolean = false,
    nameQualifier: String = "",
): ContactListItem =
    ContactListItem(
        contact =
            Contact(
                id = Uuid.random(),
                detail =
                    ContactDetail(
                        name = name,
                        description = description,
                        height = height,
                        footSize = footSize,
                        birthday = birthday,
                        birthYear = birthYear,
                        hometown = hometown,
                        howWeMet = howWeMet,
                        phoneNumberList = phoneNumberList.map { number -> ContactPhoneNumber(number = number) },
                    ),
                isFavorite = isFavorite,
                isDeleted = false,
                updatedAt = Arb.secondPrecisionInstant().next(),
                createdAt = Arb.secondPrecisionInstant().next(),
            ),
        nameQualifier = nameQualifier,
    )
