package io.github.taetae98coding.diary.compose.memo

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.swipe.SwipeFinishAction

private const val DATE_HEADER_CONTENT_TYPE = "MemoDateHeader"
private const val CONTENT_CONTENT_TYPE = "MemoCard"

internal fun LazyListScope.memoItems(
    state: MemoListState,
    memoPagingItems: LazyPagingItems<MemoListItem>,
    onEvent: (MemoListEvent) -> Unit,
    finishAction: SwipeFinishAction,
) {
    val itemKey = memoPagingItems.itemKey { item -> item.key() }

    repeat(memoPagingItems.itemCount) { index ->
        when (val item = memoPagingItems.peek(index)) {
            is MemoListItem.DateHeader ->
                stickyHeader(
                    key = itemKey(index),
                    contentType = DATE_HEADER_CONTENT_TYPE,
                ) {
                    MemoDateHeader(
                        date = item.date,
                        state = state,
                        // sticky header의 위치는 LazyColumn이 pin 오프셋으로 직접 제어하므로 placement 애니메이션은 두지 않는다.
                        modifier =
                            Modifier
                                .animateItem(placementSpec = null)
                                .fillMaxWidth(),
                    )
                }

            else ->
                item(
                    key = itemKey(index),
                    contentType = CONTENT_CONTENT_TYPE,
                ) {
                    val memo = (memoPagingItems[index] as? MemoListItem.Content)?.memo

                    SwipeMemoCard(
                        onEvent = onEvent,
                        memo = memo,
                        modifier =
                            Modifier
                                .animateItem()
                                .fillMaxWidth(),
                        finishAction = finishAction,
                    )
                }
        }
    }
}

private fun MemoListItem.key(): Any =
    when (this) {
        is MemoListItem.DateHeader -> date
        is MemoListItem.Content -> memo.id
    }
