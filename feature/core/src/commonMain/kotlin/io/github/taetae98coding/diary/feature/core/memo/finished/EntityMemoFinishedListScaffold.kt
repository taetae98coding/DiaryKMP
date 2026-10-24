package io.github.taetae98coding.diary.feature.core.memo.finished

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.FinishIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeFinishAction
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBarHost
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.compose.list.sort.memoListSortList
import io.github.taetae98coding.diary.compose.memo.list.MemoList
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.MemoListState
import io.github.taetae98coding.diary.compose.memo.list.rememberMemoListState
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.core.Res
import io.github.taetae98coding.diary.feature.core.entity_memo_finished_list_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.core.entity_memo_finished_list_subtitle
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
public fun EntityMemoFinishedListScaffold(
    onEvent: (EntityMemoFinishedListScaffoldEvent) -> Unit,
    onMemoListEvent: (MemoListEvent) -> Unit,
    emptyTitle: String,
    modifier: Modifier = Modifier,
    memoListState: MemoListState = rememberMemoListState(),
    sortSheetState: DialogState = rememberDialogState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    memoPagingItems: LazyPagingItems<MemoListItem> = remember { flowOf(PagingData.empty<MemoListItem>()) }.collectAsLazyPagingItems(),
    syncUiStateProvider: () -> SyncRefreshUiState = { SyncRefreshUiState() },
    uiStateProvider: () -> EntityMemoFinishedListUiState = { EntityMemoFinishedListUiState() },
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                onEvent = onEvent,
                uiStateProvider = uiStateProvider,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            DiaryListSortBarHost(
                onClick = { onEvent(EntityMemoFinishedListScaffoldEvent.ClickSort) },
                modifier = Modifier.fillMaxWidth(),
                sortProvider = sortProvider,
                isSortVisibleProvider = { memoPagingItems.itemCount > 0 },
            )

            MemoList(
                onEvent = onMemoListEvent,
                state = memoListState,
                memoPagingItems = memoPagingItems,
                modifier = Modifier.fillMaxSize(),
                isRefreshingProvider = { syncUiStateProvider().isRefreshing },
                sortProvider = sortProvider,
                finishAction = SwipeFinishAction.RESTART,
                empty = {
                    DiaryEmptyBox(
                        title = emptyTitle,
                        icon = { FinishIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                    )
                },
            )
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(EntityMemoFinishedListScaffoldEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortList = memoListSortList,
        sortProvider = sortProvider,
    )
}

@Composable
private fun TopBar(
    onEvent: (EntityMemoFinishedListScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> EntityMemoFinishedListUiState = { EntityMemoFinishedListUiState() },
) {
    val subtitle = stringResource(Res.string.entity_memo_finished_list_subtitle)
    val navigateUpContentDescription = stringResource(Res.string.entity_memo_finished_list_navigate_up_button_content_description)

    DiaryTopBar(
        title = {
            Title(
                text = uiStateProvider().title,
                subtitle = subtitle,
                isMarquee = true,
            )
        },
        modifier = modifier,
        navigationIcon = {
            NavigateUp(
                onClick = { onEvent(EntityMemoFinishedListScaffoldEvent.ClickNavigateUp) },
                contentDescription = navigateUpContentDescription,
            )
        },
    )
}

@ScreenPreview
@Composable
private fun EntityMemoFinishedListScaffoldPreview() {
    DiaryTheme {
        EntityMemoFinishedListScaffold(
            onEvent = {},
            onMemoListEvent = {},
            emptyTitle = "이 연락처에서 완료한 메모가 없습니다",
            uiStateProvider = { EntityMemoFinishedListUiState(title = "홍길동") },
        )
    }
}
