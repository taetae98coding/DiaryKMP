package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
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
import io.github.taetae98coding.diary.compose.core.icon.TagIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBarHost
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.list.TagList
import io.github.taetae98coding.diary.feature.tag.ui.tag_home_add_button_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_home_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_home_empty_title
import io.github.taetae98coding.diary.feature.tag.ui.tag_home_filtered_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_home_filtered_empty_title
import io.github.taetae98coding.diary.feature.tag.ui.tag_home_finished_list_action_label
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val TAG_HOME_LIST_TEST_TAG: String = "TagHomeList"

@Composable
internal fun TagHomeScaffold(
    onEvent: (TagHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    sortSheetState: DialogState = rememberDialogState(),
    gridState: LazyGridState = rememberLazyGridState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> TagHomeUiState = { TagHomeUiState() },
    filterUiStateProvider: () -> TagHomeScaffoldFilterUiState = { TagHomeScaffoldFilterUiState() },
    sortProvider: () -> ListSort = { ListSort.TITLE },
    componentVisibleProvider: () -> TagHomeScaffoldComponentVisible = { TagHomeScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TagHomeTopBar(
                onEvent = onEvent,
                filterUiStateProvider = filterUiStateProvider,
            )
        },
        floatingActionButton = {
            if (componentVisibleProvider().isAddButtonVisible) {
                FloatingAddButton(
                    onClick = { onEvent(TagHomeScaffoldEvent.ClickAdd) },
                    contentDescription = stringResource(Res.string.tag_home_add_button_content_description),
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
                onClick = { onEvent(TagHomeScaffoldEvent.ClickSort) },
                modifier = Modifier.fillMaxWidth(),
                sortProvider = sortProvider,
                isSortVisibleProvider = { tagPagingItems.itemCount > 0 },
                trailing = {
                    ListEntryButton(
                        onClick = { onEvent(TagHomeScaffoldEvent.ClickFinishedList) },
                        label = stringResource(Res.string.tag_home_finished_list_action_label),
                    )
                },
            )

            TagList(
                tagPagingItems = tagPagingItems,
                gridState = gridState,
                onTagClick = { id -> onEvent(TagHomeScaffoldEvent.ClickTag(id)) },
                onRefresh = { onEvent(TagHomeScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                isRefreshingProvider = { uiStateProvider().isRefreshing },
                sortProvider = sortProvider,
                filterProvider = filterUiStateProvider,
                listTestTag = TAG_HOME_LIST_TEST_TAG,
                empty = { Empty(isFilterAppliedProvider = { filterUiStateProvider().isApplied }) },
            )
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(TagHomeScaffoldEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
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
                title = stringResource(Res.string.tag_home_filtered_empty_title),
                description = stringResource(Res.string.tag_home_filtered_empty_description),
                icon = { FilterIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            )
        } else {
            DiaryEmptyBox(
                title = stringResource(Res.string.tag_home_empty_title),
                description = stringResource(Res.string.tag_home_empty_description),
                icon = { TagIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            )
        }
    }
}

@ScreenPreview
@Composable
private fun TagHomeScaffoldPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isFilterApplied: Boolean,
) {
    DiaryTheme {
        TagHomeScaffold(
            onEvent = {},
            filterUiStateProvider = { TagHomeScaffoldFilterUiState(isApplied = isFilterApplied) },
        )
    }
}
