package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.place.Place

/**
 * 전달한 장소가 모두 준비된 상태의 선택 목록을 만든다.
 */
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

/**
 * 아직 아무 장소도 준비되지 않고 목록을 처음 불러오는 중인 선택 목록을 만든다.
 */
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

/**
 * 전달한 장소까지 준비되고 다음 장소를 불러오는 데 실패한 선택 목록을 만든다.
 */
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
