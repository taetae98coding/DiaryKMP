package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceAddMapAreaTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-SEARCH-DIALOG-FEATURE-002 지도가 표시되지 않으면 검색을 제공하지 않는다`() {
        composeRule.setPlaceAddScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-024 기본 지도를 확인하지 못하면 지도 영역과 로딩 안내 없이 입력 영역만 표시한다`() {
        composeRule.setPlaceAddScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule
            .onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .fetchSemanticsNodes()
            .size shouldBe 0

        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_MAP_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_MAP_PROVIDER_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-024 지도를 표시하지 않아도 좌표 입력과 추가를 사용할 수 있다`() {
        composeRule.setPlaceAddScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule
            .onAllNodes(hasSetTextAction())
            .fetchSemanticsNodes()
            .size shouldBe COORDINATE_INPUT_COUNT
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-023 지도가 표시되지 않아도 좌표를 직접 입력해 추가를 실행한다`() {
        val viewModel = screenTestViewModel()
        every { viewModel.add(any(), tagIdSet = any()) } returns Unit

        composeRule.setPlaceAddScreen(viewModel = viewModel)
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction())[TITLE_INDEX].performTextInput(TYPED_TITLE)
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].performTextInput(TYPED_LATITUDE)
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].performTextInput(TYPED_LONGITUDE)
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()

        verify(exactly = 1) {
            viewModel.add(
                match { detail ->
                    detail.title == TYPED_TITLE &&
                        detail.coordinate.latitude == TYPED_LATITUDE.toDouble() &&
                        detail.coordinate.longitude == TYPED_LONGITUDE.toDouble()
                },
                tagIdSet = emptySet(),
            )
        }
    }

    public companion object {
        private const val TITLE_INDEX = 0
        private const val COORDINATE_INPUT_COUNT = 5
        private const val DEFAULT_MAP_DESCRIPTION = "Place location map"
        private const val DEFAULT_MAP_PROVIDER_DESCRIPTION = "Map provider"
    }
}
