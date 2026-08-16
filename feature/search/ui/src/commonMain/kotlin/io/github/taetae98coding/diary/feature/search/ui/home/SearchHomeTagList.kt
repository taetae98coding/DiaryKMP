package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.icon.TagIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.TagCard
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.search.ui.previewTag
import kotlinx.coroutines.flow.flowOf

internal const val SEARCH_HOME_TAG_LIST_TEST_TAG: String = "SearchHomeTagList"

@Composable
internal fun SearchHomeTagList(
    onEvent: (SearchHomeResultEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    sortSheetState: DialogState = rememberDialogState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    query: String = "",
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    SearchHomeResult(
        onEvent = onEvent,
        modifier = modifier,
        sortSheetState = sortSheetState,
        query = query,
        pagingItems = tagPagingItems,
        sortProvider = sortProvider,
        emptyIcon = { TagIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
    ) {
        LazyColumn(
            modifier = Modifier.testTag(SEARCH_HOME_TAG_LIST_TEST_TAG),
            state = listState,
            contentPadding = DiaryTheme.dimens.screenPaddingValues,
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        ) {
            items(
                count = tagPagingItems.itemCount,
                key = tagPagingItems.itemKey { tag -> tag.id },
            ) { index ->
                val tag = tagPagingItems[index]

                TagCard(
                    onClick = { tag?.let { value -> onEvent(SearchHomeResultEvent.ClickResult(id = value.id)) } },
                    modifier =
                        Modifier
                            .animateItem()
                            .fillMaxWidth(),
                    tag = tag,
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun SearchHomeTagListPreview() {
    val tagPagingData =
        remember {
            flowOf(
                PagingData.from(
                    listOf(
                        previewTag(emoji = "✈️", title = "여행", color = 0xFF3A7BD5),
                        previewTag(emoji = "", title = "여행 기록", color = 0xFF81C784),
                    ),
                ),
            )
        }

    DiaryTheme {
        SearchHomeTagList(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
        )
    }
}
