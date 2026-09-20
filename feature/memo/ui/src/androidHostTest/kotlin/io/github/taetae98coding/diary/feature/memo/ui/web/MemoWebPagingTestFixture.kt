package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.web.Web

internal fun webPagingDataOf(webList: List<Web>): PagingData<Web> =
    PagingData.from(
        data = webList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun refreshingWebPagingData(): PagingData<Web> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun appendingWebPagingDataOf(webList: List<Web>): PagingData<Web> =
    PagingData.from(
        data = webList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Loading,
            ),
    )

internal fun appendFailedWebPagingDataOf(webList: List<Web>): PagingData<Web> =
    PagingData.from(
        data = webList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Error(IllegalStateException("append failed")),
            ),
    )
