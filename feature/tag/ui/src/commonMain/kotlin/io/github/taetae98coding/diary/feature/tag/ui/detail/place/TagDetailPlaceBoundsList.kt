package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
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
import io.github.taetae98coding.diary.compose.place.PlaceCard
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.previewPlace
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_place_map_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_place_map_empty_title
import org.jetbrains.compose.resources.stringResource

internal const val TAG_DETAIL_PLACE_BOUNDS_LIST_TEST_TAG: String = "TagDetailPlaceBoundsList"

@Composable
internal fun TagDetailPlaceBoundsList(
    onEvent: (TagDetailPlaceContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    placeListUiStateProvider: () -> TagDetailPlaceListUiState = { TagDetailPlaceListUiState() },
    isRefreshingProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.TITLE },
    scopeProvider: () -> TagScope = { TagScope.SELF },
) {
    ListQueryScrollEffect(
        staggeredGridState = gridState,
        sortProvider = sortProvider,
        filterProvider = scopeProvider,
        itemListProvider = { placeListUiStateProvider().placeList },
    )

    DiaryCrossfade(
        targetState = placeListUiStateProvider(),
        modifier = modifier,
        contentKey = { state -> state.isEmpty },
    ) { state ->
        if (state.isEmpty) {
            DiaryPullToRefreshBox(
                isRefreshingProvider = isRefreshingProvider,
                onRefresh = { onEvent(TagDetailPlaceContentEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                DiaryEmptyBox(
                    title = stringResource(Res.string.tag_detail_place_map_empty_title),
                    // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    description = stringResource(Res.string.tag_detail_place_map_empty_description),
                    icon = { MapIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            }
        } else {
            DiaryRefreshableStaggeredGrid(
                onRefresh = { onEvent(TagDetailPlaceContentEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                isRefreshingProvider = isRefreshingProvider,
                listTestTag = TAG_DETAIL_PLACE_BOUNDS_LIST_TEST_TAG,
            ) {
                items(
                    items = state.placeList,
                    key = { place -> place.id },
                ) { place ->
                    PlaceCard(
                        onClick = { onEvent(TagDetailPlaceContentEvent.ClickPlace(id = place.id)) },
                        place = place,
                    )
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun TagDetailPlaceBoundsListPreview() {
    val placeList = remember { listOf(previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780)) }

    DiaryTheme {
        Surface {
            TagDetailPlaceBoundsList(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
                placeListUiStateProvider = { TagDetailPlaceListUiState(isLoaded = true, placeList = placeList) },
            )
        }
    }
}
