package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.mockk.every
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
class PlaceAddScreenFocusTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-4478dc0a-1225-4746-9f6f-10494ba8a7a2 추가에 성공하면 제목 입력으로 초점을 옮긴다`() {
        setPlaceAddScreen(effect = PlaceAddEffect.AddSucceeded(id = Uuid.random()))

        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].performTextInput(TYPED_ADDRESS)
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].assertIsFocused()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assertIsFocused()
    }

    @Test
    fun `TC-db0b4474-7921-4e83-8d6c-a2e2845dfd00 제목 미입력으로 추가하면 제목 입력으로 초점을 옮긴다`() {
        setPlaceAddScreen(effect = PlaceAddEffect.TitleBlank)

        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].performTextInput(TYPED_LONGITUDE)
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].assertIsFocused()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assertIsFocused()
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].assertIsNotFocused()
    }

    @Test
    fun `TC-00d98bf0-91af-4e73-ac19-e14005d297eb 좌표 오류로 추가해도 초점을 옮기지 않는다`() {
        setPlaceAddScreen(effect = PlaceAddEffect.CoordinateInvalid)

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].performTextInput(TYPED_ADDRESS)
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].assertIsFocused()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].assertIsFocused()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assertIsNotFocused()
    }

    private fun setPlaceAddScreen(effect: PlaceAddEffect) {
        val effectChannel = Channel<PlaceAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effectChannel.receiveAsFlow())
        every { viewModel.add(any(), tagIdSet = any()) } answers { effectChannel.trySend(effect).getOrThrow() }
        composeRule.setPlaceAddScreen(viewModel = viewModel)
    }
}
