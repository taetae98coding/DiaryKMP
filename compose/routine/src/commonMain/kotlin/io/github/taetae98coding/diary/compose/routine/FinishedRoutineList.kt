package io.github.taetae98coding.diary.compose.routine

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
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.FinishIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableEmptyBox
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import kotlinx.coroutines.flow.flowOf

@Composable
public fun FinishedRoutineList(
    onEvent: (RoutineListEvent) -> Unit,
    onRefresh: () -> Unit,
    emptyTitle: String,
    modifier: Modifier = Modifier,
    state: RoutineListState = rememberRoutineListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    routinePagingItems: LazyPagingItems<RoutineSummary> = remember { flowOf(PagingData.empty<RoutineSummary>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryCrossfade(
        targetState = routinePagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            DiaryRefreshableEmptyBox(
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
                isRefreshingProvider = isRefreshingProvider,
            ) {
                DiaryEmptyBox(
                    title = emptyTitle,
                    icon = { FinishIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            }
        } else {
            DiaryRefreshableGrid(
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                isRefreshingProvider = isRefreshingProvider,
            ) {
                items(
                    count = routinePagingItems.itemCount,
                    key = routinePagingItems.itemKey { summary -> summary.routine.id },
                    span = { GridItemSpan(maxLineSpan) },
                ) { index ->
                    SwipeRoutineCard(
                        onEvent = onEvent,
                        modifier =
                            Modifier
                                .animateItem()
                                .fillMaxWidth(),
                        state = state,
                        summary = routinePagingItems[index],
                    )
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun FinishedRoutineListPreview() {
    val pagingData = remember { flowOf(PagingData.from(listOf(previewRoutineSummary(title = "물 마시기", isFinished = true)))) }

    DiaryTheme {
        Surface {
            FinishedRoutineList(
                onEvent = {},
                onRefresh = {},
                emptyTitle = "완료한 루틴이 없습니다",
                modifier = Modifier.fillMaxSize(),
                state = rememberRoutineListState(initialToday = previewRoutineToday()),
                routinePagingItems = pagingData.collectAsLazyPagingItems(),
            )
        }
    }
}
