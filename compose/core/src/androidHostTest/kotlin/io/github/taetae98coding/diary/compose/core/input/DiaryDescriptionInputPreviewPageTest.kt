package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.gestures.snapTo
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
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
class DiaryDescriptionInputPreviewPageTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var scope: CoroutineScope

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-002 입력 페이지에서 왼쪽으로 스와이프하면 미리보기 페이지가 표시된다`() {
        val state = setDiaryDescriptionInput()
        composeRule.onNode(hasSetTextAction()).performTextInput(MARKDOWN_SOURCE)
        awaitPreviewText(MARKDOWN_HEADING_TEXT)

        swipeToPage(state = state, page = DiaryDescriptionInputPage.Preview)

        composeRule.onNodeWithText(MARKDOWN_HEADING_TEXT).assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).assertIsNotDisplayed()
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-003 미리보기 페이지에서 오른쪽으로 스와이프하면 입력 페이지가 표시된다`() {
        val state = setDiaryDescriptionInput()
        composeRule.onNode(hasSetTextAction()).performTextInput(MARKDOWN_SOURCE)
        awaitPreviewText(MARKDOWN_HEADING_TEXT)
        snapToPage(state = state, page = DiaryDescriptionInputPage.Preview)

        swipeToPage(state = state, page = DiaryDescriptionInputPage.Input)

        composeRule.onNode(hasSetTextAction()).assertIsDisplayed()
        composeRule.onNodeWithText(MARKDOWN_HEADING_TEXT).assertIsNotDisplayed()
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-009 미리보기 페이지에 마크다운 해석 결과가 표시된다`() {
        val state = setDiaryDescriptionInput()
        composeRule.onNode(hasSetTextAction()).performTextInput(MARKDOWN_SOURCE)
        awaitPreviewText(MARKDOWN_HEADING_TEXT)

        snapToPage(state = state, page = DiaryDescriptionInputPage.Preview)

        composeRule.onNodeWithText(MARKDOWN_HEADING_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(MARKDOWN_SOURCE).assertIsNotDisplayed()
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-010 수정한 내용이 미리보기에 반영된다`() {
        val state = setDiaryDescriptionInput()
        composeRule.onNode(hasSetTextAction()).performTextInput(MARKDOWN_SOURCE)
        awaitPreviewText(MARKDOWN_HEADING_TEXT)
        snapToPage(state = state, page = DiaryDescriptionInputPage.Preview)
        snapToPage(state = state, page = DiaryDescriptionInputPage.Input)

        composeRule.onNode(hasSetTextAction()).performTextReplacement(MODIFIED_MARKDOWN_SOURCE)
        awaitPreviewText(MODIFIED_MARKDOWN_HEADING_TEXT)
        snapToPage(state = state, page = DiaryDescriptionInputPage.Preview)

        composeRule.onNodeWithText(MODIFIED_MARKDOWN_HEADING_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(MARKDOWN_HEADING_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-011 입력한 내용이 없으면 미리보기 페이지는 빈 상태다`() {
        val state = setDiaryDescriptionInput()

        snapToPage(state = state, page = DiaryDescriptionInputPage.Preview)

        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
            .fetchSemanticsNodes()
            .map { it.id }
            .forEach { id ->
                composeRule
                    .onNode(SemanticsMatcher("SemanticsNode id is $id") { it.id == id })
                    .assertIsNotDisplayed()
            }
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-012 화면 재생성 후에도 입력 내용과 보고 있던 페이지가 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: DiaryDescriptionInputState
        restorationTester.setContent {
            state = rememberDiaryDescriptionInputState()
            scope = rememberCoroutineScope()

            DiaryTheme {
                DiaryDescriptionInput(state = state)
            }
        }
        composeRule.onNode(hasSetTextAction()).performTextInput(MARKDOWN_SOURCE)
        awaitPreviewText(MARKDOWN_HEADING_TEXT)
        snapToPage(state = state, page = DiaryDescriptionInputPage.Preview)

        restorationTester.emulateSavedInstanceStateRestore()
        awaitPreviewText(MARKDOWN_HEADING_TEXT)

        composeRule.runOnIdle {
            state.swipeState.currentValue shouldBe DiaryDescriptionInputPage.Preview
            state.text.toString() shouldBe MARKDOWN_SOURCE
        }
        composeRule.onNodeWithText(MARKDOWN_HEADING_TEXT).assertIsDisplayed()
        composeRule.onNode(hasSetTextAction()).assertIsNotDisplayed()

        snapToPage(state = state, page = DiaryDescriptionInputPage.Input)

        composeRule.onNode(hasSetTextAction()).assert(hasText(MARKDOWN_SOURCE))
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-017 엔터로 나눈 줄이 미리보기에서도 나뉘어 표시된다`() {
        val state = setDiaryDescriptionInput()
        composeRule.onNode(hasSetTextAction()).performTextInput(LINE_BREAK_SOURCE)

        snapToPage(state = state, page = DiaryDescriptionInputPage.Preview)
        awaitPreviewNode(previewText(LINE_BREAK_PREVIEW_TEXT))

        composeRule.onNode(previewText(LINE_BREAK_PREVIEW_TEXT)).assertIsDisplayed()
        composeRule.onNodeWithText(SOFT_LINE_BREAK_MERGED_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-018 빈 줄로 나눈 내용은 문단으로 구분되어 표시된다`() {
        val state = setDiaryDescriptionInput()
        composeRule.onNode(hasSetTextAction()).performTextInput(PARAGRAPH_SOURCE)

        snapToPage(state = state, page = DiaryDescriptionInputPage.Preview)
        awaitPreviewNode(hasText(FIRST_PARAGRAPH_TEXT))

        composeRule.onNodeWithText(FIRST_PARAGRAPH_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_PARAGRAPH_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-019 코드 블록 내부의 줄바꿈은 입력한 그대로 표시된다`() {
        val state = setDiaryDescriptionInput()
        composeRule.onNode(hasSetTextAction()).performTextInput(CODE_BLOCK_SOURCE)

        snapToPage(state = state, page = DiaryDescriptionInputPage.Preview)
        awaitPreviewNode(hasText(CODE_BLOCK_PREVIEW_TEXT))

        composeRule.onNodeWithText(CODE_BLOCK_PREVIEW_TEXT).assertIsDisplayed()
    }

    private fun previewText(text: String): SemanticsMatcher = hasText(text) and SemanticsMatcher.keyNotDefined(SemanticsActions.SetText)

    private fun awaitPreviewNode(matcher: SemanticsMatcher) {
        composeRule.waitUntil(timeoutMillis = WAIT_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
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

    private fun swipeToPage(
        state: DiaryDescriptionInputState,
        page: DiaryDescriptionInputPage,
    ) {
        composeRule.onRoot().performTouchInput {
            when (page) {
                DiaryDescriptionInputPage.Input -> swipeRight()
                DiaryDescriptionInputPage.Preview -> swipeLeft()
            }
        }
        awaitSettled(state = state, page = page)
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
        private const val MODIFIED_MARKDOWN_SOURCE = "# ModifiedHeading"
        private const val MODIFIED_MARKDOWN_HEADING_TEXT = "ModifiedHeading"
        private const val LINE_BREAK_SOURCE = "FirstLine\nSecondLine"
        private const val LINE_BREAK_PREVIEW_TEXT = "FirstLine\nSecondLine"
        private const val SOFT_LINE_BREAK_MERGED_TEXT = "FirstLine SecondLine"
        private const val PARAGRAPH_SOURCE = "FirstParagraph\n\nSecondParagraph"
        private const val FIRST_PARAGRAPH_TEXT = "FirstParagraph"
        private const val SECOND_PARAGRAPH_TEXT = "SecondParagraph"
        private val CODE_BLOCK_SOURCE = listOf("```", "CodeFirstLine", "CodeSecondLine", "```").joinToString(separator = "\n")
        private const val CODE_BLOCK_PREVIEW_TEXT = "CodeFirstLine\nCodeSecondLine"
    }
}
