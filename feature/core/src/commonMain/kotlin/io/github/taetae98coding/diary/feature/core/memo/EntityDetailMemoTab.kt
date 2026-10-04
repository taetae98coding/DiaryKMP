package io.github.taetae98coding.diary.feature.core.memo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
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
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import kotlinx.coroutines.flow.flowOf

@Composable
public fun EntityDetailMemoTab(
    onEvent: (EntityDetailMemoTabEvent) -> Unit,
    onMemoListEvent: (MemoListEvent) -> Unit,
    emptyTitle: String,
    emptyDescription: String,
    modifier: Modifier = Modifier,
    state: MemoListState = rememberMemoListState(),
    sortSheetState: DialogState = rememberDialogState(),
    memoPagingItems: LazyPagingItems<MemoListItem> = remember { flowOf(PagingData.empty<MemoListItem>()) }.collectAsLazyPagingItems(),
    syncUiStateProvider: () -> SyncRefreshUiState = { SyncRefreshUiState() },
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    filterProvider: () -> Any? = { Unit },
    trailing: @Composable (() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        DiaryListSortBarHost(
            onClick = { onEvent(EntityDetailMemoTabEvent.ClickSort) },
            modifier = Modifier.fillMaxWidth(),
            sortProvider = sortProvider,
            isSortVisibleProvider = { memoPagingItems.itemCount > 0 },
            trailing = trailing,
        )
        MemoList(
            onEvent = onMemoListEvent,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1F),
            state = state,
            memoPagingItems = memoPagingItems,
            isRefreshingProvider = { syncUiStateProvider().isRefreshing },
            sortProvider = sortProvider,
            filterProvider = filterProvider,
            empty = {
                DiaryEmptyBox(
                    title = emptyTitle,
                    description = emptyDescription,
                    icon = { MemoIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            },
        )
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(EntityDetailMemoTabEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortList = memoListSortList,
        sortProvider = sortProvider,
    )
}

@ScreenPreview
@Composable
private fun EntityDetailMemoTabPreview() {
    DiaryTheme {
        Surface {
            EntityDetailMemoTab(
                onEvent = {},
                onMemoListEvent = {},
                emptyTitle = "연결된 메모가 없어요",
                emptyDescription = "메모를 추가해 보세요",
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
