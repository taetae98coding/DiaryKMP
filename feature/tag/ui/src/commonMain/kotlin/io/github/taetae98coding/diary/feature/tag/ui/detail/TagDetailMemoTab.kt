package io.github.taetae98coding.diary.feature.tag.ui.detail

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
import io.github.taetae98coding.diary.compose.core.button.ListEntryButton
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.sort.DiaryListSortBar
import io.github.taetae98coding.diary.compose.core.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.compose.core.sort.memoListSortList
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MemoList
import io.github.taetae98coding.diary.compose.memo.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.MemoListItem
import io.github.taetae98coding.diary.compose.memo.MemoListState
import io.github.taetae98coding.diary.compose.memo.MemoListUiState
import io.github.taetae98coding.diary.compose.memo.rememberMemoListState
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_memo_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_memo_empty_title
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_memo_finished_list_action_label
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val TAG_DETAIL_MEMO_LIST_TEST_TAG: String = "TagDetailMemoList"

@Composable
internal fun TagDetailMemoTab(
    onEvent: (TagDetailMemoContentEvent) -> Unit,
    onMemoListEvent: (MemoListEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: MemoListState = rememberMemoListState(),
    sortSheetState: DialogState = rememberDialogState(),
    memoPagingItems: LazyPagingItems<MemoListItem> = remember { flowOf(PagingData.empty<MemoListItem>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoListUiState = { MemoListUiState() },
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
) {
    Column(modifier = modifier) {
        DiaryListSortBar(
            onClick = { onEvent(TagDetailMemoContentEvent.ClickSort) },
            modifier = Modifier.fillMaxWidth(),
            sortProvider = sortProvider,
            trailing = {
                ListEntryButton(
                    onClick = { onEvent(TagDetailMemoContentEvent.ClickFinishedList) },
                    label = stringResource(Res.string.tag_detail_memo_finished_list_action_label),
                )
            },
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
            listTestTag = TAG_DETAIL_MEMO_LIST_TEST_TAG,
            empty = {
                DiaryEmptyBox(
                    title = stringResource(Res.string.tag_detail_memo_empty_title),
                    description = stringResource(Res.string.tag_detail_memo_empty_description),
                    icon = { MemoIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            },
        )
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(TagDetailMemoContentEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortList = memoListSortList,
        sortProvider = sortProvider,
    )
}

@ScreenPreview
@Composable
private fun TagDetailMemoTabPreview() {
    DiaryTheme {
        Surface {
            TagDetailMemoTab(
                onEvent = {},
                onMemoListEvent = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
