package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
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
class PlaceAddScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-ADD-FEATURE-031 진입하면 제목 입력 칸에 자동으로 초점이 맞춰진다`() {
        composeRule.setPlaceAddScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assertIsFocused()
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-015 뒤로가기 버튼을 선택하면 이전 화면으로 돌아가는 행동을 실행한다`() {
        var navigateUpCount = 0
        composeRule.setPlaceAddScreen(
            viewModel = screenTestViewModel(),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-004 추가 버튼으로 성공하면 입력을 초기화하고 다음 컬러와 제목 초점을 표시한다`() {
        assertSuccessResetsInput {
            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        }
    }

    @Test
    fun `TC-PLACE-ADD-FEATURE-004 단축키로 성공하면 입력을 초기화하고 다음 컬러와 제목 초점을 표시한다`() {
        assertSuccessResetsInput {
            composeRule.onAllNodes(hasSetTextAction()).onFirst().performKeyInput {
                keyDown(Key.MetaLeft)
                keyDown(Key.Enter)
                keyUp(Key.Enter)
                keyUp(Key.MetaLeft)
            }
        }
    }

    private fun assertSuccessResetsInput(triggerAdd: () -> Unit) {
        val effect = Channel<PlaceAddEffect>(capacity = Channel.BUFFERED)
        val viewModel = screenTestViewModel(effect = effect.receiveAsFlow())
        every { viewModel.add(any(), tagIdSet = any()) } answers { effect.trySend(PlaceAddEffect.AddSucceeded(id = Uuid.random())).getOrThrow() }
        composeRule.setPlaceAddScreen(viewModel = viewModel)

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].performTextInput(TYPED_DESCRIPTION)
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].performTextInput(TYPED_ADDRESS)
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].performTextInput(TYPED_LATITUDE)
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].performTextInput(TYPED_LONGITUDE)

        triggerAdd()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.add(any(), tagIdSet = any()) }
        composeRule.onNodeWithText(TYPED_TITLE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].assert(hasText(""))
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].assert(hasText(""))
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].assert(hasText(""))
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].assert(hasText(""))
        composeRule.onNode(hasHexText() and hasClickAction()).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assertIsFocused()
    }
}
