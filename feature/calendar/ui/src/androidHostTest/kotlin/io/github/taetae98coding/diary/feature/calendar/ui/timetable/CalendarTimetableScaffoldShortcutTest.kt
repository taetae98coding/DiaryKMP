package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.calendar.api.CalendarTimetableNavKey
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarTimetableScaffoldShortcutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-9edcfcd3-6e29-4156-bdba-f323272d36b6 왼쪽 방향키를 누르면 이전 날짜로 이동한다`() {
        setCalendarTimetableScaffold(type = CalendarTimetableNavKey.Type.DAY, initialDate = september(day = 23))

        performKey(Key.DirectionLeft)

        composeRule.onNodeWithText("Tue").assertIsDisplayed()
        composeRule.onNodeWithText("22").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertDoesNotExist()
    }

    @Test
    fun `TC-fd152a35-4625-470e-90a0-acfacacc21e9 오른쪽 방향키를 누르면 다음 날짜로 이동한다`() {
        setCalendarTimetableScaffold(type = CalendarTimetableNavKey.Type.DAY, initialDate = september(day = 23))

        performKey(Key.DirectionRight)

        composeRule.onNodeWithText("Thu").assertIsDisplayed()
        composeRule.onNodeWithText("24").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertDoesNotExist()
    }

    @Test
    fun `TC-cdf12a56-0dac-4795-9354-e8bdf74bd37a 이동 범위를 벗어나는 방향키 입력은 무시된다`() {
        var latestState: CalendarTimetableScaffoldState? = null
        setCalendarTimetableScaffold(
            type = CalendarTimetableNavKey.Type.WEEK,
            initialDate = FIRST_DATE,
            onState = { latestState = it },
        )
        val initialDateRange: LocalDateRange? = composeRule.runOnIdle { latestState?.timetableState?.currentDateRange }

        performKey(Key.DirectionLeft)

        composeRule.runOnIdle { latestState?.timetableState?.currentDateRange shouldBe initialDateRange }
        initialDateRange?.contains(FIRST_DATE) shouldBe true
        composeRule.onNodeWithText(FIRST_DATE.day.toString()).assertIsDisplayed()
    }

    private fun setCalendarTimetableScaffold(
        type: CalendarTimetableNavKey.Type,
        initialDate: LocalDate,
        onState: (CalendarTimetableScaffoldState) -> Unit = {},
    ) {
        composeRule.setContent {
            val state = rememberCalendarTimetableScaffoldState(type = type, initialDate = initialDate)
            onState(state)

            DiaryTheme {
                CalendarTimetableScaffold(
                    onEvent = {},
                    onTimetableEvent = {},
                    state = state,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun performKey(key: Key) {
        composeRule.onRoot().performKeyInput {
            keyDown(key)
            keyUp(key)
        }
        composeRule.waitForIdle()
    }

    private fun september(day: Int): LocalDate = LocalDate(year = 2026, month = 9, day = day)

    private companion object {
        val FIRST_DATE = LocalDate(year = 1, month = 1, day = 1)
    }
}
