package io.github.taetae98coding.diary.compose.calendar.week

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.calendar.FIRST_ITEM_TEXT
import io.github.taetae98coding.diary.compose.calendar.SECOND_ITEM_TEXT
import io.github.taetae98coding.diary.compose.calendar.july
import io.github.taetae98coding.diary.compose.calendar.textItem
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarWeekOfMonthItemScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-61053b13-566b-44e4-a179-91747dd77009 위쪽 줄에 아이템이 새로 생겨도 보던 아이템이 계속 보인다`() {
        assertLeadingGroupStaysHidden(filledItemStart = july(day = 5), filledItemEndInclusive = july(day = 11))
    }

    @Test
    fun `TC-61053b13-566b-44e4-a179-91747dd77009 첫 줄의 아이템이 주 중간에서 시작해도 위쪽 줄에 새로 생긴 아이템은 바로 보이지 않는다`() {
        assertLeadingGroupStaysHidden(filledItemStart = july(day = 7), filledItemEndInclusive = july(day = 8))
    }

    private fun assertLeadingGroupStaysHidden(
        filledItemStart: LocalDate,
        filledItemEndInclusive: LocalDate,
    ) {
        val hasLeadingGroupFlow = MutableStateFlow(false)
        setCalendarWeekOfMonth(
            hasLeadingGroupFlow = hasLeadingGroupFlow,
            filledItemStart = filledItemStart,
            filledItemEndInclusive = filledItemEndInclusive,
        )
        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()

        hasLeadingGroupFlow.value = true
        composeRule.waitForIdle()

        composeRule.onNodeWithText(SECOND_ITEM_TEXT).isNotDisplayed() shouldBe true
        composeRule.onNodeWithText(FIRST_ITEM_TEXT).assertIsDisplayed()
    }

    private fun setCalendarWeekOfMonth(
        hasLeadingGroupFlow: MutableStateFlow<Boolean>,
        filledItemStart: LocalDate,
        filledItemEndInclusive: LocalDate,
    ) {
        composeRule.setContent {
            val hasLeadingGroup by hasLeadingGroupFlow.collectAsStateWithLifecycle()

            DiaryTheme {
                CalendarWeekOfMonth(
                    yearMonth = YearMonth(year = 2026, month = Month.JULY),
                    weekOfMonth = 1,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(WEEK_HEIGHT),
                ) {
                    if (hasLeadingGroup) {
                        group { textItem(text = SECOND_ITEM_TEXT, start = july(day = 5), endInclusive = july(day = 11)) }
                    }
                    group {
                        repeat(FILLED_ITEM_COUNT) { index ->
                            textItem(
                                text = filledItemText(index = index),
                                start = filledItemStart,
                                endInclusive = filledItemEndInclusive,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun filledItemText(index: Int): String = if (index == 0) FIRST_ITEM_TEXT else "$FILLED_ITEM_TEXT_PREFIX$index"

    companion object {
        private const val FILLED_ITEM_COUNT = 6
        private const val FILLED_ITEM_TEXT_PREFIX = "FilledCalendarItem"
        private val WEEK_HEIGHT = 96.dp
    }
}
