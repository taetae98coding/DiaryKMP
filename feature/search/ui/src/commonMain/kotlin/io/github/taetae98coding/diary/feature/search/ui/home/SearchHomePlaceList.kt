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
import io.github.taetae98coding.diary.compose.core.icon.PlaceIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.place.PlaceCard
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.search.ui.previewPlace
import kotlinx.coroutines.flow.flowOf

internal const val SEARCH_HOME_PLACE_LIST_TEST_TAG: String = "SearchHomePlaceList"

@Composable
internal fun SearchHomePlaceList(
    onEvent: (SearchHomeResultEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    sortSheetState: DialogState = rememberDialogState(),
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
    query: String = "",
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    SearchHomeResult(
        onEvent = onEvent,
        modifier = modifier,
        sortSheetState = sortSheetState,
        query = query,
        pagingItems = placePagingItems,
        sortProvider = sortProvider,
        emptyIcon = { PlaceIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
    ) {
        LazyColumn(
            modifier = Modifier.testTag(SEARCH_HOME_PLACE_LIST_TEST_TAG),
            state = listState,
            contentPadding = DiaryTheme.dimens.screenPaddingValues,
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        ) {
            items(
                count = placePagingItems.itemCount,
                key = placePagingItems.itemKey { place -> place.id },
            ) { index ->
                val place = placePagingItems[index]

                PlaceCard(
                    onClick = { place?.let { value -> onEvent(SearchHomeResultEvent.ClickResult(id = value.id)) } },
                    modifier =
                        Modifier
                            .animateItem()
                            .fillMaxWidth(),
                    place = place,
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun SearchHomePlaceListPreview() {
    val placePagingData =
        remember {
            flowOf(
                PagingData.from(
                    listOf(
                        previewPlace(title = "여행 숙소", color = 0xFF3A7BD5, address = "서울특별시 중구 세종대로 110"),
                        previewPlace(title = "여행 카페", color = 0xFFE57373, address = ""),
                    ),
                ),
            )
        }

    DiaryTheme {
        SearchHomePlaceList(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            placePagingItems = placePagingData.collectAsLazyPagingItems(),
        )
    }
}
