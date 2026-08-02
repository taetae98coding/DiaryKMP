@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.taetae98coding.diary.feature.tag.ui.memo.finished

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.FinishIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.sort.DiaryListSortBar
import io.github.taetae98coding.diary.compose.core.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.compose.core.sort.memoListSortList
import io.github.taetae98coding.diary.compose.core.swipe.SwipeFinishAction
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MemoList
import io.github.taetae98coding.diary.compose.memo.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.MemoListItem
import io.github.taetae98coding.diary.compose.memo.MemoListState
import io.github.taetae98coding.diary.compose.memo.MemoListUiState
import io.github.taetae98coding.diary.compose.memo.rememberMemoListState
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_memo_finished_list_empty_title
import io.github.taetae98coding.diary.feature.tag.ui.tag_memo_finished_list_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_memo_finished_list_subtitle
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val TAG_MEMO_FINISHED_LIST_TEST_TAG: String = "TagMemoFinishedList"

@Composable
internal fun TagMemoFinishedListScaffold(
    onEvent: (TagMemoFinishedListScaffoldEvent) -> Unit,
    onMemoListEvent: (MemoListEvent) -> Unit,
    modifier: Modifier = Modifier,
    memoListState: MemoListState = rememberMemoListState(),
    sortSheetState: DialogState = rememberDialogState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    memoPagingItems: LazyPagingItems<MemoListItem> = remember { flowOf(PagingData.empty<MemoListItem>()) }.collectAsLazyPagingItems(),
    memoListUiStateProvider: () -> MemoListUiState = { MemoListUiState() },
    uiStateProvider: () -> TagMemoFinishedListUiState = { TagMemoFinishedListUiState() },
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                uiStateProvider = uiStateProvider,
                onEvent = onEvent,
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
                onClick = { onEvent(TagMemoFinishedListScaffoldEvent.ClickSort) },
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
                listTestTag = TAG_MEMO_FINISHED_LIST_TEST_TAG,
                finishAction = SwipeFinishAction.RESTART,
                empty = {
                    DiaryEmptyBox(
                        title = stringResource(Res.string.tag_memo_finished_list_empty_title),
                        icon = { FinishIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                    )
                },
            )
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(TagMemoFinishedListScaffoldEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortList = memoListSortList,
        sortProvider = sortProvider,
    )
}

@Composable
private fun TopBar(
    onEvent: (TagMemoFinishedListScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> TagMemoFinishedListUiState = { TagMemoFinishedListUiState() },
) {
    TopAppBar(
        title = {
            Text(
                text = uiStateProvider().title,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
            )
        },
        subtitle = {
            Text(
                text = stringResource(Res.string.tag_memo_finished_list_subtitle),
                maxLines = 1,
            )
        },
        modifier = modifier,
        navigationIcon = {
            NavigateUpButton(
                onClick = { onEvent(TagMemoFinishedListScaffoldEvent.ClickNavigateUp) },
                contentDescription = stringResource(Res.string.tag_memo_finished_list_navigate_up_button_content_description),
            )
        },
    )
}

@ScreenPreview
@Composable
private fun TagMemoFinishedListScaffoldPreview() {
    DiaryTheme {
        TagMemoFinishedListScaffold(
            onEvent = {},
            onMemoListEvent = {},
            uiStateProvider = { TagMemoFinishedListUiState(title = "📌 Tag") },
        )
    }
}
