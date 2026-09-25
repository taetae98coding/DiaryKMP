package io.github.taetae98coding.diary.compose.calendar.text

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarTextTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-TEXT-FEATURE-001 지정한 글이 표시된다`() {
        composeRule.setContent {
            DiaryTheme {
                CalendarText(text = SHORT_TEXT)
            }
        }

        composeRule.onNodeWithText(SHORT_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-TEXT-DOMAIN-001 글이 한 번에 다 들어가지 않아도 줄을 바꾸거나 생략하지 않는다`() {
        composeRule.setContent {
            DiaryTheme {
                CalendarText(
                    text = LONG_TEXT,
                    modifier = Modifier.width(NARROW_WIDTH.dp),
                )
            }
        }

        val textLayoutResult = composeRule.onNodeWithText(LONG_TEXT).textLayoutResult()

        textLayoutResult.lineCount shouldBe 1
        textLayoutResult.getLineEnd(lineIndex = 0, visibleEnd = false) shouldBe LONG_TEXT.length
    }

    private fun SemanticsNodeInteraction.textLayoutResult(): TextLayoutResult {
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            action(textLayoutResults)
        }

        return textLayoutResults.single()
    }

    companion object {
        private const val SHORT_TEXT = "제헌절"
        private const val LONG_TEXT = "한 줄에 담기지 않을 만큼 아주 긴 캘린더 아이템 이름"
        private const val NARROW_WIDTH = 40
    }
}
