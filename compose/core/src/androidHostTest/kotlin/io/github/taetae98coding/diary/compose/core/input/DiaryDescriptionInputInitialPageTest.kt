package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
class DiaryDescriptionInputInitialPageTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-020 채워진 설명이 있으면 처음 표시될 때 미리보기 페이지가 보인다`() {
        val state = setDiaryDescriptionInput(initialText = MARKDOWN_SOURCE)

        awaitSettled(state = state, page = DiaryDescriptionInputPage.Preview)
        awaitText(MARKDOWN_HEADING_TEXT)

        composeRule.onNodeWithText(MARKDOWN_HEADING_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(MARKDOWN_SOURCE).assertIsNotDisplayed()
        composeRule.onNode(hasSetTextAction()).assertIsNotDisplayed()
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).assertIsNotSelected()
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-021 채워진 설명이 공백뿐이면 처음 표시될 때 입력 페이지가 보인다`() {
        setDiaryDescriptionInput(initialText = BLANK_SOURCE)

        composeRule.onNode(hasSetTextAction()).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).assertIsNotSelected()
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-022 처음 표시된 뒤에 설명을 입력해도 입력 페이지가 유지된다`() {
        val state = setDiaryDescriptionInput()

        composeRule.onNode(hasSetTextAction()).performTextInput(MARKDOWN_SOURCE)
        composeRule.waitForIdle()

        composeRule.runOnIdle { state.swipeState.currentValue shouldBe DiaryDescriptionInputPage.Input }
        composeRule.onNode(hasSetTextAction()).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).assertIsSelected()
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-023 다른 설명으로 새로 채우면 미리보기 페이지가 보인다`() {
        val initialText = mutableStateOf("")
        composeRule.setContent {
            val text by initialText

            DiaryTheme {
                key(text) {
                    DiaryDescriptionInput(state = rememberDiaryDescriptionInputState(initialText = text))
                }
            }
        }
        composeRule.onNode(hasSetTextAction()).assertIsDisplayed()

        composeRule.runOnIdle { initialText.value = MARKDOWN_SOURCE }
        awaitText(MARKDOWN_HEADING_TEXT)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(MARKDOWN_HEADING_TEXT).assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).assertIsNotDisplayed()
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).assertIsSelected()
    }

    private fun setDiaryDescriptionInput(initialText: String = ""): DiaryDescriptionInputState {
        lateinit var state: DiaryDescriptionInputState

        composeRule.setContent {
            state = rememberDiaryDescriptionInputState(initialText = initialText)

            DiaryTheme {
                DiaryDescriptionInput(state = state)
            }
        }

        return state
    }

    private fun awaitText(text: String) {
        composeRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun awaitSettled(
        state: DiaryDescriptionInputState,
        page: DiaryDescriptionInputPage,
    ) {
        composeRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MILLIS) {
            state.swipeState.settledValue == page && !state.swipeState.isAnimationRunning
        }
        composeRule.waitForIdle()
    }

    public companion object {
        private const val WAIT_TIMEOUT_MILLIS = 5_000L
        private const val MARKDOWN_SOURCE = "# MarkdownHeading"
        private const val MARKDOWN_HEADING_TEXT = "MarkdownHeading"
        private const val BLANK_SOURCE = "   \n   "
        private const val INPUT_TAB_DESCRIPTION = "Input"
        private const val PREVIEW_TAB_DESCRIPTION = "Preview"
    }
}
