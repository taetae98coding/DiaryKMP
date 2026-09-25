package io.github.taetae98coding.diary.feature.place.ui.home.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.PlaceIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableStaggeredGrid
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBarHost
import io.github.taetae98coding.diary.compose.place.PlaceCard
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeScaffoldEvent
import io.github.taetae98coding.diary.feature.place.ui.place_home_list_empty_description
import io.github.taetae98coding.diary.feature.place.ui.place_home_list_empty_title
import io.github.taetae98coding.diary.feature.place.ui.previewPlace
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val PLACE_HOME_PAGING_LIST_TEST_TAG: String = "PlaceHomePagingList"

@Composable
internal fun PlaceHomePagingList(
    onEvent: (PlaceHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    ListQueryScrollEffect(
        staggeredGridState = gridState,
        sortProvider = sortProvider,
        itemListProvider = { placePagingItems.itemSnapshotList.items },
    )

    Column(modifier = modifier) {
        DiaryListSortBarHost(
            onClick = { onEvent(PlaceHomeScaffoldEvent.ClickSort) },
            modifier = Modifier.fillMaxWidth(),
            sortProvider = sortProvider,
            isSortVisibleProvider = { placePagingItems.itemCount > 0 },
        )

        DiaryCrossfade(
            targetState = placePagingItems.isLoadedEmpty(),
            modifier = Modifier.fillMaxSize(),
        ) { isEmpty ->
            if (isEmpty) {
                DiaryPullToRefreshBox(
                    isRefreshingProvider = isRefreshingProvider,
                    onRefresh = { onEvent(PlaceHomeScaffoldEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    DiaryEmptyBox(
                        title = stringResource(Res.string.place_home_list_empty_title),
                        // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        description = stringResource(Res.string.place_home_list_empty_description),
                        icon = { PlaceIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                    )
                }
            } else {
                DiaryRefreshableStaggeredGrid(
                    onRefresh = { onEvent(PlaceHomeScaffoldEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                    isRefreshingProvider = isRefreshingProvider,
                    listTestTag = PLACE_HOME_PAGING_LIST_TEST_TAG,
                ) {
                    items(
                        count = placePagingItems.itemCount,
                        key = placePagingItems.itemKey { place -> place.id },
                    ) { index ->
                        val place = placePagingItems[index]

                        PlaceCard(
                            onClick = { place?.let { value -> onEvent(PlaceHomeScaffoldEvent.ClickPlace(id = value.id)) } },
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
    }
}

@ScreenPreview
@Composable
private fun PlaceHomePagingListPreview() {
    val placeList =
        remember {
            listOf(
                previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780),
                previewPlace(title = "회사", color = 0xFFE57373, latitude = 37.3595, longitude = 127.1052),
            )
        }
    val placePagingData = remember(placeList) { flowOf(PagingData.from(placeList)) }

    DiaryTheme {
        PlaceHomePagingList(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            placePagingItems = placePagingData.collectAsLazyPagingItems(),
        )
    }
}
