@file:OptIn(ExperimentalTestApi::class)

package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryTitleInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TITLE-INPUT-FEATURE-002 기본 환경에서 제목 입력 라벨은 Title이다`() {
        setDiaryTitleInput()

        composeRule.onNodeWithText(DEFAULT_LABEL).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TITLE-INPUT-FEATURE-002 한국어 환경에서 제목 입력 라벨은 제목이다`() {
        setDiaryTitleInput()

        composeRule.onNodeWithText(KOREAN_LABEL).assertExists()
    }

    @Test
    fun `TC-TITLE-INPUT-FEATURE-001 엔터를 눌러도 줄이 나뉘지 않는다`() {
        lateinit var state: DiaryTitleInputState
        setDiaryTitleInput(initialText = INITIAL_TEXT, onState = { state = it })
        composeRule.onNode(hasSetTextAction()).performClick()

        composeRule.onNode(hasSetTextAction()).performKeyInput { pressKey(Key.Enter) }

        composeRule.runOnIdle { state.text.toString() shouldBe INITIAL_TEXT }
    }

    @Test
    fun `TC-TITLE-INPUT-FEATURE-003 지우기 버튼을 선택하면 텍스트가 비워지고 초점이 제목 입력에 남는다`() {
        lateinit var state: DiaryTitleInputState
        setDiaryTitleInput(initialText = INITIAL_TEXT, onState = { state = it })

        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_DESCRIPTION).performClick()

        composeRule.runOnIdle { state.text.toString() shouldBe "" }
        composeRule.onNodeWithText(INITIAL_TEXT).assertDoesNotExist()
        composeRule.onNode(hasSetTextAction()).assertIsFocused()
    }

    @Test
    fun `TC-TITLE-INPUT-FEATURE-004 제목이 비어 있으면 지우기 버튼이 표시되지 않는다`() {
        setDiaryTitleInput()

        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_DESCRIPTION).assertDoesNotExist()
    }

    private fun setDiaryTitleInput(
        initialText: String = "",
        onState: (DiaryTitleInputState) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                val state = rememberDiaryTitleInputState(initialText = initialText)
                onState(state)
                DiaryTitleInput(state = state)
            }
        }
    }

    public companion object {
        private const val DEFAULT_LABEL = "Title"
        private const val KOREAN_LABEL = "제목"
        private const val INITIAL_TEXT = "DiaryTitleInputValue"
        private const val DEFAULT_CLEAR_DESCRIPTION = "Clear text"
    }
}
