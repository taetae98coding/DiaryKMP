package io.github.taetae98coding.diary.feature.search.ui.home.memo

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
import io.github.taetae98coding.diary.compose.core.icon.MemoIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.MemoCard
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResult
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultEvent
import io.github.taetae98coding.diary.feature.search.ui.previewMemo
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

internal const val SEARCH_HOME_MEMO_LIST_TEST_TAG: String = "SearchHomeMemoList"

@Composable
internal fun SearchHomeMemoList(
    onEvent: (SearchHomeResultEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    sortSheetState: DialogState = rememberDialogState(),
    memoPagingItems: LazyPagingItems<Memo> = remember { flowOf(PagingData.empty<Memo>()) }.collectAsLazyPagingItems(),
    query: String = "",
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    SearchHomeResult(
        onEvent = onEvent,
        modifier = modifier,
        sortSheetState = sortSheetState,
        query = query,
        pagingItems = memoPagingItems,
        sortProvider = sortProvider,
        emptyIcon = { MemoIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
    ) {
        LazyColumn(
            modifier = Modifier.testTag(SEARCH_HOME_MEMO_LIST_TEST_TAG),
            state = listState,
            contentPadding = DiaryTheme.dimens.screenPaddingValues,
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        ) {
            items(
                count = memoPagingItems.itemCount,
                key = memoPagingItems.itemKey { memo -> memo.id },
            ) { index ->
                val memo = memoPagingItems[index]

                MemoCard(
                    onClick = { memo?.let { value -> onEvent(SearchHomeResultEvent.ClickResult(id = value.id)) } },
                    modifier =
                        Modifier
                            .animateItem()
                            .fillMaxWidth(),
                    memo = memo,
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun SearchHomeMemoListPreview() {
    val memoPagingData =
        remember {
            flowOf(
                PagingData.from(
                    listOf(
                        previewMemo(
                            title = "여름 여행 계획",
                            color = 0xFF3A7BD5,
                            dateTime =
                                MemoDateTime.AllDay(
                                    dateRange =
                                        LocalDate(year = 2026, month = Month.AUGUST, day = 1)..LocalDate(year = 2026, month = Month.AUGUST, day = 5),
                                ),
                        ),
                        previewMemo(title = "여행 준비물", color = 0xFFE57373),
                    ),
                ),
            )
        }

    DiaryTheme {
        SearchHomeMemoList(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            memoPagingItems = memoPagingData.collectAsLazyPagingItems(),
        )
    }
}
