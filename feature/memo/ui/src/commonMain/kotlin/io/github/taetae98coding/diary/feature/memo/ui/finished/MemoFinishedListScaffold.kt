package io.github.taetae98coding.diary.feature.memo.ui.finished

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
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.FinishIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.swipe.SwipeFinishAction
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBar
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.compose.list.sort.memoListSortList
import io.github.taetae98coding.diary.compose.memo.list.MemoList
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.MemoListState
import io.github.taetae98coding.diary.compose.memo.list.MemoListUiState
import io.github.taetae98coding.diary.compose.memo.list.rememberMemoListState
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_finished_list_empty_title
import io.github.taetae98coding.diary.feature.memo.ui.memo_finished_list_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_finished_list_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val MEMO_FINISHED_LIST_TEST_TAG: String = "MemoFinishedList"

@Composable
internal fun MemoFinishedListScaffold(
    onEvent: (MemoFinishedListScaffoldEvent) -> Unit,
    onMemoListEvent: (MemoListEvent) -> Unit,
    modifier: Modifier = Modifier,
    memoListState: MemoListState = rememberMemoListState(),
    sortSheetState: DialogState = rememberDialogState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    memoPagingItems: LazyPagingItems<MemoListItem> = remember { flowOf(PagingData.empty<MemoListItem>()) }.collectAsLazyPagingItems(),
    memoListUiStateProvider: () -> MemoListUiState = { MemoListUiState() },
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.memo_finished_list_title),
                onNavigateUp = { onEvent(MemoFinishedListScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.memo_finished_list_navigate_up_button_content_description),
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
            DiaryListSortBar(
                onClick = { onEvent(MemoFinishedListScaffoldEvent.ClickSort) },
                modifier = Modifier.fillMaxWidth(),
                sortProvider = sortProvider,
            )

            MemoList(
                onEvent = onMemoListEvent,
                state = memoListState,
                memoPagingItems = memoPagingItems,
                modifier = Modifier.fillMaxSize(),
                uiStateProvider = memoListUiStateProvider,
                sortProvider = sortProvider,
                listTestTag = MEMO_FINISHED_LIST_TEST_TAG,
                finishAction = SwipeFinishAction.RESTART,
                empty = {
                    DiaryEmptyBox(
                        title = stringResource(Res.string.memo_finished_list_empty_title),
                        icon = { FinishIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                    )
                },
            )
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(MemoFinishedListScaffoldEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortList = memoListSortList,
        sortProvider = sortProvider,
    )
}

@ScreenPreview
@Composable
private fun MemoFinishedListScaffoldPreview() {
    DiaryTheme {
        MemoFinishedListScaffold(
            onEvent = {},
            onMemoListEvent = {},
        )
    }
}
