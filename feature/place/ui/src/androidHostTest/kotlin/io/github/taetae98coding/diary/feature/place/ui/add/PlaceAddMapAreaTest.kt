package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceAddMapAreaTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-d754884d-a7f4-414b-b145-940491fcdad9 지도가 표시되지 않으면 검색을 제공하지 않는다`() {
        composeRule.setPlaceAddScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule
            .onAllNodes(hasContentDescription(DEFAULT_SEARCH_BUTTON_DESCRIPTION))
            .fetchSemanticsNodes()
            .size shouldBe 0
    }

    @Test
    fun `TC-324ffa89-7d94-4c54-b19a-93741f9ae256 기본 지도를 확인하지 못하면 지도 영역과 로딩 안내 없이 입력 영역만 표시한다`() {
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
    fun `TC-324ffa89-7d94-4c54-b19a-93741f9ae256 지도를 표시하지 않아도 좌표 입력과 추가를 사용할 수 있다`() {
        composeRule.setPlaceAddScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule
            .onAllNodes(hasSetTextAction())
            .fetchSemanticsNodes()
            .size shouldBe COORDINATE_INPUT_COUNT
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-76878263-9dc3-41c5-a746-494460c5df31 지도가 표시되지 않아도 좌표를 직접 입력해 추가를 실행한다`() {
        val effect = Channel<PlaceAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        every { viewModel.add(any(), tagIdSet = any()) } answers { effect.trySend(PlaceAddEffect.AddSucceeded(id = Uuid.random())).getOrThrow() }

        composeRule.setPlaceAddScreen(viewModel = viewModel)
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction())[TITLE_INDEX].performTextInput(TYPED_TITLE)
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].performTextInput(TYPED_LATITUDE)
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].performTextInput(TYPED_LONGITUDE)
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

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
        composeRule.onNodeWithText(ADD_SUCCEEDED_MESSAGE).assertExists()
    }

    public companion object {
        private const val TITLE_INDEX = 0
        private const val COORDINATE_INPUT_COUNT = 5
        private const val DEFAULT_MAP_DESCRIPTION = "Place location map"
        private const val DEFAULT_MAP_PROVIDER_DESCRIPTION = "Map provider"
        private const val ADD_SUCCEEDED_MESSAGE = "Place added."
    }
}
