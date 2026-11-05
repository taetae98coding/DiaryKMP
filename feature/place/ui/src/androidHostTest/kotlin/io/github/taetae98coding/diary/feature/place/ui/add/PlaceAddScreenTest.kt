package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.slot
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
    fun `TC-1a8e068d-a28a-4012-a5ba-997513dddf0d 진입하면 제목 입력 칸에 자동으로 초점이 맞춰진다`() {
        composeRule.setPlaceAddScreen(viewModel = screenTestViewModel())

        composeRule.waitForIdle()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().assertIsFocused()
    }

    @Test
    fun `TC-7e2d31c7-b8db-4c66-96de-3afeab9a475e 처음 표시되면 컬러가 제시된다`() {
        composeRule.setPlaceAddScreen(viewModel = screenTestViewModel())

        composeRule.currentColorHex().length shouldBe HEX_COLOR_LENGTH
    }

    @Test
    fun `TC-834ba537-b7ac-4584-83db-56b518f19c5a 뒤로가기 버튼을 선택하면 이전 화면으로 돌아가는 행동을 실행한다`() {
        var navigateUpCount = 0
        composeRule.setPlaceAddScreen(
            viewModel = screenTestViewModel(),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-a224a380-ae77-44dc-9144-58b68a62e9f8 추가 버튼으로 성공하면 입력을 초기화하고 다음 컬러와 제목 초점을 표시한다`() {
        assertSuccessResetsInput {
            composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        }
    }

    @Test
    fun `TC-a224a380-ae77-44dc-9144-58b68a62e9f8 단축키로 성공하면 입력을 초기화하고 다음 컬러와 제목 초점을 표시한다`() {
        assertSuccessResetsInput {
            composeRule.onAllNodes(hasSetTextAction()).onFirst().performKeyInput {
                keyDown(Key.MetaLeft)
                keyDown(Key.Enter)
                keyUp(Key.Enter)
                keyUp(Key.MetaLeft)
            }
        }
    }

    @Test
    fun `TC-62d37f5e-c163-4ad2-a1c5-fcbf2e5c45c0 설명과 주소를 입력하지 않고 컬러를 바꾸지 않으면 빈 설명, 빈 주소와 처음 제시한 컬러로 추가를 요청한다`() {
        val detailSlot = slot<PlaceDetail>()
        val viewModel = screenTestViewModel()
        every { viewModel.add(capture(detailSlot), tagIdSet = any()) } returns Unit
        composeRule.setPlaceAddScreen(viewModel = viewModel)
        composeRule.waitForIdle()
        val presentedColorHex = composeRule.currentColorHex()

        composeRule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(TYPED_TITLE)
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].performTextInput(TYPED_LATITUDE)
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].performTextInput(TYPED_LONGITUDE)
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        detailSlot.captured.title shouldBe TYPED_TITLE
        detailSlot.captured.description shouldBe ""
        detailSlot.captured.address shouldBe ""
        "#%06X".format(detailSlot.captured.color and RGB_MASK) shouldBe presentedColorHex
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
        val colorBefore = composeRule.currentColorHex()

        triggerAdd()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.add(any(), tagIdSet = any()) }
        composeRule.onNodeWithText(TYPED_TITLE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX].assert(hasText(""))
        composeRule.onAllNodes(hasSetTextAction())[ADDRESS_INDEX].assert(hasText(""))
        composeRule.onAllNodes(hasSetTextAction())[LATITUDE_INDEX].assert(hasText(""))
        composeRule.onAllNodes(hasSetTextAction())[LONGITUDE_INDEX].assert(hasText(""))
        // 컬러는 추가에 성공할 때마다 무작위로 새로 제시되므로, 이전과 다르면 새로 제시된 것이다.
        composeRule.currentColorHex() shouldNotBe colorBefore
        composeRule.onAllNodes(hasSetTextAction()).onFirst().assertIsFocused()
    }

    private companion object {
        private const val RGB_MASK = 0xFFFFFFL
    }
}
