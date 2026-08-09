package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.place.ui.TEST_TAG_ADD_REQUEST_KEY
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceAddScreenRetentionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-ADD-FEATURE-007 제목이 비어 있으면 설명과 좌표를 유지하고 제목에 다시 초점을 맞춘다`() {
        assertTitleBlankRetainsInput(initialTitle = "")
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-007 제목이 공백이면 모든 입력을 유지하고 제목에 다시 초점을 맞춘다`() {
        assertTitleBlankRetainsInput(initialTitle = WHITESPACE_TITLE)
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-010 좌표를 읽을 수 없으면 작성 내용을 유지한다`() {
        assertCoordinateFailureRetainsInput(latitude = INVALID_LATITUDE)
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-010 좌표가 범위를 벗어나도 작성 내용을 유지한다`() {
        assertCoordinateFailureRetainsInput(latitude = OUT_OF_RANGE_LATITUDE)
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-013 화면이 재생성되어도 제목 설명 좌표를 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val viewModel = screenTestViewModel()
        restorationTester.setContent {
            PlaceAddScreenTestTheme {
                PlaceAddScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    initialCoordinate = null,
                    addViewModel = viewModel,
                    searchViewModel = searchScreenTestViewModel(),
                    navigateToTagDetail = {},
                    tagViewModel = addTagScreenTestViewModel(),
                )
            }
        }

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].performTextInput(TYPED_DESCRIPTION)
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].performTextInput(TYPED_ADDRESS)
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].performTextInput(TYPED_LATITUDE)
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].performTextInput(TYPED_LONGITUDE)
        val initialColorHex = composeRule.currentColorHex()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TYPED_TITLE).assertExists()
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].assert(hasText(TYPED_DESCRIPTION))
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].assert(hasText(TYPED_ADDRESS))
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].assert(hasText(TYPED_LATITUDE))
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].assert(hasText(TYPED_LONGITUDE))
        composeRule.onNodeWithText(initialColorHex, substring = true).assertExists()
    }

    private fun assertTitleBlankRetainsInput(initialTitle: String) {
        val effect = Channel<PlaceAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        every { viewModel.add(any(), tagIdSet = any()) } answers { effect.trySend(PlaceAddEffect.TitleBlank).getOrThrow() }
        composeRule.setPlaceAddScreen(viewModel = viewModel)

        if (initialTitle.isNotEmpty()) {
            composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(initialTitle)
        }
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].performTextInput(TYPED_DESCRIPTION)
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].performTextInput(TYPED_ADDRESS)
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].performTextInput(TYPED_LATITUDE)
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].performTextInput(TYPED_LONGITUDE)
        val initialColorHex = composeRule.currentColorHex()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.add(any(), tagIdSet = any()) }
        composeRule
            .onAllNodes(hasSetTextAction())
            .onFirst()
            .assert(hasText(initialTitle))
            .assertIsFocused()
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].assert(hasText(TYPED_DESCRIPTION))
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].assert(hasText(TYPED_ADDRESS))
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].assert(hasText(TYPED_LATITUDE))
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].assert(hasText(TYPED_LONGITUDE))
        composeRule.onNodeWithText(initialColorHex, substring = true).assertExists()
    }

    private fun assertCoordinateFailureRetainsInput(latitude: String) {
        val effectChannel = Channel<PlaceAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effectChannel.receiveAsFlow())
        every { viewModel.add(any(), tagIdSet = any()) } answers { effectChannel.trySend(PlaceAddEffect.CoordinateInvalid).getOrThrow() }
        composeRule.setPlaceAddScreen(viewModel = viewModel)

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].performTextInput(TYPED_DESCRIPTION)
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].performTextInput(latitude)
        val initialColorHex = composeRule.currentColorHex()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.add(any(), tagIdSet = any()) }
        composeRule.onNodeWithText(TYPED_TITLE).assertExists()
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].assert(hasText(TYPED_DESCRIPTION))
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].assert(hasText(latitude))
        composeRule.onNodeWithText(initialColorHex, substring = true).assertExists()
    }

    public companion object {
        private const val WHITESPACE_TITLE = "   "
        private const val INVALID_LATITUDE = "abc"
        private const val OUT_OF_RANGE_LATITUDE = "90.1"
    }
}
