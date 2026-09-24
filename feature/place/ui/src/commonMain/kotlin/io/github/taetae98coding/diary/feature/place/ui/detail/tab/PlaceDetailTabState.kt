package io.github.taetae98coding.diary.feature.place.ui.detail.tab

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Stable
internal class PlaceDetailTabState(
    val pagerState: PagerState,
) {
    val tab: PlaceDetailTab
        get() = placeDetailTabList[pagerState.currentPage]

    suspend fun select(tab: PlaceDetailTab) {
        pagerState.animateScrollToPage(placeDetailTabList.indexOf(tab))
    }
}

@Composable
internal fun rememberPlaceDetailTabState(initialTab: PlaceDetailTab = PlaceDetailTab.DETAIL): PlaceDetailTabState {
    val pagerState = rememberPagerState(initialPage = placeDetailTabList.indexOf(initialTab)) { placeDetailTabList.size }

    return remember(pagerState) { PlaceDetailTabState(pagerState = pagerState) }
}
