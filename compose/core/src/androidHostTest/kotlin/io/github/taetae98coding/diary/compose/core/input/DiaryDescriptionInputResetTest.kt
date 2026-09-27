package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryDescriptionInputResetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `미리보기를 보던 중 빈 설명으로 새로 채우면 입력 페이지로 돌아간다`() {
        val (state, scope) = setDiaryDescriptionInput(initialText = MARKDOWN_SOURCE)
        composeRule.runOnIdle { state.swipeState.currentValue shouldBe DiaryDescriptionInputPage.Preview }

        composeRule.runOnIdle { scope.launch { state.reset() } }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            state.text.toString() shouldBe ""
            state.swipeState.currentValue shouldBe DiaryDescriptionInputPage.Input
        }
        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `입력 페이지에서 내용이 있는 설명으로 새로 채우면 미리보기 페이지로 바뀐다`() {
        val (state, scope) = setDiaryDescriptionInput(initialText = "")

        composeRule.runOnIdle { scope.launch { state.reset(text = MARKDOWN_SOURCE) } }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            state.text.toString() shouldBe MARKDOWN_SOURCE
            state.swipeState.currentValue shouldBe DiaryDescriptionInputPage.Preview
        }
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).assertIsSelected()
    }

    private fun setDiaryDescriptionInput(initialText: String): Pair<DiaryDescriptionInputState, CoroutineScope> {
        lateinit var state: DiaryDescriptionInputState
        lateinit var scope: CoroutineScope

        composeRule.setContent {
            state = rememberDiaryDescriptionInputState(initialText = initialText)
            scope = rememberCoroutineScope()

            DiaryTheme {
                DiaryDescriptionInput(state = state)
            }
        }
        composeRule.waitForIdle()

        return state to scope
    }

    private companion object {
        private const val MARKDOWN_SOURCE = "# MarkdownHeading"
        private const val INPUT_TAB_DESCRIPTION = "Input"
        private const val PREVIEW_TAB_DESCRIPTION = "Preview"
    }
}
