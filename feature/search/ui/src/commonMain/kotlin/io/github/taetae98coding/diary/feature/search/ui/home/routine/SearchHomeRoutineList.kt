package io.github.taetae98coding.diary.feature.search.ui.home.routine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.icon.RoutineIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.systemscroll.systemScroll
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.RoutineListState
import io.github.taetae98coding.diary.compose.routine.SwipeRoutineCard
import io.github.taetae98coding.diary.compose.routine.previewRoutineDetail
import io.github.taetae98coding.diary.compose.routine.previewRoutineSummary
import io.github.taetae98coding.diary.compose.routine.previewRoutineToday
import io.github.taetae98coding.diary.compose.routine.rememberRoutineListState
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultBody
import kotlinx.coroutines.flow.flowOf

internal const val SEARCH_HOME_ROUTINE_LIST_TEST_TAG: String = "SearchHomeRoutineList"

@Composable
internal fun SearchHomeRoutineList(
    onRoutineListEvent: (RoutineListEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    routineListState: RoutineListState = rememberRoutineListState(),
    routinePagingItems: LazyPagingItems<RoutineSummary> = remember { flowOf(PagingData.empty<RoutineSummary>()) }.collectAsLazyPagingItems(),
    query: String = "",
) {
    SearchHomeResultBody(
        emptyIcon = { RoutineIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
        modifier = modifier.fillMaxSize(),
        query = query,
        pagingItems = routinePagingItems,
    ) {
        LazyColumn(
            modifier = Modifier.testTag(SEARCH_HOME_ROUTINE_LIST_TEST_TAG).systemScroll(listState) { listState.animateScrollToItem(0) },
            state = listState,
            contentPadding = DiaryTheme.dimens.screenPaddingValues,
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        ) {
            items(
                count = routinePagingItems.itemCount,
                key = routinePagingItems.itemKey { summary -> summary.routine.id },
            ) { index ->
                SwipeRoutineCard(
                    onEvent = onRoutineListEvent,
                    modifier =
                        Modifier
                            .animateItem()
                            .fillMaxWidth(),
                    state = routineListState,
                    summary = routinePagingItems[index],
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun SearchHomeRoutineListPreview() {
    val routinePagingData =
        remember {
            flowOf(
                PagingData.from(
                    listOf(
                        previewRoutineSummary(title = "물 마시기"),
                        previewRoutineSummary(title = "스트레칭", isFinished = true),
                        previewRoutineSummary(title = "달리기", cycleAmount = 12_000, detail = previewRoutineDetail(title = "달리기", goalAmount = null, color = 0xFFE57373)),
                    ),
                ),
            )
        }

    DiaryTheme {
        SearchHomeRoutineList(
            onRoutineListEvent = {},
            modifier = Modifier.fillMaxSize(),
            routineListState = rememberRoutineListState(initialToday = previewRoutineToday()),
            routinePagingItems = routinePagingData.collectAsLazyPagingItems(),
        )
    }
}
