@file:OptIn(ExperimentalTestApi::class)

package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryTitleInputNextFocusTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TITLE-INPUT-DOMAIN-004 설명 입력 상태이면 Tab을 눌렀을 때 설명 입력으로 초점이 옮겨 간다`() {
        setTitleWithDescription(descriptionText = "")

        pressTabOnTitle()

        composeRule.onAllNodes(hasSetTextAction()).onLast().assertIsFocused()
    }

    @Test
    fun `TC-TITLE-INPUT-DOMAIN-005 마크다운 미리보기 상태이면 Tab을 눌렀을 때 제목 지우기로 초점이 옮겨 간다`() {
        setTitleWithDescription(descriptionText = DESCRIPTION_TEXT)

        pressTabOnTitle()

        composeRule.onAllNodesWithContentDescription(CLEAR_DESCRIPTION).onFirst().assertIsFocused()
    }

    @Test
    fun `TC-TITLE-INPUT-DOMAIN-006 뒤에 설명 입력이 없으면 Tab을 눌렀을 때 제목 지우기로 초점이 옮겨 간다`() {
        composeRule.setContent {
            DiaryTheme {
                DiaryTitleInput(state = rememberDiaryTitleInputState(initialText = TITLE_TEXT))
            }
        }

        pressTabOnTitle()

        composeRule.onAllNodesWithContentDescription(CLEAR_DESCRIPTION).onFirst().assertIsFocused()
    }

    private fun setTitleWithDescription(descriptionText: String) {
        composeRule.setContent {
            DiaryTheme {
                val descriptionState = rememberDiaryDescriptionInputState(initialText = descriptionText)

                Column {
                    DiaryTitleInput(
                        state = rememberDiaryTitleInputState(initialText = TITLE_TEXT),
                        nextFocusProvider = { descriptionState.focusTarget },
                    )
                    DiaryDescriptionInput(state = descriptionState)
                }
            }
        }
    }

    private fun pressTabOnTitle() {
        val title = composeRule.onNode(hasSetTextAction() and hasText(TITLE_TEXT))

        title.performClick()
        title.assertIsFocused()
        title.performKeyInput { pressKey(Key.Tab) }
    }

    public companion object {
        private const val TITLE_TEXT = "DiaryTitleInputValue"
        private const val DESCRIPTION_TEXT = "# DiaryDescriptionInputValue"
        private const val CLEAR_DESCRIPTION = "Clear text"
    }
}
