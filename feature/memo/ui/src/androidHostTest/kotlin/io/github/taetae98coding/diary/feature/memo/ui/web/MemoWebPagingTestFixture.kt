package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.web.Web

/**
 * 전달한 웹 항목이 모두 준비된 상태의 선택 목록을 만든다.
 */
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

/**
 * 아직 아무 웹 항목도 준비되지 않고 목록을 처음 불러오는 중인 선택 목록을 만든다.
 */
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

/**
 * 전달한 웹 항목까지 준비되고 다음 웹 항목을 이어서 불러오는 중인 선택 목록을 만든다.
 */
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

/**
 * 전달한 웹 항목까지 준비되고 다음 웹 항목을 불러오는 데 실패한 선택 목록을 만든다.
 */
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
