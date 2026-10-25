package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
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
class CalendarClickTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-2b348907-dc1a-4c66-b567-e1219771947a 날짜 누름을 사용하면 누른 날짜가 전달된다`() {
        val clickedDateList = mutableListOf<LocalDate>()
        composeRule.setContent {
            DiaryTheme {
                Calendar(
                    state = rememberCalendarState(initialYearMonth = JULY_2026),
                    onEvent = { event -> (event as? CalendarEvent.ClickDate)?.let { clickedDateList += it.date } },
                    isDateClickEnabled = true,
                ) {}
            }
        }

        composeRule.onNodeWithContentDescription(JULY_15_DESCRIPTION).assert(isButton).performClick()

        clickedDateList shouldBe listOf(july(day = 15))
    }

    @Test
    fun `TC-ed535056-7571-4ded-b53a-91fcc534dfe0 주 누름을 사용하면 누른 주의 일요일 날짜가 전달된다`() {
        val clickedWeekList = mutableListOf<LocalDate>()
        composeRule.setContent {
            DiaryTheme {
                Calendar(
                    state = rememberCalendarState(initialYearMonth = JULY_2026),
                    onEvent = { event -> (event as? CalendarEvent.ClickWeek)?.let { clickedWeekList += it.startDate } },
                    isWeekClickEnabled = true,
                ) {}
            }
        }

        composeRule.onNodeWithContentDescription(WEEK_OF_JULY_12_DESCRIPTION).assert(isButton).performClick()

        clickedWeekList shouldBe listOf(july(day = 12))
    }

    @Test
    fun `TC-ff677d7a-7a1e-40c4-96e6-e6a84484bcf1 날짜 누름과 주 누름을 사용하지 않으면 날짜 칸과 주를 버튼으로 알리지 않는다`() {
        composeRule.setContent {
            DiaryTheme {
                Calendar(state = rememberCalendarState(initialYearMonth = JULY_2026)) {}
            }
        }

        composeRule.onAllNodesWithContentDescription(JULY_15_DESCRIPTION).assertCountEquals(0)
        composeRule.onAllNodesWithContentDescription(WEEK_OF_JULY_12_DESCRIPTION).assertCountEquals(0)
        composeRule.onAllNodes(hasClickAction()).assertCountEquals(0)
    }

    @Test
    fun `TC-ff677d7a-7a1e-40c4-96e6-e6a84484bcf1 이벤트를 받는 화면도 날짜 누름과 주 누름을 사용하지 않으면 날짜 칸과 주를 버튼으로 알리지 않는다`() {
        val eventList = mutableListOf<CalendarEvent>()
        composeRule.setContent {
            DiaryTheme {
                Calendar(
                    state = rememberCalendarState(initialYearMonth = JULY_2026),
                    onEvent = { eventList += it },
                ) {}
            }
        }

        composeRule.onAllNodesWithContentDescription(JULY_15_DESCRIPTION).assertCountEquals(0)
        composeRule.onAllNodesWithContentDescription(WEEK_OF_JULY_12_DESCRIPTION).assertCountEquals(0)
        composeRule.onAllNodes(hasClickAction()).assertCountEquals(0)
        composeRule.onNodeWithText("15").performClick()
        eventList shouldBe emptyList()
    }

    @Test
    @Config(sdk = [36], qualifiers = "ko")
    fun `날짜 칸과 주는 한국어 접근성 이름을 가진 버튼으로 알린다`() {
        composeRule.setContent {
            DiaryTheme {
                Calendar(
                    state = rememberCalendarState(initialYearMonth = JULY_2026),
                    onEvent = {},
                    isDateClickEnabled = true,
                    isWeekClickEnabled = true,
                ) {}
            }
        }

        composeRule.onNodeWithContentDescription(KOREAN_JULY_15_DESCRIPTION).assert(isButton)
        composeRule.onNodeWithContentDescription(KOREAN_WEEK_OF_JULY_12_DESCRIPTION).assert(isButton)
    }

    @Test
    fun `TC-dd67b552-7357-4107-98af-04bcdc4f7ef3 이웃 달 날짜를 누르면 그 실제 날짜가 전달된다`() {
        val clickedDateList = mutableListOf<LocalDate>()
        composeRule.setContent {
            DiaryTheme {
                Calendar(
                    state = rememberCalendarState(initialYearMonth = JULY_2026),
                    onEvent = { event -> (event as? CalendarEvent.ClickDate)?.let { clickedDateList += it.date } },
                    isDateClickEnabled = true,
                ) {}
            }
        }

        composeRule.onNodeWithContentDescription("June 28").performClick()

        clickedDateList shouldBe listOf(LocalDate(year = 2026, month = Month.JUNE, day = 28))
    }

    @Test
    fun `TC-5f15a085-7e21-4e86-81bc-15743d7ab226 날짜 칸이나 주를 길게 누르면 날짜 누름과 주 누름으로 다루지 않는다`() {
        val eventList = mutableListOf<CalendarEvent>()
        composeRule.setContent {
            DiaryTheme {
                Calendar(
                    state = rememberCalendarState(initialYearMonth = JULY_2026),
                    onEvent = { eventList += it },
                    isDateClickEnabled = true,
                    isWeekClickEnabled = true,
                ) {}
            }
        }

        composeRule.onNodeWithContentDescription(JULY_15_DESCRIPTION).performTouchInput { longClick() }
        composeRule.onNodeWithContentDescription(WEEK_OF_JULY_12_DESCRIPTION).performTouchInput { longClick() }
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
    }

    @Test
    fun `TC-9d24b8e0-736f-4428-aef3-aeb3fee908e6 아이템을 누르면 주 누름이 실행되지 않는다`() {
        val clickedWeekList = mutableListOf<LocalDate>()
        var itemClickCount = 0
        composeRule.setContent {
            DiaryTheme {
                Calendar(
                    state = rememberCalendarState(initialYearMonth = JULY_2026),
                    onEvent = { event -> (event as? CalendarEvent.ClickWeek)?.let { clickedWeekList += it.startDate } },
                    isWeekClickEnabled = true,
                ) {
                    group {
                        item(dateRange = july(day = 13)..july(day = 13), key = ITEM_TEXT) {
                            Text(
                                text = ITEM_TEXT,
                                modifier = Modifier.clickable { itemClickCount += 1 },
                            )
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithText(ITEM_TEXT).performClick()

        itemClickCount shouldBe 1
        clickedWeekList shouldBe emptyList()
    }

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    private companion object {
        val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
        const val ITEM_TEXT = "Item"
        const val JULY_15_DESCRIPTION = "July 15"
        const val WEEK_OF_JULY_12_DESCRIPTION = "Week of July 12"
        const val KOREAN_JULY_15_DESCRIPTION = "7월 15일"
        const val KOREAN_WEEK_OF_JULY_12_DESCRIPTION = "7월 12일 주"
        val isButton = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
    }
}
