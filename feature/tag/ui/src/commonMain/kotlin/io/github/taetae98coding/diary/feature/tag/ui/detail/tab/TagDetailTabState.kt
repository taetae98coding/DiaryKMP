package io.github.taetae98coding.diary.feature.tag.ui.detail.tab

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Stable
internal class TagDetailTabState(
    val pagerState: PagerState,
) {
    val tab: TagDetailTab
        get() = tagDetailTabList[pagerState.currentPage]

    suspend fun select(tab: TagDetailTab) {
        pagerState.animateScrollToPage(tagDetailTabList.indexOf(tab))
    }
}

@Composable
internal fun rememberTagDetailTabState(initialTab: TagDetailTab = TagDetailTab.DETAIL): TagDetailTabState {
    val pagerState = rememberPagerState(initialPage = tagDetailTabList.indexOf(initialTab)) { tagDetailTabList.size }

    return remember(pagerState) { TagDetailTabState(pagerState = pagerState) }
}
