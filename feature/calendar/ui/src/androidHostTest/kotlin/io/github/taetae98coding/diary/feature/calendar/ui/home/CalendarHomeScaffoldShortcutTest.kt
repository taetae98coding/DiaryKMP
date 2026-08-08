package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.DEFAULT_CANCEL
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.englishTitle
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
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
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val previousYearMonth =
            today.yearMonth.firstDay
                .minus(1, DateTimeUnit.MONTH)
                .yearMonth

        setCalendarHomeScaffold()

        composeRule.onRoot().performKeyInput {
            keyDown(Key.DirectionLeft)
            keyUp(Key.DirectionLeft)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(previousYearMonth)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-011 오른쪽 방향키를 누르면 다음 달로 이동한다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val nextYearMonth =
            today.yearMonth.firstDay
                .plus(1, DateTimeUnit.MONTH)
                .yearMonth

        setCalendarHomeScaffold()

        composeRule.onRoot().performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(nextYearMonth)).assertIsDisplayed()
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
                )
            }
        }

        composeRule.onRoot().performKeyInput {
            keyDown(Key.DirectionLeft)
            keyUp(Key.DirectionLeft)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(minYearMonth)).assertIsDisplayed()
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
                CalendarHomeScaffold(onEvent = {})
            }
        }
    }
}
