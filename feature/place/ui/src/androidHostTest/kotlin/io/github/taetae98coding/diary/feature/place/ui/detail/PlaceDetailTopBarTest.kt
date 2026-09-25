package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceDetailTopBarTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-FEATURE-049 지도가 표시되면 상단 바에서 장소 검색을 제공한다`() {
        composeRule.setContent {
            DiaryTheme {
                PlaceDetailTopBar(
                    onEvent = {},
                    uiStateProvider = { content().copy(defaultProvider = MapProvider.NAVER) },
                )
            }
        }

        composeRule.onNodeWithContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-002 지도가 표시되지 않으면 검색을 제공하지 않는다`() {
        composeRule.setPlaceDetailScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }
}
