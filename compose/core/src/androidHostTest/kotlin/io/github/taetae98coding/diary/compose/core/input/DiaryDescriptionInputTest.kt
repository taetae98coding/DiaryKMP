package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryDescriptionInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-001 채워진 설명이 없으면 처음 표시될 때 입력 페이지가 보인다`() {
        setDiaryDescriptionInput()

        composeRule.onNode(hasSetTextAction()).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_LABEL).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경 입력 칸 라벨은 설명이다`() {
        setDiaryDescriptionInput()

        composeRule.onNodeWithText(KOREAN_LABEL).assertIsDisplayed()
    }

    @Test
    fun `기본 환경 입력 칸 라벨은 Description이다`() {
        setDiaryDescriptionInput()

        composeRule.onNodeWithText(DEFAULT_LABEL).assertIsDisplayed()
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-005 여러 줄의 설명을 입력할 수 있다`() {
        setDiaryDescriptionInput()

        composeRule.onNode(hasSetTextAction()).performTextInput(MULTILINE_TEXT)

        composeRule.onNode(hasSetTextAction()).assert(hasText(MULTILINE_TEXT))
    }

    @Test
    fun `내용이 있으면 지우기 버튼이 표시된다`() {
        setDiaryDescriptionInput()

        composeRule.onNode(hasSetTextAction()).performTextInput(INPUT_TEXT)

        composeRule.onNode(clearButtonMatcher()).assertIsDisplayed()
    }

    @Test
    fun `TC-DESCRIPTION-INPUT-FEATURE-007 지우기 버튼을 누르면 내용이 모두 지워진다`() {
        setDiaryDescriptionInput()
        composeRule.onNode(hasSetTextAction()).performTextInput(INPUT_TEXT)

        composeRule.onNode(clearButtonMatcher()).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(hasSetTextAction()).assert(hasText(""))
        composeRule.onNode(clearButtonMatcher()).assertDoesNotExist()
    }

    @Test
    fun `내용이 없으면 지우기 버튼이 표시되지 않는다`() {
        setDiaryDescriptionInput()

        composeRule.onNode(clearButtonMatcher()).assertDoesNotExist()
    }

    @Test
    fun `컴포넌트는 탭 행과 입력 칸을 세로로 쌓은 높이를 가진다`() {
        setDiaryDescriptionInput()

        val tabBounds = composeRule.onNodeWithContentDescription(INPUT_TAB_CONTENT_DESCRIPTION).getUnclippedBoundsInRoot()
        val inputBounds = composeRule.onNode(hasSetTextAction()).getUnclippedBoundsInRoot()

        tabBounds.top shouldBe 0.dp
        inputBounds.top shouldBe tabBounds.bottom
        composeRule.onRoot().getUnclippedBoundsInRoot().height shouldBe inputBounds.bottom
    }

    private fun setDiaryDescriptionInput() {
        composeRule.setContent {
            DiaryTheme {
                DiaryDescriptionInput(state = rememberDiaryDescriptionInputState())
            }
        }
    }

    private fun clearButtonMatcher(): SemanticsMatcher = hasClickAction().and(hasContentDescription(CLEAR_BUTTON_CONTENT_DESCRIPTION))

    public companion object {
        private const val DEFAULT_LABEL = "Description"
        private const val KOREAN_LABEL = "설명"
        private const val INPUT_TAB_CONTENT_DESCRIPTION = "Input"
        private const val CLEAR_BUTTON_CONTENT_DESCRIPTION = "Clear text"
        private const val MULTILINE_TEXT = "first line\nsecond line\nthird line"
        private const val INPUT_TEXT = "DiaryDescriptionInputValue"
    }
}
