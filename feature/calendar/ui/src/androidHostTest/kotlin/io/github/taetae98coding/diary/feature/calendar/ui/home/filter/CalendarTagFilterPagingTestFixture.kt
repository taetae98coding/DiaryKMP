package io.github.taetae98coding.diary.feature.calendar.ui.home.filter

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.tag.Tag

/**
 * 전달한 태그가 모두 준비된 상태의 태그 필터 목록을 만든다.
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
