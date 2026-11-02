package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.contact.ContactListItem

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

internal fun refreshingContactPagingData(): PagingData<ContactListItem> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun refreshFailedContactPagingData(): PagingData<ContactListItem> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Error(IllegalStateException("refresh failed")),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun appendingContactPagingDataOf(contactList: List<ContactListItem>): PagingData<ContactListItem> =
    PagingData.from(
        data = contactList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Loading,
            ),
    )

internal fun appendFailedContactPagingDataOf(contactList: List<ContactListItem>): PagingData<ContactListItem> =
    PagingData.from(
        data = contactList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Error(IllegalStateException("append failed")),
            ),
    )
