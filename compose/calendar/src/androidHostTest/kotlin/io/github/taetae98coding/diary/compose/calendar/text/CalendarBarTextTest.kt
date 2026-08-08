package io.github.taetae98coding.diary.compose.calendar.text

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
class CalendarBarTextTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-BAR-TEXT-FEATURE-001 지정한 글이 표시된다`() {
        composeRule.setContent {
            DiaryTheme {
                CalendarBarText(text = SHORT_TEXT)
            }
        }

        composeRule.onNodeWithText(SHORT_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-BAR-TEXT-FEATURE-002 글이 한 줄에 다 들어가지 않아도 줄을 바꾸거나 생략하지 않는다`() {
        composeRule.setContent {
            DiaryTheme {
                CalendarBarText(
                    text = LONG_TEXT,
                    modifier = Modifier.width(NARROW_WIDTH.dp),
                )
            }
        }

        val textLayoutResult = composeRule.onNodeWithText(LONG_TEXT).textLayoutResult()

        textLayoutResult.lineCount shouldBe 1
        textLayoutResult.getLineEnd(lineIndex = 0, visibleEnd = false) shouldBe LONG_TEXT.length
    }

    @Test
    fun `색상 바는 제목과 같은 높이를 차지한다`() {
        composeRule.setContent {
            DiaryTheme {
                CalendarBarText(
                    text = SHORT_TEXT,
                    modifier = Modifier.width(WIDE_WIDTH.dp),
                )
            }
        }

        val barHeight =
            composeRule
                .onNodeWithTag(CALENDAR_BAR_TEXT_BAR_TEST_TAG)
                .fetchSemanticsNode()
                .size.height
        val textHeight =
            composeRule
                .onNodeWithText(SHORT_TEXT)
                .fetchSemanticsNode()
                .size.height

        barHeight shouldBe textHeight
    }

    @Test
    fun `CalendarText와 같은 줄 높이를 차지한다`() {
        composeRule.setContent {
            DiaryTheme {
                Column {
                    CalendarText(
                        text = SHORT_TEXT,
                        modifier =
                            Modifier
                                .width(WIDE_WIDTH.dp)
                                .testTag(CALENDAR_TEXT_TEST_TAG),
                    )
                    CalendarBarText(
                        text = SHORT_TEXT,
                        modifier =
                            Modifier
                                .width(WIDE_WIDTH.dp)
                                .testTag(CALENDAR_BAR_TEXT_TEST_TAG),
                    )
                }
            }
        }

        val calendarTextHeight =
            composeRule
                .onNodeWithTag(CALENDAR_TEXT_TEST_TAG)
                .fetchSemanticsNode()
                .size.height
        val calendarBarTextHeight =
            composeRule
                .onNodeWithTag(CALENDAR_BAR_TEXT_TEST_TAG)
                .fetchSemanticsNode()
                .size.height

        calendarBarTextHeight shouldBe calendarTextHeight
    }

    private fun SemanticsNodeInteraction.textLayoutResult(): TextLayoutResult {
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            action(textLayoutResults)
        }

        return textLayoutResults.single()
    }

    companion object {
        private const val SHORT_TEXT = "회의 준비"
        private const val LONG_TEXT = "한 줄에 담기지 않을 만큼 아주 긴 캘린더 아이템 이름"
        private const val NARROW_WIDTH = 40
        private const val WIDE_WIDTH = 200
        private const val CALENDAR_TEXT_TEST_TAG = "CalendarText"
        private const val CALENDAR_BAR_TEXT_TEST_TAG = "CalendarBarText"
    }
}
