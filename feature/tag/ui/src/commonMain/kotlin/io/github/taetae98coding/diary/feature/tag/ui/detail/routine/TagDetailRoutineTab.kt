package io.github.taetae98coding.diary.feature.tag.ui.detail.routine

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import io.github.taetae98coding.diary.compose.core.button.ListEntryButton
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.RoutineIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableEmptyBox
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBarHost
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.RoutineListState
import io.github.taetae98coding.diary.compose.routine.SwipeRoutineCard
import io.github.taetae98coding.diary.compose.routine.previewRoutineSummary
import io.github.taetae98coding.diary.compose.routine.previewRoutineToday
import io.github.taetae98coding.diary.compose.routine.rememberRoutineListState
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_routine_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_routine_empty_title
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_routine_finished_list_action_label
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagDetailRoutineTab(
    onEvent: (TagDetailRoutineContentEvent) -> Unit,
    onRoutineListEvent: (RoutineListEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: RoutineListState = rememberRoutineListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    routinePagingItems: LazyPagingItems<RoutineSummary> = remember { flowOf(PagingData.empty<RoutineSummary>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    scopeProvider: () -> TagScope = { TagScope.SELF },
) {
    ListQueryScrollEffect(
        gridState = gridState,
        filterProvider = scopeProvider,
        itemListProvider = { routinePagingItems.itemSnapshotList.items.map { summary -> summary.routine.id } },
        isRefreshingProvider = { routinePagingItems.loadState.refresh is LoadState.Loading },
    )

    Column(modifier = modifier) {
        DiaryListSortBarHost(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            isSortVisibleProvider = { false },
            trailing = {
                ListEntryButton(
                    onClick = { onEvent(TagDetailRoutineContentEvent.ClickFinishedList) },
                    label = stringResource(Res.string.tag_detail_routine_finished_list_action_label),
                )
            },
        )
        DiaryCrossfade(
            targetState = routinePagingItems.isLoadedEmpty(),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1F),
        ) { isEmpty ->
            if (isEmpty) {
                Empty(
                    onEvent = onEvent,
                    isRefreshingProvider = isRefreshingProvider,
                )
            } else {
                RoutineGrid(
                    onEvent = onEvent,
                    onRoutineListEvent = onRoutineListEvent,
                    listState = listState,
                    gridState = gridState,
                    routinePagingItems = routinePagingItems,
                    isRefreshingProvider = isRefreshingProvider,
                )
            }
        }
    }
}

@Composable
private fun RoutineGrid(
    onEvent: (TagDetailRoutineContentEvent) -> Unit,
    onRoutineListEvent: (RoutineListEvent) -> Unit,
    listState: RoutineListState,
    gridState: LazyGridState,
    routinePagingItems: LazyPagingItems<RoutineSummary>,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryRefreshableGrid(
        onRefresh = { onEvent(TagDetailRoutineContentEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
        state = gridState,
        isRefreshingProvider = isRefreshingProvider,
    ) {
        items(
            count = routinePagingItems.itemCount,
            key = routinePagingItems.itemKey { summary -> summary.routine.id },
            span = { GridItemSpan(maxLineSpan) },
        ) { index ->
            SwipeRoutineCard(
                onEvent = onRoutineListEvent,
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
                state = listState,
                summary = routinePagingItems[index],
            )
        }
    }
}

@Composable
private fun Empty(
    onEvent: (TagDetailRoutineContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryRefreshableEmptyBox(
        onRefresh = { onEvent(TagDetailRoutineContentEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
        isRefreshingProvider = isRefreshingProvider,
    ) {
        DiaryEmptyBox(
            title = stringResource(Res.string.tag_detail_routine_empty_title),
            description = stringResource(Res.string.tag_detail_routine_empty_description),
            icon = { RoutineIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
        )
    }
}

@ScreenPreview
@Composable
private fun TagDetailRoutineTabPreview() {
    val pagingData = remember { flowOf(PagingData.from(listOf(previewRoutineSummary(title = "물 마시기")))) }

    DiaryTheme {
        Surface {
            TagDetailRoutineTab(
                onEvent = {},
                onRoutineListEvent = {},
                modifier = Modifier.fillMaxSize(),
                listState = rememberRoutineListState(initialToday = previewRoutineToday()),
                routinePagingItems = pagingData.collectAsLazyPagingItems(),
            )
        }
    }
}
