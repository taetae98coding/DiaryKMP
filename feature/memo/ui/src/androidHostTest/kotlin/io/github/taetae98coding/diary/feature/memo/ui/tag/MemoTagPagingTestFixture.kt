package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag

/**
 * 전달한 태그가 모두 준비된 상태의 선택 목록을 만든다.
 */
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

/**
 * 아직 아무 태그도 준비되지 않고 목록을 처음 불러오는 중인 선택 목록을 만든다.
 */
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

/**
 * 전달한 태그까지 준비되고 다음 태그를 이어서 불러오는 중인 선택 목록을 만든다.
 */
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

/**
 * 전달한 태그까지 준비되고 다음 태그를 불러오는 데 실패한 선택 목록을 만든다.
 */
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
