package io.github.taetae98coding.diary.feature.search.ui.home.dday

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
import io.github.taetae98coding.diary.compose.core.icon.DDayIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.dday.DDayListEvent
import io.github.taetae98coding.diary.compose.dday.DDayListState
import io.github.taetae98coding.diary.compose.dday.SwipeDDayCard
import io.github.taetae98coding.diary.compose.dday.previewDDay
import io.github.taetae98coding.diary.compose.dday.previewDDayToday
import io.github.taetae98coding.diary.compose.dday.rememberDDayListState
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResult
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultEvent
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultItemEvent
import kotlinx.coroutines.flow.flowOf

internal const val SEARCH_HOME_DDAY_LIST_TEST_TAG: String = "SearchHomeDDayList"

@Composable
internal fun SearchHomeDDayList(
    onEvent: (SearchHomeResultEvent) -> Unit,
    onItemEvent: (SearchHomeResultItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    ddayListState: DDayListState = rememberDDayListState(),
    sortSheetState: DialogState = rememberDialogState(),
    ddayPagingItems: LazyPagingItems<DDay> = remember { flowOf(PagingData.empty<DDay>()) }.collectAsLazyPagingItems(),
    query: String = "",
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    SearchHomeResult(
        onEvent = onEvent,
        modifier = modifier,
        sortSheetState = sortSheetState,
        query = query,
        pagingItems = ddayPagingItems,
        sortProvider = sortProvider,
        emptyIcon = { DDayIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
    ) {
        LazyColumn(
            modifier = Modifier.testTag(SEARCH_HOME_DDAY_LIST_TEST_TAG),
            state = listState,
            contentPadding = DiaryTheme.dimens.screenPaddingValues,
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        ) {
            items(
                count = ddayPagingItems.itemCount,
                key = ddayPagingItems.itemKey { dday -> dday.id },
            ) { index ->
                SwipeDDayCard(
                    onEvent = { event ->
                        when (event) {
                            is DDayListEvent.ClickDDay -> onItemEvent(SearchHomeResultItemEvent.Click(id = event.id))
                            is DDayListEvent.SwipeDelete -> onItemEvent(SearchHomeResultItemEvent.SwipeDelete(id = event.id))
                        }
                    },
                    modifier =
                        Modifier
                            .animateItem()
                            .fillMaxWidth(),
                    state = ddayListState,
                    dday = ddayPagingItems[index],
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun SearchHomeDDayListPreview() {
    val ddayPagingData =
        remember {
            flowOf(
                PagingData.from(
                    listOf(
                        previewDDay(title = "여행 출발"),
                        previewDDay(title = "여행 기념일", color = 0xFFE57373),
                    ),
                ),
            )
        }

    DiaryTheme {
        SearchHomeDDayList(
            onEvent = {},
            onItemEvent = {},
            modifier = Modifier.fillMaxSize(),
            ddayListState = rememberDDayListState(initialToday = previewDDayToday()),
            ddayPagingItems = ddayPagingData.collectAsLazyPagingItems(),
        )
    }
}
