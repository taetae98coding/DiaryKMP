package io.github.taetae98coding.diary.compose.calendar.select

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.calendar.Calendar
import io.github.taetae98coding.diary.compose.calendar.CalendarState
import io.github.taetae98coding.diary.compose.calendar.FIRST_ITEM_TEXT
import io.github.taetae98coding.diary.compose.calendar.textItem
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarSelectItemScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-c9099121-cbcf-4b49-9edd-ba9aa208eafe 날짜 기간 선택 중에는 다른 손가락으로 움직여도 아이템 영역이 스크롤되지 않는다`() {
        val calendarState = CalendarState(initialYearMonth = JULY_2026)
        setCalendarWithFilledWeek(calendarState = calendarState)
        val initialFirstItemTop = firstItemTop()
        val itemPosition = firstItemCenter()

        composeRule.performLongPress(composeRule.dayCenter(day = 8))
        composeRule.onRoot().performTouchInput { down(pointerId = SECONDARY_POINTER_ID, position = itemPosition) }
        composeRule.waitForIdle()
        repeat(DRAG_STEP_COUNT) {
            composeRule.onRoot().performTouchInput { moveBy(pointerId = SECONDARY_POINTER_ID, delta = dragStep()) }
        }
        composeRule.waitForIdle()

        firstItemTop() shouldBe initialFirstItemTop
        composeRule.onNodeWithText(LAST_ITEM_TEXT).isNotDisplayed() shouldBe true
        calendarState.selectState.dateRange shouldBe july(day = 8)..july(day = 8)
        composeRule.onRoot().performTouchInput { up(pointerId = SECONDARY_POINTER_ID) }
        composeRule.performUp()
    }

    // 위 케이스가 스크롤되지 않음을 확인하는 이동이 선택 중이 아닐 때는 실제로 아이템 영역을 스크롤하는지 확인한다.
    @Test
    fun `선택 중이 아니면 같은 세로 이동으로 아이템 영역이 스크롤된다`() {
        setCalendarWithFilledWeek(calendarState = CalendarState(initialYearMonth = JULY_2026))
        val initialFirstItemTop = firstItemTop()
        val itemPosition = firstItemCenter()

        composeRule.onRoot().performTouchInput { down(position = itemPosition) }
        repeat(DRAG_STEP_COUNT) {
            composeRule.onRoot().performTouchInput { moveBy(delta = dragStep()) }
        }
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitForIdle()

        // 첫 아이템이 화면 밖으로 밀려나면 구성에서 빠지므로, 위치가 바뀌었거나 사라졌으면 스크롤된 것으로 본다.
        firstItemTopOrNull() shouldNotBe initialFirstItemTop
    }

    private fun setCalendarWithFilledWeek(calendarState: CalendarState) {
        composeRule.setContent {
            DiaryTheme {
                Calendar(state = calendarState, onEvent = {}, isSelectEnabled = true) {
                    group {
                        repeat(FILLED_ITEM_COUNT) { index ->
                            textItem(
                                text = filledItemText(index = index),
                                start = july(day = 5),
                                endInclusive = july(day = 11),
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(LAST_ITEM_TEXT).isNotDisplayed() shouldBe true
    }

    private fun firstItemTop(): Dp = composeRule.onNodeWithText(FIRST_ITEM_TEXT).getUnclippedBoundsInRoot().top

    private fun firstItemTopOrNull(): Dp? = if (composeRule.onAllNodesWithText(FIRST_ITEM_TEXT).fetchSemanticsNodes().isEmpty()) null else firstItemTop()

    private fun firstItemCenter(): Offset =
        composeRule
            .onNodeWithText(FIRST_ITEM_TEXT)
            .fetchSemanticsNode()
            .boundsInRoot.center

    private fun dragStep(): Offset = Offset(x = 0F, y = -with(composeRule.density) { DRAG_STEP_DISTANCE.toPx() })

    private fun filledItemText(index: Int): String =
        when (index) {
            0 -> FIRST_ITEM_TEXT
            FILLED_ITEM_COUNT - 1 -> LAST_ITEM_TEXT
            else -> "$FILLED_ITEM_TEXT_PREFIX$index"
        }

    private companion object {
        const val SECONDARY_POINTER_ID = 1
        const val FILLED_ITEM_COUNT = 10
        const val FILLED_ITEM_TEXT_PREFIX = "FilledCalendarItem"
        const val LAST_ITEM_TEXT = "LastCalendarItem"
        const val DRAG_STEP_COUNT = 10
        val DRAG_STEP_DISTANCE = 10.dp
    }
}
