package io.github.taetae98coding.diary.feature.tag.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.TagIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableGrid
import io.github.taetae98coding.diary.compose.core.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.TagCard
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.tag.ui.previewTag
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

internal const val TAG_LIST_TEST_TAG: String = "TagList"

@Composable
internal fun TagList(
    onTagClick: (Uuid) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.TITLE },
    filterProvider: () -> Any? = { Unit },
    listTestTag: String = TAG_LIST_TEST_TAG,
    empty: @Composable () -> Unit,
) {
    ListQueryScrollEffect(
        gridState = gridState,
        sortProvider = sortProvider,
        filterProvider = filterProvider,
        itemListProvider = { tagPagingItems.itemSnapshotList.items },
    )

    DiaryCrossfade(
        targetState = tagPagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            DiaryPullToRefreshBox(
                isRefreshingProvider = isRefreshingProvider,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center,
                ) {
                    empty()
                }
            }
        } else {
            DiaryRefreshableGrid(
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                isRefreshingProvider = isRefreshingProvider,
                listTestTag = listTestTag,
            ) {
                items(
                    count = tagPagingItems.itemCount,
                    key = tagPagingItems.itemKey { tag -> tag.id },
                ) { index ->
                    val tag = tagPagingItems[index]

                    TagCard(
                        tag = tag,
                        onClick = { tag?.let { value -> onTagClick(value.id) } },
                        modifier =
                            Modifier
                                .animateItem()
                                .fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun TagListPreview() {
    val tagList =
        remember {
            listOf(
                previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5),
                previewTag(emoji = "🏃", title = "운동", color = 0xFFE57373),
            )
        }
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        TagList(
            onTagClick = {},
            onRefresh = {},
            modifier = Modifier.fillMaxSize(),
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
            empty = {
                DiaryEmptyBox(
                    title = "아직 태그가 없습니다",
                    icon = { TagIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            },
        )
    }
}
