package io.github.taetae98coding.diary.compose.core.tooltip

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import io.github.taetae98coding.diary.compose.core.button.SearchButton
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryTooltipBoxTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `설명 문구는 길게 누르기 전에는 표시되지 않는다`() {
        setDiaryTooltipBox(text = TOOLTIP_TEXT)

        composeRule.onNodeWithText(TOOLTIP_TEXT).assertDoesNotExist()
    }

    @Test
    fun `길게 누르면 설명 문구를 표시한다`() {
        setDiaryTooltipBox(text = TOOLTIP_TEXT)

        composeRule.onNodeWithText(ANCHOR_TEXT).performTouchInput { longClick() }

        composeRule.onNodeWithText(TOOLTIP_TEXT).assertExists()
    }

    @Test
    fun `설명 문구가 비어 있으면 길게 눌러도 아무것도 표시하지 않는다`() {
        setDiaryTooltipBox(text = "")

        composeRule.onNodeWithText(ANCHOR_TEXT).performTouchInput { longClick() }

        composeRule.onNodeWithText(TOOLTIP_TEXT).assertDoesNotExist()
    }

    @Test
    fun `아이콘 버튼은 접근성 이름을 설명 문구로 표시한다`() {
        composeRule.setContent {
            DiaryTheme {
                SearchButton(
                    onClick = {},
                    contentDescription = TOOLTIP_TEXT,
                )
            }
        }

        composeRule.onNodeWithContentDescription(TOOLTIP_TEXT).performTouchInput { longClick() }

        composeRule.onNodeWithText(TOOLTIP_TEXT).assertExists()
    }

    private fun setDiaryTooltipBox(text: String) {
        composeRule.setContent {
            DiaryTheme {
                DiaryTooltipBox(text = text) {
                    Text(text = ANCHOR_TEXT)
                }
            }
        }
    }

    public companion object {
        private const val ANCHOR_TEXT = "버튼"
        private const val TOOLTIP_TEXT = "검색"
    }
}
