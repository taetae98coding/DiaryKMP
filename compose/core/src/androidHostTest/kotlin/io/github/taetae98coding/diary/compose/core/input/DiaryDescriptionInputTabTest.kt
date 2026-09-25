package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.gestures.snapTo
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.comparables.shouldBeLessThan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryDescriptionInputTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var scope: CoroutineScope

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-002 미리보기 탭을 선택하면 미리보기 페이지가 표시된다`() {
        val state = setDiaryDescriptionInput()
        composeRule.onNode(hasSetTextAction()).performTextInput(MARKDOWN_SOURCE)
        awaitPreviewText(MARKDOWN_HEADING_TEXT)

        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).performClick()
        awaitSettled(state = state, page = DiaryDescriptionInputPage.Preview)

        composeRule.onNodeWithText(MARKDOWN_HEADING_TEXT).assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).assertIsNotDisplayed()
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).assertIsNotSelected()
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-003 입력 탭을 선택하면 입력 페이지가 표시된다`() {
        val state = setDiaryDescriptionInput()
        composeRule.onNode(hasSetTextAction()).performTextInput(MARKDOWN_SOURCE)
        awaitPreviewText(MARKDOWN_HEADING_TEXT)
        snapToPage(state = state, page = DiaryDescriptionInputPage.Preview)

        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).performClick()
        awaitSettled(state = state, page = DiaryDescriptionInputPage.Input)

        composeRule.onNode(hasSetTextAction()).assertIsDisplayed()
        composeRule.onNodeWithText(MARKDOWN_HEADING_TEXT).assertIsNotDisplayed()
        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).assertIsNotSelected()
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-001 채워진 설명이 없으면 처음 표시될 때 입력 탭이 선택되어 있다`() {
        setDiaryDescriptionInput()

        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).assertIsNotSelected()
    }

    @Test
    fun `스와이프로 전환하면 선택된 탭이 보고 있는 페이지를 따라간다`() {
        val state = setDiaryDescriptionInput()

        composeRule.onRoot().performTouchInput { swipeLeft() }
        awaitSettled(state = state, page = DiaryDescriptionInputPage.Preview)

        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).assertIsSelected()
        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).assertIsNotSelected()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경 탭 접근성 이름은 입력과 미리보기다`() {
        setDiaryDescriptionInput()

        composeRule.onNodeWithContentDescription(KOREAN_INPUT_TAB_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(KOREAN_PREVIEW_TAB_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `기본 환경 탭 접근성 이름은 Input과 Preview다`() {
        setDiaryDescriptionInput()

        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `입력 탭을 미리보기 탭보다 왼쪽에 표시한다`() {
        setDiaryDescriptionInput()

        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).getUnclippedBoundsInRoot().left shouldBeLessThan
            composeRule.onNodeWithContentDescription(PREVIEW_TAB_DESCRIPTION).getUnclippedBoundsInRoot().left
    }

    @Test
    fun `이미 보고 있는 페이지의 탭을 다시 선택해도 페이지가 유지된다`() {
        val state = setDiaryDescriptionInput()

        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).performClick()
        awaitSettled(state = state, page = DiaryDescriptionInputPage.Input)

        composeRule.onNode(hasSetTextAction()).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(INPUT_TAB_DESCRIPTION).assertIsSelected()
    }

    private fun setDiaryDescriptionInput(): DiaryDescriptionInputState {
        lateinit var state: DiaryDescriptionInputState

        composeRule.setContent {
            state = rememberDiaryDescriptionInputState()
            scope = rememberCoroutineScope()

            DiaryTheme {
                DiaryDescriptionInput(state = state)
            }
        }

        return state
    }

    private fun awaitPreviewText(text: String) {
        composeRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun snapToPage(
        state: DiaryDescriptionInputState,
        page: DiaryDescriptionInputPage,
    ) {
        composeRule.runOnIdle {
            scope.launch { state.swipeState.snapTo(page) }
        }
        awaitSettled(state = state, page = page)
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
        private const val INPUT_TAB_DESCRIPTION = "Input"
        private const val PREVIEW_TAB_DESCRIPTION = "Preview"
        private const val KOREAN_INPUT_TAB_DESCRIPTION = "입력"
        private const val KOREAN_PREVIEW_TAB_DESCRIPTION = "미리보기"
    }
}
