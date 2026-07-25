package io.github.taetae98coding.diary.compose.core.textfield

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.ComposeContentTestRule
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
class DiarySearchInputFieldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `검색어가 없으면 지우기 버튼을 두지 않는다`() {
        composeRule.setDiarySearchInputField()

        composeRule.onNodeWithContentDescription(CLEAR_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `검색어를 입력하면 지우기 버튼을 표시한다`() {
        composeRule.setDiarySearchInputField()

        composeRule.onNodeWithText(PLACEHOLDER).performTextInput(QUERY)

        composeRule.onNodeWithContentDescription(CLEAR_DESCRIPTION).assertExists()
    }

    @Test
    fun `지우기 버튼을 누르면 검색어를 지우고 초점을 검색 입력에 남긴다`() {
        val state = TextFieldState()
        composeRule.setDiarySearchInputField(state = state)

        composeRule.onNodeWithText(PLACEHOLDER).performTextInput(QUERY)
        composeRule.onNodeWithContentDescription(CLEAR_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        state.text.toString() shouldBe ""
        composeRule.onNodeWithText(PLACEHOLDER).assertIsFocused()
    }

    private fun ComposeContentTestRule.setDiarySearchInputField(state: TextFieldState = TextFieldState()) {
        setContent {
            DiaryTheme {
                Surface {
                    DiarySearchInputField(
                        placeholder = PLACEHOLDER,
                        state = state,
                    )
                }
            }
        }
    }

    private companion object {
        private const val PLACEHOLDER = "Search"
        private const val QUERY = "query"
        private const val CLEAR_DESCRIPTION = "Clear text"
    }
}
