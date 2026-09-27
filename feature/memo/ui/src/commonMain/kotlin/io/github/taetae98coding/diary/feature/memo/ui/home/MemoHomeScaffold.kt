package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.button.ListEntryButton
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.FilterIcon
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
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
import io.github.taetae98coding.diary.core.model.memo.MemoExistenceFilter
import io.github.taetae98coding.diary.core.model.memo.MemoFilterExistence
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_add_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_empty_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_empty_title
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filtered_empty_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filtered_empty_title
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_finished_list_action_label
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val MEMO_HOME_LIST_TEST_TAG: String = "MemoHomeList"

@Composable
internal fun MemoHomeScaffold(
    onEvent: (MemoHomeScaffoldEvent) -> Unit,
    onMemoListEvent: (MemoListEvent) -> Unit,
    modifier: Modifier = Modifier,
    memoListState: MemoListState = rememberMemoListState(),
    listState: LazyListState = rememberLazyListState(),
    sortSheetState: DialogState = rememberDialogState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    memoPagingItems: LazyPagingItems<MemoListItem> = remember { flowOf(PagingData.empty<MemoListItem>()) }.collectAsLazyPagingItems(),
    filterUiStateProvider: () -> MemoHomeScaffoldFilterUiState = { MemoHomeScaffoldFilterUiState() },
    memoListUiStateProvider: () -> MemoListUiState = { MemoListUiState() },
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    componentVisibleProvider: () -> MemoHomeScaffoldComponentVisible = { MemoHomeScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MemoHomeTopBar(
                filterUiStateProvider = filterUiStateProvider,
                onEvent = onEvent,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (componentVisibleProvider().isAddButtonVisible) {
                FloatingAddButton(
                    onClick = { onEvent(MemoHomeScaffoldEvent.ClickAdd) },
                    contentDescription = stringResource(Res.string.memo_home_add_button_content_description),
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            DiaryListSortBarHost(
                onClick = { onEvent(MemoHomeScaffoldEvent.ClickSort) },
                modifier = Modifier.fillMaxWidth(),
                sortProvider = sortProvider,
                isSortVisibleProvider = { memoPagingItems.itemCount > 0 },
                trailing = {
                    ListEntryButton(
                        onClick = { onEvent(MemoHomeScaffoldEvent.ClickFinishedList) },
                        label = stringResource(Res.string.memo_home_finished_list_action_label),
                    )
                },
            )

            MemoList(
                onEvent = onMemoListEvent,
                state = memoListState,
                listState = listState,
                memoPagingItems = memoPagingItems,
                modifier = Modifier.fillMaxSize(),
                uiStateProvider = memoListUiStateProvider,
                sortProvider = sortProvider,
                filterProvider = { filterUiStateProvider().listQueryFilter },
                listTestTag = MEMO_HOME_LIST_TEST_TAG,
                empty = { Empty(isFilterAppliedProvider = { filterUiStateProvider().isApplied }) },
            )
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(MemoHomeScaffoldEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortList = memoListSortList,
        sortProvider = sortProvider,
    )
}

@Composable
private fun Empty(
    isFilterAppliedProvider: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    DiaryCrossfade(
        targetState = isFilterAppliedProvider(),
        modifier = modifier,
    ) { isFilterApplied ->
        if (isFilterApplied) {
            DiaryEmptyBox(
                title = stringResource(Res.string.memo_home_filtered_empty_title),
                description = stringResource(Res.string.memo_home_filtered_empty_description),
                icon = { FilterIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            )
        } else {
            DiaryEmptyBox(
                title = stringResource(Res.string.memo_home_empty_title),
                description = stringResource(Res.string.memo_home_empty_description),
                icon = { MemoIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            )
        }
    }
}

@ScreenPreview
@Composable
private fun MemoHomeScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isFilterApplied: Boolean,
) {
    val filterUiState =
        if (isFilterApplied) {
            MemoHomeScaffoldFilterUiState(existence = MemoExistenceFilter(date = MemoFilterExistence.EXIST))
        } else {
            MemoHomeScaffoldFilterUiState()
        }

    DiaryTheme {
        MemoHomeScaffold(
            onEvent = {},
            onMemoListEvent = {},
            filterUiStateProvider = { filterUiState },
        )
    }
}
