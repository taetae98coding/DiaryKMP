package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

/**
 * 전달한 연락처가 모두 준비된 상태의 연락처 목록을 만든다.
 */
internal fun contactPagingDataOf(contactList: List<Contact>): PagingData<Contact> =
    PagingData.from(
        data = contactList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun loadingContactPagingData(): PagingData<Contact> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun failedContactPagingData(): PagingData<Contact> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Error(IllegalStateException("연락처 목록을 조회하지 못했습니다.")),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

// FixtureMonkey가 Instant를 생성하지 못하므로 연락처는 직접 만든다.
internal fun testContact(
    name: String,
    phoneNumberList: List<String> = emptyList(),
    description: String = "설명-${fixtureMonkey.giveMeOne<String>()}",
): Contact =
    Contact(
        id = Uuid.random(),
        detail =
            ContactDetail(
                name = name,
                description = description,
                height = null,
                footSize = null,
                birthday = null,
                phoneNumberList = phoneNumberList.map { number -> ContactPhoneNumber(number = number) },
            ),
        isDeleted = false,
        updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
        createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
    )
