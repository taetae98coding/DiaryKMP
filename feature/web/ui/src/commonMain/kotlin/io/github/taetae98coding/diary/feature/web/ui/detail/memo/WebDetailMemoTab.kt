package io.github.taetae98coding.diary.feature.web.ui.detail.memo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.web_detail_memo_empty_description
import io.github.taetae98coding.diary.feature.web.ui.web_detail_memo_empty_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val WEB_DETAIL_MEMO_LIST_TEST_TAG: String = "WebDetailMemoList"

@Composable
internal fun WebDetailMemoTab(
    onEvent: (WebDetailMemoContentEvent) -> Unit,
    onMemoListEvent: (MemoListEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: MemoListState = rememberMemoListState(),
    sortSheetState: DialogState = rememberDialogState(),
    memoPagingItems: LazyPagingItems<MemoListItem> = remember { flowOf(PagingData.empty<MemoListItem>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoListUiState = { MemoListUiState() },
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
) {
    Box(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            DiaryListSortBarHost(
                onClick = { onEvent(WebDetailMemoContentEvent.ClickSort) },
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
                listTestTag = WEB_DETAIL_MEMO_LIST_TEST_TAG,
                empty = {
                    DiaryEmptyBox(
                        title = stringResource(Res.string.web_detail_memo_empty_title),
                        description = stringResource(Res.string.web_detail_memo_empty_description),
                        icon = { MemoIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                    )
                },
            )
        }

        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(DiaryTheme.dimens.screenPaddingValues),
        ) {
            WebDetailMemoFloatingActionButton(onClick = { onEvent(WebDetailMemoContentEvent.ClickAdd) })
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(WebDetailMemoContentEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortList = memoListSortList,
        sortProvider = sortProvider,
    )
}

@ScreenPreview
@Composable
private fun WebDetailMemoTabPreview() {
    DiaryTheme {
        Surface {
            WebDetailMemoTab(
                onEvent = {},
                onMemoListEvent = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
