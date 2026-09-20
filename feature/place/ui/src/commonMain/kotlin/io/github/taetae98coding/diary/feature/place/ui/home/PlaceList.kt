package io.github.taetae98coding.diary.feature.place.ui.home

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
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.MapIcon
import io.github.taetae98coding.diary.compose.core.layout.DiaryRefreshableStaggeredGrid
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.list.sort.DiaryListSortBar
import io.github.taetae98coding.diary.compose.place.PlaceCard
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.place_home_map_empty_description
import io.github.taetae98coding.diary.feature.place.ui.place_home_map_empty_title
import io.github.taetae98coding.diary.feature.place.ui.previewPlace
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaceList(
    onEvent: (PlaceHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    placeListUiStateProvider: () -> PlaceHomePlaceListUiState = { PlaceHomePlaceListUiState() },
    isRefreshingProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.TITLE },
) {
    ListQueryScrollEffect(
        staggeredGridState = gridState,
        sortProvider = sortProvider,
        itemListProvider = { placeListUiStateProvider().placeList },
    )

    val uiState = placeListUiStateProvider()

    Column(modifier = modifier) {
        DiaryListSortBar(
            onClick = { onEvent(PlaceHomeScaffoldEvent.ClickSort) },
            modifier = Modifier.fillMaxWidth(),
            sortProvider = sortProvider,
        )

        DiaryCrossfade(
            targetState = uiState,
            modifier = Modifier.fillMaxSize(),
            contentKey = { state -> state.isEmpty },
        ) { state ->
            if (state.isEmpty) {
                DiaryPullToRefreshBox(
                    isRefreshingProvider = isRefreshingProvider,
                    onRefresh = { onEvent(PlaceHomeScaffoldEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    DiaryEmptyBox(
                        title = stringResource(Res.string.place_home_map_empty_title),
                        // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        description = stringResource(Res.string.place_home_map_empty_description),
                        icon = { MapIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                    )
                }
            } else {
                DiaryRefreshableStaggeredGrid(
                    onRefresh = { onEvent(PlaceHomeScaffoldEvent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                    isRefreshingProvider = isRefreshingProvider,
                ) {
                    items(
                        items = state.placeList,
                        key = { place -> place.id },
                    ) { place ->
                        PlaceCard(
                            onClick = { onEvent(PlaceHomeScaffoldEvent.ClickPlace(id = place.id)) },
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
private fun PlaceListPreview() {
    val placeList =
        remember {
            listOf(
                previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780),
                previewPlace(title = "회사", color = 0xFFE57373, latitude = 37.3595, longitude = 127.1052),
            )
        }

    DiaryTheme {
        PlaceList(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            placeListUiStateProvider = { PlaceHomePlaceListUiState(isLoaded = true, placeList = placeList) },
        )
    }
}
