package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ColorPickerHostTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `show를 호출하면 초기 컬러가 반영된 다이얼로그가 노출된다`() {
        val dialogState = DialogState()
        setColorPickerHost(dialogState = dialogState, initialColor = TYPED_COLOR)

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()

        composeRule.runOnIdle { dialogState.show() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onNode(hasSetTextAction()).assert(hasText(TYPED_HEX))
    }

    @Test
    fun `확인하면 onConfirm으로 편집한 컬러가 전달되고 다이얼로그가 닫힌다`() {
        val dialogState = DialogState()
        val confirmedColors = mutableListOf<Color>()
        setColorPickerHost(dialogState = dialogState, initialColor = Color.Black, onConfirm = confirmedColors::add)

        composeRule.runOnIdle { dialogState.show() }
        composeRule.onNode(hasSetTextAction()).performTextReplacement(TYPED_HEX)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            confirmedColors shouldContainExactly listOf(TYPED_COLOR)
            dialogState.isVisible shouldBe false
        }
        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()
    }

    @Test
    fun `취소하면 onConfirm 없이 다이얼로그가 닫힌다`() {
        val dialogState = DialogState()
        val confirmedColors = mutableListOf<Color>()
        setColorPickerHost(dialogState = dialogState, initialColor = Color.Black, onConfirm = confirmedColors::add)

        composeRule.runOnIdle { dialogState.show() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CANCEL).performClick()
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            confirmedColors shouldContainExactly emptyList()
            dialogState.isVisible shouldBe false
        }
        composeRule.onNodeWithText(DEFAULT_CANCEL).assertDoesNotExist()
    }

    private fun setColorPickerHost(
        dialogState: DialogState,
        initialColor: Color,
        onConfirm: (Color) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                ColorPickerHost(
                    initialColorProvider = { initialColor },
                    onConfirm = onConfirm,
                    dialogState = dialogState,
                )
            }
        }
    }

    public companion object {
        private const val TYPED_HEX = "#3A7BD5"
        private const val DEFAULT_CONFIRM = "Confirm"
        private const val DEFAULT_CANCEL = "Cancel"
        private val TYPED_COLOR = Color(color = 0xFF3A7BD5.toInt())
    }
}
