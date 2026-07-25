package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryEmojiInputDialogHostTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `show를 호출하면 현재 이모지가 채워진 다이얼로그가 노출된다`() {
        val dialogState = DialogState()
        setDiaryEmojiInputDialogHost(dialogState = dialogState, state = DiaryEmojiInputState(initialText = RUNNER))

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()

        composeRule.runOnIdle { dialogState.show() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.dialogInput().assert(hasText(RUNNER))
    }

    @Test
    fun `확인하면 입력한 이모지가 상태에 반영되고 다이얼로그가 닫힌다`() {
        val dialogState = DialogState(isVisible = true)
        val state = DiaryEmojiInputState(initialText = RUNNER)
        setDiaryEmojiInputDialogHost(dialogState = dialogState, state = state)

        composeRule.dialogInput().performTextReplacement(SWIMMER)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            state.text shouldBe SWIMMER
            dialogState.isVisible shouldBe false
        }
        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()
    }

    @Test
    fun `취소하면 상태를 바꾸지 않고 다이얼로그가 닫힌다`() {
        val dialogState = DialogState(isVisible = true)
        val state = DiaryEmojiInputState(initialText = RUNNER)
        setDiaryEmojiInputDialogHost(dialogState = dialogState, state = state)

        composeRule.dialogInput().performTextReplacement(SWIMMER)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CANCEL).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            state.text shouldBe RUNNER
            dialogState.isVisible shouldBe false
        }
        composeRule.onNodeWithText(DEFAULT_CANCEL).assertDoesNotExist()
    }

    private fun setDiaryEmojiInputDialogHost(
        dialogState: DialogState,
        state: DiaryEmojiInputState,
    ) {
        composeRule.setContent {
            DiaryTheme {
                DiaryEmojiInputDialogHost(
                    dialogState = dialogState,
                    state = state,
                )
            }
        }
    }
}
