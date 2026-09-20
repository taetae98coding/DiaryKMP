package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.place.Place

internal fun placePagingDataOf(placeList: List<Place>): PagingData<Place> =
    PagingData.from(
        data = placeList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun refreshingPlacePagingData(): PagingData<Place> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun appendFailedPlacePagingDataOf(placeList: List<Place>): PagingData<Place> =
    PagingData.from(
        data = placeList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Error(IllegalStateException("append failed")),
            ),
    )
