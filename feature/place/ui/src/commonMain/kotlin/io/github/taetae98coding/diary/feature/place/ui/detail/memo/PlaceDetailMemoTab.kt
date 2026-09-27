package io.github.taetae98coding.diary.feature.place.ui.detail.memo

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
import io.github.taetae98coding.diary.compose.memo.list.MemoListUiState
import io.github.taetae98coding.diary.compose.memo.list.rememberMemoListState
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.place_detail_memo_empty_description
import io.github.taetae98coding.diary.feature.place.ui.place_detail_memo_empty_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val PLACE_DETAIL_MEMO_LIST_TEST_TAG: String = "PlaceDetailMemoList"

@Composable
internal fun PlaceDetailMemoTab(
    onEvent: (PlaceDetailMemoContentEvent) -> Unit,
    onMemoListEvent: (MemoListEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: MemoListState = rememberMemoListState(),
    sortSheetState: DialogState = rememberDialogState(),
    memoPagingItems: LazyPagingItems<MemoListItem> = remember { flowOf(PagingData.empty<MemoListItem>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoListUiState = { MemoListUiState() },
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
) {
    Column(modifier = modifier) {
        DiaryListSortBarHost(
            onClick = { onEvent(PlaceDetailMemoContentEvent.ClickSort) },
            modifier = Modifier.fillMaxWidth(),
            sortProvider = sortProvider,
            isSortVisibleProvider = { memoPagingItems.itemCount > 0 },
        )
        MemoList(
            onEvent = onMemoListEvent,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1F),
            state = state,
            memoPagingItems = memoPagingItems,
            uiStateProvider = uiStateProvider,
            sortProvider = sortProvider,
            listTestTag = PLACE_DETAIL_MEMO_LIST_TEST_TAG,
            empty = {
                DiaryEmptyBox(
                    title = stringResource(Res.string.place_detail_memo_empty_title),
                    description = stringResource(Res.string.place_detail_memo_empty_description),
                    icon = { MemoIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            },
        )
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(PlaceDetailMemoContentEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortList = memoListSortList,
        sortProvider = sortProvider,
    )
}

@ScreenPreview
@Composable
private fun PlaceDetailMemoTabPreview() {
    DiaryTheme {
        Surface {
            PlaceDetailMemoTab(
                onEvent = {},
                onMemoListEvent = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
