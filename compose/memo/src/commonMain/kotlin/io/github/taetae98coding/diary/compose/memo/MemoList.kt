package io.github.taetae98coding.diary.compose.memo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.swipe.SwipeFinishAction
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.list.ListSort
import kotlinx.coroutines.flow.flowOf

public const val MEMO_LIST_TEST_TAG: String = "MemoList"

@Composable
public fun MemoList(
    onEvent: (MemoListEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: MemoListState = rememberMemoListState(),
    listState: LazyListState = rememberLazyListState(),
    memoPagingItems: LazyPagingItems<MemoListItem> = remember { flowOf(PagingData.empty<MemoListItem>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoListUiState = { MemoListUiState() },
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    filterProvider: () -> Any? = { Unit },
    listTestTag: String = MEMO_LIST_TEST_TAG,
    finishAction: SwipeFinishAction = SwipeFinishAction.FINISH,
    empty: @Composable () -> Unit,
) {
    ListQueryScrollEffect(
        listState = listState,
        sortProvider = sortProvider,
        filterProvider = filterProvider,
        itemListProvider = { memoPagingItems.itemSnapshotList.items },
    )

    DiaryPullToRefreshBox(
        isRefreshingProvider = { uiStateProvider().isRefreshing },
        onRefresh = { onEvent(MemoListEvent.Refresh) },
        modifier = modifier,
    ) {
        DiaryCrossfade(
            targetState = memoPagingItems.isLoadedEmpty(),
            modifier = Modifier.fillMaxSize(),
        ) { isEmpty ->
            if (isEmpty) {
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
            } else {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .testTag(listTestTag),
                    state = listState,
                    contentPadding = DiaryTheme.dimens.screenPaddingValues,
                    verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
                ) {
                    memoItems(
                        state = state,
                        memoPagingItems = memoPagingItems,
                        onEvent = onEvent,
                        finishAction = finishAction,
                    )
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun MemoListPreview() {
    val memoPagingData =
        remember {
            flowOf(
                PagingData.from(
                    listOf<MemoListItem>(
                        MemoListItem.Content(memo = previewMemo(title = "메모 제목", color = 0xFF3A7BD5)),
                        MemoListItem.Content(memo = previewMemo(title = "다른 메모", color = 0xFFE57373)),
                    ),
                ),
            )
        }

    DiaryTheme {
        MemoList(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            memoPagingItems = memoPagingData.collectAsLazyPagingItems(),
            empty = {
                DiaryEmptyBox(
                    title = "아직 메모가 없습니다",
                    icon = { MemoIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            },
        )
    }
}
