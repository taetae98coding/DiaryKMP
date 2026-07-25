package io.github.taetae98coding.diary.compose.core.textfield

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ClearTextFieldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `텍스트가 비어 있으면 지우기 버튼이 표시되지 않는다`() {
        setClearTextField(initialText = "")

        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `텍스트가 있으면 지우기 버튼이 표시된다`() {
        setClearTextField(initialText = INITIAL_TEXT)

        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_DESCRIPTION).assertExists()
    }

    @Test
    fun `기본 환경에서 지우기 버튼 접근성 이름은 Clear text이다`() {
        setClearTextField(initialText = INITIAL_TEXT)

        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 지우기 버튼 접근성 이름은 지우기이다`() {
        setClearTextField(initialText = INITIAL_TEXT)

        composeRule.onNodeWithContentDescription(KOREAN_CLEAR_DESCRIPTION).assertExists()
    }

    @Test
    fun `지우기 버튼을 선택하면 텍스트가 비워진다`() {
        lateinit var state: TextFieldState
        setClearTextField(initialText = INITIAL_TEXT, onState = { state = it })

        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_DESCRIPTION).performClick()

        composeRule.runOnIdle { state.text.toString() shouldBe "" }
        composeRule.onNodeWithText(INITIAL_TEXT).assertDoesNotExist()
    }

    @Test
    fun `지우기 버튼을 선택하면 입력 필드에 포커스가 요청된다`() {
        setClearTextField(initialText = INITIAL_TEXT)

        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_DESCRIPTION).performClick()

        composeRule.onNode(hasSetTextAction()).assertIsFocused()
    }

    @Test
    fun `label을 전달하면 표시된다`() {
        composeRule.setContent {
            DiaryTheme {
                ClearTextField(
                    state = rememberTextFieldState(),
                    label = { Text(text = LABEL) },
                )
            }
        }

        composeRule.onNodeWithText(LABEL).assertExists()
    }

    @Test
    fun `사용자가 입력하면 지우기 버튼이 나타난다`() {
        setClearTextField(initialText = "")
        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_DESCRIPTION).assertDoesNotExist()

        composeRule.onNode(hasSetTextAction()).performTextInput(TYPED_TEXT)

        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_DESCRIPTION).assertExists()
    }

    private fun setClearTextField(
        initialText: String,
        onState: (TextFieldState) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                val state = rememberTextFieldState(initialText = initialText)
                onState(state)
                ClearTextField(state = state)
            }
        }
    }

    public companion object {
        private const val INITIAL_TEXT = "ClearTextFieldValue"
        private const val TYPED_TEXT = "TypedValue"
        private const val LABEL = "ClearTextFieldLabel"
        private const val DEFAULT_CLEAR_DESCRIPTION = "Clear text"
        private const val KOREAN_CLEAR_DESCRIPTION = "지우기"
    }
}
