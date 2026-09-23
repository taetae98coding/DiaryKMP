package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.detail.form.TagDetailFormTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.memo.TagDetailMemoTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.place.TagDetailPlaceTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTabState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.rememberTagDetailTabState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.tagDetailTabList
import io.github.taetae98coding.diary.feature.tag.ui.detail.web.TagDetailWebTab

internal const val TAG_DETAIL_PAGER_TEST_TAG: String = "TagDetailPager"

@Composable
internal fun TagDetailPager(
    modifier: Modifier = Modifier,
    state: TagDetailTabState = rememberTagDetailTabState(),
    tabContent: @Composable (TagDetailTab) -> Unit,
) {
    HorizontalPager(
        state = state.pagerState,
        modifier = modifier.testTag(TAG_DETAIL_PAGER_TEST_TAG),
    ) { page ->
        tabContent(tagDetailTabList[page])
    }
}

@ScreenPreview
@Composable
private fun TagDetailPagerPreview() {
    DiaryTheme {
        TagDetailPager(modifier = Modifier.fillMaxSize()) { tab ->
            when (tab) {
                TagDetailTab.DETAIL -> TagDetailFormTab(onEvent = {}, modifier = Modifier.fillMaxSize())
                TagDetailTab.MEMO -> TagDetailMemoTab(onEvent = {}, onMemoListEvent = {}, modifier = Modifier.fillMaxSize())
                TagDetailTab.WEB -> TagDetailWebTab(onEvent = {}, modifier = Modifier.fillMaxSize())
                TagDetailTab.PLACE -> TagDetailPlaceTab(onEvent = {}, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
