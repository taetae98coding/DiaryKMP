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
class CalendarTextTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-57f3c28c-32b4-46c0-a809-3ec05a8ee224 지정한 글이 표시된다`() {
        composeRule.setContent {
            DiaryTheme {
                CalendarText(text = SHORT_TEXT)
            }
        }

        composeRule.onNodeWithText(SHORT_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-1b7aa080-34ea-4ca9-adb2-fe9abdd0e32f 글이 한 번에 다 들어가지 않아도 줄을 바꾸거나 생략하지 않는다`() {
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

    @Test
    fun `TC-f72def00-66a7-4eef-89e5-9d995fa8be6e 글자색은 흰색과 검은색 중 배경색과의 명암 대비가 더 큰 색이다`() {
        composeRule.setContent {
            DiaryTheme {
                Column {
                    CONTRAST_CASES.forEachIndexed { index, (backgroundColor, _) ->
                        CalendarText(
                            text = SHORT_TEXT,
                            modifier = Modifier.testTag(contrastCaseTestTag(index)),
                            color = backgroundColor,
                        )
                    }
                }
            }
        }

        CONTRAST_CASES.forEachIndexed { index, (_, textColor) ->
            composeRule
                .onNodeWithTag(contrastCaseTestTag(index))
                .textLayoutResult()
                .layoutInput.style.color shouldBe textColor
        }
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `지정한 배경색 위에 글이 표시된다`() {
        composeRule.setContent {
            DiaryTheme {
                CalendarText(
                    text = SHORT_TEXT,
                    modifier =
                        Modifier
                            .width(WIDE_WIDTH.dp)
                            .testTag(CALENDAR_TEXT_TEST_TAG),
                    color = BACKGROUND_COLOR,
                )
            }
        }

        composeRule.onNodeWithText(SHORT_TEXT).assertIsDisplayed()
        backgroundContainsColor(color = BACKGROUND_COLOR) shouldBe true
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `배경색을 지정하지 않으면 화면 기본 글자색을 배경색으로 사용한다`() {
        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalContentColor provides DEFAULT_CONTENT_COLOR) {
                    CalendarText(
                        text = SHORT_TEXT,
                        modifier =
                            Modifier
                                .width(WIDE_WIDTH.dp)
                                .testTag(CALENDAR_TEXT_TEST_TAG),
                    )
                }
            }
        }

        composeRule.onNodeWithText(SHORT_TEXT).assertIsDisplayed()
        backgroundContainsColor(color = DEFAULT_CONTENT_COLOR) shouldBe true
    }

    /**
     * 짧은 글을 넓은 너비 가운데에 두어, 글이 그려지지 않는 세로 가운데 줄의 앞쪽 구간에서 배경색을 확인한다.
     */
    private fun backgroundContainsColor(color: Color): Boolean {
        val bounds =
            composeRule
                .onNodeWithTag(CALENDAR_TEXT_TEST_TAG)
                .fetchSemanticsNode()
                .boundsInRoot
        val pixelMap = composeRule.onRoot().captureToImage().toPixelMap()
        val y = bounds.center.y.roundToInt()

        return (bounds.left.roundToInt() until bounds.center.x.roundToInt()).any { x -> pixelMap[x, y] == color }
    }

    private fun SemanticsNodeInteraction.textLayoutResult(): TextLayoutResult {
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            action(textLayoutResults)
        }

        return textLayoutResults.single()
    }

    private fun contrastCaseTestTag(index: Int): String = "$CALENDAR_TEXT_TEST_TAG$index"

    companion object {
        private const val SHORT_TEXT = "제헌절"
        private const val LONG_TEXT = "한 줄에 담기지 않을 만큼 아주 긴 캘린더 아이템 이름"
        private const val NARROW_WIDTH = 40
        private const val WIDE_WIDTH = 200
        private const val CALENDAR_TEXT_TEST_TAG = "CalendarText"
        private val BACKGROUND_COLOR = Color(0xFFE53935)
        private val DEFAULT_CONTENT_COLOR = Color(0xFF1E88E5)
        private val CONTRAST_CASES =
            listOf(
                Color(0xFFFFEB3B) to Color.Black,
                Color(0xFF1A237E) to Color.White,
            )
    }
}
