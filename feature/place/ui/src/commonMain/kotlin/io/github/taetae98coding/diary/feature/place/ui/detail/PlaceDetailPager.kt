package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.place.ui.PREVIEW_PLACE_DETAIL
import io.github.taetae98coding.diary.feature.place.ui.detail.memo.PlaceDetailMemoTab
import io.github.taetae98coding.diary.feature.place.ui.detail.tab.PlaceDetailTab
import io.github.taetae98coding.diary.feature.place.ui.detail.tab.PlaceDetailTabState
import io.github.taetae98coding.diary.feature.place.ui.detail.tab.placeDetailTabList
import io.github.taetae98coding.diary.feature.place.ui.detail.tab.rememberPlaceDetailTabState
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceDetailFormState

internal const val PLACE_DETAIL_PAGER_TEST_TAG: String = "PlaceDetailPager"

@Composable
internal fun PlaceDetailPager(
    modifier: Modifier = Modifier,
    state: PlaceDetailTabState = rememberPlaceDetailTabState(),
    tabContent: @Composable (PlaceDetailTab) -> Unit,
) {
    // 장소 디테일 탭의 지도가 좌우 끌기를 받으므로 스와이프로는 탭을 전환하지 않는다.
    HorizontalPager(
        state = state.pagerState,
        modifier = modifier.testTag(PLACE_DETAIL_PAGER_TEST_TAG),
        userScrollEnabled = false,
    ) { page ->
        tabContent(placeDetailTabList[page])
    }
}

@ScreenPreview
@Composable
private fun PlaceDetailPagerPreview() {
    DiaryTheme {
        PlaceDetailPager(modifier = Modifier.fillMaxSize()) { tab ->
            when (tab) {
                PlaceDetailTab.DETAIL ->
                    PlaceDetailScaffoldContent(
                        onFormEvent = {},
                        state = rememberPlaceDetailFormState(initialDetail = PREVIEW_PLACE_DETAIL),
                        modifier = Modifier.fillMaxSize(),
                    )

                PlaceDetailTab.MEMO -> PlaceDetailMemoTab(onEvent = {}, onMemoListEvent = {}, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
