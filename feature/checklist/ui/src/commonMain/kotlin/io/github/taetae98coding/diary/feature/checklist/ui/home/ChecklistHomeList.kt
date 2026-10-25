package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.ChecklistIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.card.SwipeToDeleteChecklistCard
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_home_empty_description
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_home_empty_title
import io.github.taetae98coding.diary.feature.checklist.ui.previewChecklistSummary
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistHomeList(
    onEvent: (ChecklistHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    checklistPagingItems: LazyPagingItems<ChecklistSummary> = remember { flowOf(PagingData.empty<ChecklistSummary>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    bottomPadding: Dp = DiaryTheme.dimens.screenVerticalPadding,
) {
    DiaryCrossfade(
        targetState = checklistPagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            DiaryPullToRefreshBox(
                isRefreshingProvider = isRefreshingProvider,
                onRefresh = { onEvent(ChecklistHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                DiaryEmptyBox(
                    title = stringResource(Res.string.checklist_home_empty_title),
                    // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    description = stringResource(Res.string.checklist_home_empty_description),
                    icon = { ChecklistIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            }
        } else {
            DiaryRefreshableGrid(
                onRefresh = { onEvent(ChecklistHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                isRefreshingProvider = isRefreshingProvider,
                bottomPadding = bottomPadding,
            ) {
                items(
                    count = checklistPagingItems.itemCount,
                    key = checklistPagingItems.itemKey { summary -> summary.checklist.id },
                ) { index ->
                    val summary = checklistPagingItems[index]

                    SwipeToDeleteChecklistCard(
                        onClick = { summary?.let { value -> onEvent(ChecklistHomeScaffoldEvent.ClickChecklist(id = value.checklist.id)) } },
                        onDelete = { summary?.let { value -> onEvent(ChecklistHomeScaffoldEvent.DeleteChecklist(id = value.checklist.id)) } },
                        modifier =
                            Modifier
                                .animateItem()
                                .fillMaxWidth(),
                        summary = summary,
                    )
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun ChecklistHomeListPreview() {
    val summaryList =
        remember {
            listOf(
                previewChecklistSummary(title = "여행 준비"),
                previewChecklistSummary(title = "장보기", checkedCount = 0, totalCount = 0),
            )
        }
    val pagingData = remember(summaryList) { flowOf(PagingData.from(summaryList)) }

    DiaryTheme {
        ChecklistHomeList(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            checklistPagingItems = pagingData.collectAsLazyPagingItems(),
        )
    }
}
