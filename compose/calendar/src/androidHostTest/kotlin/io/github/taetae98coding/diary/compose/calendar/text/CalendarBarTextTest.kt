package io.github.taetae98coding.diary.compose.calendar.text

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
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
import org.robolectric.annotation.GraphicsMode
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarBarTextTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-2b338e05-8b5f-4313-bb3a-5ced086b7e33 지정한 글이 표시된다`() {
        composeRule.setContent {
            DiaryTheme {
                CalendarBarText(text = SHORT_TEXT)
            }
        }

        composeRule.onNodeWithText(SHORT_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-1ce78552-31a6-4816-a2bd-74bd5c2b27fc 글이 한 번에 다 보이지 않아도 뒷부분을 생략하지 않는다`() {
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
        val lastLineIndex = textLayoutResult.lineCount - 1
        (0..lastLineIndex).none { lineIndex -> textLayoutResult.isLineEllipsized(lineIndex) } shouldBe true
        textLayoutResult.getLineEnd(lineIndex = lastLineIndex, visibleEnd = false) shouldBe LONG_TEXT.length
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

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `지정한 색이 색상 바에 표시된다`() {
        composeRule.setContent {
            DiaryTheme {
                CalendarBarText(
                    text = SHORT_TEXT,
                    modifier = Modifier.width(WIDE_WIDTH.dp),
                    color = BAR_COLOR,
                )
            }
        }

        barContainsColor(color = BAR_COLOR) shouldBe true
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `색을 지정하지 않으면 색상 바에 화면 기본 글자색을 사용한다`() {
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalContentColor provides DEFAULT_CONTENT_COLOR) {
                    CalendarBarText(
                        text = SHORT_TEXT,
                        modifier = Modifier.width(WIDE_WIDTH.dp),
                    )
                }
            }
        }

        barContainsColor(color = DEFAULT_CONTENT_COLOR) shouldBe true
    }

    @Test
    fun `글자색은 지정한 색과 관계없이 화면 기본 글자색을 사용한다`() {
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalContentColor provides DEFAULT_CONTENT_COLOR) {
                    CalendarBarText(
                        text = SHORT_TEXT,
                        modifier = Modifier.width(WIDE_WIDTH.dp),
                        color = BAR_COLOR,
                    )
                }
            }
        }

        composeRule
            .onNodeWithText(SHORT_TEXT)
            .textLayoutResult()
            .layoutInput.style.color shouldBe DEFAULT_CONTENT_COLOR
    }

    private fun barContainsColor(color: Color): Boolean {
        val bounds =
            composeRule
                .onNodeWithTag(CALENDAR_BAR_TEXT_BAR_TEST_TAG)
                .fetchSemanticsNode()
                .boundsInRoot
        val pixelMap = composeRule.onRoot().captureToImage().toPixelMap()

        return (bounds.left.roundToInt() until bounds.right.roundToInt()).any { x ->
            (bounds.top.roundToInt() until bounds.bottom.roundToInt()).any { y -> pixelMap[x, y] == color }
        }
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
        private val BAR_COLOR = Color(0xFFE53935)
        private val DEFAULT_CONTENT_COLOR = Color(0xFF1E88E5)
    }
}
