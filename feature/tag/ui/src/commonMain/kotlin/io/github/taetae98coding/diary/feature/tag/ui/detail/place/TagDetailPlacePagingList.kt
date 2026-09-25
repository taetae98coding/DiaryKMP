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
import io.github.taetae98coding.diary.compose.place.SwipeToDeletePlaceCard
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_place_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_place_empty_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

internal const val TAG_DETAIL_PLACE_LIST_TEST_TAG: String = "TagDetailPlaceList"

@Composable
internal fun TagDetailPlacePagingList(
    onEvent: (TagDetailPlaceContentEvent) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
    isRefreshingProvider: () -> Boolean = { false },
    sortProvider: () -> ListSort = { ListSort.TITLE },
    scopeProvider: () -> TagScope = { TagScope.SELF },
) {
    ListQueryScrollEffect(
        staggeredGridState = gridState,
        sortProvider = sortProvider,
        filterProvider = scopeProvider,
        itemListProvider = { placePagingItems.itemSnapshotList.items },
    )

    DiaryCrossfade(
        targetState = placePagingItems.isLoadedEmpty(),
        modifier = modifier,
    ) { isEmpty ->
        if (isEmpty) {
            DiaryPullToRefreshBox(
                isRefreshingProvider = isRefreshingProvider,
                onRefresh = { onEvent(TagDetailPlaceContentEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                DiaryEmptyBox(
                    title = stringResource(Res.string.tag_detail_place_empty_title),
                    // 빈 상태에서도 당겨서 새로고침할 수 있도록 중첩 스크롤을 전달한다.
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    description = stringResource(Res.string.tag_detail_place_empty_description),
                    icon = { PlaceIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
                )
            }
        } else {
            DiaryRefreshableStaggeredGrid(
                onRefresh = { onEvent(TagDetailPlaceContentEvent.Refresh) },
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                isRefreshingProvider = isRefreshingProvider,
                listTestTag = TAG_DETAIL_PLACE_LIST_TEST_TAG,
            ) {
                items(
                    count = placePagingItems.itemCount,
                    key = placePagingItems.itemKey { place -> place.id },
                ) { index ->
                    val place = placePagingItems[index]

                    SwipeToDeletePlaceCard(
                        onClick = { place?.let { value -> onEvent(TagDetailPlaceContentEvent.ClickPlace(id = value.id)) } },
                        onDelete = { place?.let { value -> onEvent(TagDetailPlaceContentEvent.DeletePlace(id = value.id)) } },
                        modifier = Modifier.animateItem(),
                        place = place,
                    )
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun TagDetailPlacePagingListPreview() {
    DiaryTheme {
        Surface {
            TagDetailPlacePagingList(
                onEvent = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
