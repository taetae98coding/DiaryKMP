package io.github.taetae98coding.diary.feature.tag.ui.detail.dday

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.DDayIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableEmptyBox
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.dday.DDayListEvent
import io.github.taetae98coding.diary.compose.dday.DDayListState
import io.github.taetae98coding.diary.compose.dday.SwipeDDayCard
import io.github.taetae98coding.diary.compose.dday.previewDDay
import io.github.taetae98coding.diary.compose.dday.previewDDayToday
import io.github.taetae98coding.diary.compose.dday.rememberDDayListState
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_dday_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_dday_empty_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagDetailDDayTab(
    onEvent: (TagDetailDDayContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: DDayListState = rememberDDayListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    ddayPagingItems: LazyPagingItems<DDay> = remember { flowOf(PagingData.empty<DDay>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    scopeProvider: () -> TagScope = { TagScope.SELF },
) {
    ListQueryScrollEffect(
        gridState = gridState,
        filterProvider = scopeProvider,
        itemListProvider = { ddayPagingItems.itemSnapshotList.items },
        isRefreshingProvider = { ddayPagingItems.loadState.refresh is LoadState.Loading },
    )

    DiaryCrossfade(
        targetState = ddayPagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            Empty(
                onEvent = onEvent,
                isRefreshingProvider = isRefreshingProvider,
            )
        } else {
            DDayGrid(
                onEvent = onEvent,
                listState = listState,
                gridState = gridState,
                ddayPagingItems = ddayPagingItems,
                isRefreshingProvider = isRefreshingProvider,
            )
        }
    }
}

@Composable
private fun DDayGrid(
    onEvent: (TagDetailDDayContentEvent) -> Unit,
    listState: DDayListState,
    gridState: LazyGridState,
    ddayPagingItems: LazyPagingItems<DDay>,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryRefreshableGrid(
        onRefresh = { onEvent(TagDetailDDayContentEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
        state = gridState,
        isRefreshingProvider = isRefreshingProvider,
    ) {
        items(
            count = ddayPagingItems.itemCount,
            key = ddayPagingItems.itemKey { dday -> dday.id },
        ) { index ->
            SwipeDDayCard(
                onEvent = { event ->
                    when (event) {
                        is DDayListEvent.ClickDDay -> onEvent(TagDetailDDayContentEvent.ClickDDay(id = event.id))
                        is DDayListEvent.SwipeDelete -> onEvent(TagDetailDDayContentEvent.DeleteDDay(id = event.id))
                    }
                },
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
                state = listState,
                dday = ddayPagingItems[index],
            )
        }
    }
}

@Composable
private fun Empty(
    onEvent: (TagDetailDDayContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryRefreshableEmptyBox(
        onRefresh = { onEvent(TagDetailDDayContentEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
        isRefreshingProvider = isRefreshingProvider,
    ) {
        DiaryEmptyBox(
            title = stringResource(Res.string.tag_detail_dday_empty_title),
            description = stringResource(Res.string.tag_detail_dday_empty_description),
            icon = { DDayIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
        )
    }
}

@ScreenPreview
@Composable
private fun TagDetailDDayTabPreview() {
    val pagingData = remember { flowOf(PagingData.from(listOf(previewDDay(title = "만난 날")))) }

    DiaryTheme {
        Surface {
            TagDetailDDayTab(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                listState = rememberDDayListState(initialToday = previewDDayToday()),
                ddayPagingItems = pagingData.collectAsLazyPagingItems(),
            )
        }
    }
}
