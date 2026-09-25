package io.github.taetae98coding.diary.feature.tag.ui.finished

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
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
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBarHost
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.list.TagList
import io.github.taetae98coding.diary.feature.tag.ui.tag_finished_list_empty_title
import io.github.taetae98coding.diary.feature.tag.ui.tag_finished_list_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_finished_list_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val TAG_FINISHED_LIST_TEST_TAG: String = "TagFinishedList"

@Composable
internal fun TagFinishedListScaffold(
    onEvent: (TagFinishedListScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    sortSheetState: DialogState = rememberDialogState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> TagFinishedListUiState = { TagFinishedListUiState() },
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.tag_finished_list_title),
                onNavigateUp = { onEvent(TagFinishedListScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.tag_finished_list_navigate_up_button_content_description),
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            DiaryListSortBarHost(
                onClick = { onEvent(TagFinishedListScaffoldEvent.ClickSort) },
                modifier = Modifier.fillMaxWidth(),
                sortProvider = sortProvider,
                isSortVisibleProvider = { tagPagingItems.itemCount > 0 },
            )

            TagList(
                tagPagingItems = tagPagingItems,
                onTagClick = { id -> onEvent(TagFinishedListScaffoldEvent.ClickTag(id)) },
                onRefresh = { onEvent(TagFinishedListScaffoldEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                isRefreshingProvider = { uiStateProvider().isRefreshing },
                sortProvider = sortProvider,
                listTestTag = TAG_FINISHED_LIST_TEST_TAG,
                empty = {
                    DiaryEmptyBox(
                        title = stringResource(Res.string.tag_finished_list_empty_title),
                        icon = { FinishIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                    )
                },
            )
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(TagFinishedListScaffoldEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortProvider = sortProvider,
    )
}

@ScreenPreview
@Composable
private fun TagFinishedListScaffoldPreview() {
    DiaryTheme {
        TagFinishedListScaffold(
            onEvent = {},
        )
    }
}
