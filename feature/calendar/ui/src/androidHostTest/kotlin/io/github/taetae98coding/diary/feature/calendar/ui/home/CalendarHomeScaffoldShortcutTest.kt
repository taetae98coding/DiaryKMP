package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.DEFAULT_CANCEL
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.englishTitle
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScaffoldShortcutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-010 왼쪽 방향키를 누르면 이전 달로 이동한다`() {
        setCalendarHomeScaffold(initialYearMonth = JULY_2026)

        composeRule.onRoot().performKeyInput {
            keyDown(Key.DirectionLeft)
            keyUp(Key.DirectionLeft)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(YearMonth(year = 2026, month = Month.JUNE))).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(JUNE_15_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(JULY_15_DESCRIPTION).isNotDisplayed() shouldBe true
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-011 오른쪽 방향키를 누르면 다음 달로 이동한다`() {
        setCalendarHomeScaffold(initialYearMonth = JULY_2026)

        composeRule.onRoot().performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(YearMonth(year = 2026, month = Month.AUGUST))).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(AUGUST_15_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(JULY_15_DESCRIPTION).isNotDisplayed() shouldBe true
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-012 이동 범위를 벗어나는 방향키 입력은 무시된다`() {
        val minYearMonth = YearMonth(year = 1, month = Month.JANUARY)

        composeRule.setContent {
            DiaryTheme {
                CalendarHomeScaffold(
                    state =
                        rememberCalendarHomeScaffoldState(
                            calendarState = rememberCalendarState(initialYearMonth = minYearMonth),
                        ),
                    onEvent = {},
                    onCalendarEvent = {},
                )
            }
        }

        composeRule.onRoot().performKeyInput {
            keyDown(Key.DirectionLeft)
            keyUp(Key.DirectionLeft)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(minYearMonth)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(JANUARY_15_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `다이얼로그가 열려 있는 동안 방향키 입력은 달 이동으로 처리하지 않는다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        setCalendarHomeScaffold()

        composeRule.onNodeWithText(englishTitle(today.yearMonth)).performClick()
        composeRule.onAllNodes(isRoot()).onFirst().performKeyInput {
            keyDown(Key.DirectionLeft)
            keyUp(Key.DirectionLeft)
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CANCEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(today.yearMonth)).assertIsDisplayed()
    }

    private fun setCalendarHomeScaffold() {
        composeRule.setContent {
            DiaryTheme {
                CalendarHomeScaffold(onEvent = {}, onCalendarEvent = {})
            }
        }
    }

    private fun setCalendarHomeScaffold(initialYearMonth: YearMonth) {
        composeRule.setContent {
            DiaryTheme {
                CalendarHomeScaffold(
                    state =
                        rememberCalendarHomeScaffoldState(
                            calendarState = rememberCalendarState(initialYearMonth = initialYearMonth),
                        ),
                    onEvent = {},
                    onCalendarEvent = {},
                )
            }
        }
    }

    companion object {
        private const val JANUARY_15_DESCRIPTION = "January 15"
        private const val JUNE_15_DESCRIPTION = "June 15"
        private const val JULY_15_DESCRIPTION = "July 15"
        private const val AUGUST_15_DESCRIPTION = "August 15"
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
    }
}
