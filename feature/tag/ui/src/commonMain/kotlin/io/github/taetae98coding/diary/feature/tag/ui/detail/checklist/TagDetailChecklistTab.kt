package io.github.taetae98coding.diary.feature.tag.ui.detail.checklist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.checklist.ChecklistListEvent
import io.github.taetae98coding.diary.compose.checklist.SwipeChecklistCard
import io.github.taetae98coding.diary.compose.checklist.previewChecklistSummary
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.ChecklistIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_checklist_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_checklist_empty_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagDetailChecklistTab(
    onEvent: (TagDetailChecklistContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    checklistPagingItems: LazyPagingItems<ChecklistSummary> = remember { flowOf(PagingData.empty<ChecklistSummary>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    scopeProvider: () -> TagScope = { TagScope.SELF },
) {
    ListQueryScrollEffect(
        gridState = gridState,
        filterProvider = scopeProvider,
        itemListProvider = { checklistPagingItems.itemSnapshotList.items },
        isRefreshingProvider = { checklistPagingItems.loadState.refresh is LoadState.Loading },
    )

    DiaryCrossfade(
        targetState = checklistPagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            Empty(
                onEvent = onEvent,
                isRefreshingProvider = isRefreshingProvider,
            )
        } else {
            ChecklistGrid(
                onEvent = onEvent,
                gridState = gridState,
                checklistPagingItems = checklistPagingItems,
                isRefreshingProvider = isRefreshingProvider,
            )
        }
    }
}

@Composable
private fun ChecklistGrid(
    onEvent: (TagDetailChecklistContentEvent) -> Unit,
    gridState: LazyGridState,
    checklistPagingItems: LazyPagingItems<ChecklistSummary>,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryRefreshableGrid(
        onRefresh = { onEvent(TagDetailChecklistContentEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
        state = gridState,
        isRefreshingProvider = isRefreshingProvider,
    ) {
        items(
            count = checklistPagingItems.itemCount,
            key = checklistPagingItems.itemKey { item -> item.checklist.id },
        ) { index ->
            val summary = checklistPagingItems[index]

            SwipeChecklistCard(
                onEvent = { event ->
                    when (event) {
                        is ChecklistListEvent.ClickChecklist -> onEvent(TagDetailChecklistContentEvent.ClickChecklist(id = event.id))
                        is ChecklistListEvent.SwipeDelete -> onEvent(TagDetailChecklistContentEvent.DeleteChecklist(id = event.id))
                    }
                },
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
                summary = summary,
            )
        }
    }
}

@Composable
private fun Empty(
    onEvent: (TagDetailChecklistContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryPullToRefreshBox(
        isRefreshingProvider = isRefreshingProvider,
        onRefresh = { onEvent(TagDetailChecklistContentEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
    ) {
        DiaryEmptyBox(
            title = stringResource(Res.string.tag_detail_checklist_empty_title),
            // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            description = stringResource(Res.string.tag_detail_checklist_empty_description),
            icon = { ChecklistIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
        )
    }
}

@ScreenPreview
@Composable
private fun TagDetailChecklistTabPreview() {
    val pagingData = remember { flowOf(PagingData.from(listOf(previewChecklistSummary(title = "여행 준비")))) }

    DiaryTheme {
        Surface {
            TagDetailChecklistTab(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                checklistPagingItems = pagingData.collectAsLazyPagingItems(),
            )
        }
    }
}
