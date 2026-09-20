package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag

internal fun tagPagingDataOf(tagList: List<Tag>): PagingData<Tag> =
    PagingData.from(
        data = tagList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun refreshingTagPagingData(): PagingData<Tag> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun appendingTagPagingDataOf(tagList: List<Tag>): PagingData<Tag> =
    PagingData.from(
        data = tagList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Loading,
            ),
    )

internal fun appendFailedTagPagingDataOf(tagList: List<Tag>): PagingData<Tag> =
    PagingData.from(
        data = tagList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Error(IllegalStateException("append failed")),
            ),
    )
