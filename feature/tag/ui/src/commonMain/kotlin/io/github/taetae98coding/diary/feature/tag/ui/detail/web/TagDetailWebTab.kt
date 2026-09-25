package io.github.taetae98coding.diary.feature.tag.ui.detail.web

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.WebIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBarHost
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBottomSheetHost
import io.github.taetae98coding.diary.compose.web.WebCard
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_web_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_web_empty_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val TAG_DETAIL_WEB_LIST_TEST_TAG: String = "TagDetailWebList"

@Composable
internal fun TagDetailWebTab(
    onEvent: (TagDetailWebContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    sortSheetState: DialogState = rememberDialogState(),
    gridState: LazyGridState = rememberLazyGridState(),
    webPagingItems: LazyPagingItems<Web> = remember { flowOf(PagingData.empty<Web>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    ListQueryScrollEffect(
        gridState = gridState,
        sortProvider = sortProvider,
        itemListProvider = { webPagingItems.itemSnapshotList.items },
    )

    Column(modifier = modifier) {
        DiaryListSortBarHost(
            onClick = { onEvent(TagDetailWebContentEvent.ClickSort) },
            modifier = Modifier.fillMaxWidth(),
            sortProvider = sortProvider,
            isSortVisibleProvider = { webPagingItems.itemCount > 0 },
        )

        DiaryCrossfade(
            targetState = webPagingItems.isLoadedEmpty(),
            modifier = Modifier.fillMaxSize(),
        ) { isEmpty ->
            if (isEmpty) {
                Empty(
                    onEvent = onEvent,
                    isRefreshingProvider = isRefreshingProvider,
                )
            } else {
                DiaryRefreshableGrid(
                    onRefresh = { onEvent(TagDetailWebContentEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                    isRefreshingProvider = isRefreshingProvider,
                    listTestTag = TAG_DETAIL_WEB_LIST_TEST_TAG,
                ) {
                    items(
                        count = webPagingItems.itemCount,
                        key = webPagingItems.itemKey { web -> web.id },
                    ) { index ->
                        val web = webPagingItems[index]

                        WebCard(
                            onClick = { web?.let { value -> onEvent(TagDetailWebContentEvent.ClickWeb(id = value.id)) } },
                            modifier =
                                Modifier
                                    .animateItem()
                                    .fillMaxWidth(),
                            web = web,
                        )
                    }
                }
            }
        }
    }

    DiaryListSortBottomSheetHost(
        onSelect = { sort -> onEvent(TagDetailWebContentEvent.SelectSort(sort = sort)) },
        state = sortSheetState,
        sortProvider = sortProvider,
    )
}

@Composable
private fun Empty(
    onEvent: (TagDetailWebContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    isRefreshingProvider: () -> Boolean = { false },
) {
    DiaryPullToRefreshBox(
        isRefreshingProvider = isRefreshingProvider,
        onRefresh = { onEvent(TagDetailWebContentEvent.Refresh) },
        modifier = modifier.fillMaxSize(),
    ) {
        DiaryEmptyBox(
            title = stringResource(Res.string.tag_detail_web_empty_title),
            // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            description = stringResource(Res.string.tag_detail_web_empty_description),
            icon = { WebIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
        )
    }
}

@ScreenPreview
@Composable
private fun TagDetailWebTabPreview() {
    DiaryTheme {
        Surface {
            TagDetailWebTab(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
