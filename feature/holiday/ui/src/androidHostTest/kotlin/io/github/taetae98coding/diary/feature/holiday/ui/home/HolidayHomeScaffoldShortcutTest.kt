package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHolidayGroup
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.holiday
import io.github.taetae98coding.diary.feature.holiday.ui.home.goldenholiday.GoldenHolidayYear
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HolidayHomeScaffoldShortcutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-bd77cf90-eac1-4a44-ab84-f0b579ec6f47 왼쪽 방향키를 누르면 이전 년도로 이동한다`() {
        val state = HolidayHomeScaffoldState(initialYear = YEAR, initialAnnualLeaveCount = 0)
        setHolidayHomeScaffold(state = state)

        pressKey(Key.DirectionLeft)

        composeRule.onNodeWithText("${YEAR - 1}").assertExists()
        composeRule.textCount(holidayName(YEAR - 1)) shouldBe SUMMARY_AND_ONE_WEEK_COUNT
        composeRule.textCount(holidayName(YEAR)) shouldBe 0
    }

    @Test
    fun `TC-bd77cf90-eac1-4a44-ab84-f0b579ec6f47 오른쪽 방향키를 누르면 다음 년도로 이동한다`() {
        val state = HolidayHomeScaffoldState(initialYear = YEAR, initialAnnualLeaveCount = 0)
        setHolidayHomeScaffold(state = state)

        pressKey(Key.DirectionRight)

        composeRule.onNodeWithText("${YEAR + 1}").assertExists()
        composeRule.textCount(holidayName(YEAR + 1)) shouldBe SUMMARY_AND_ONE_WEEK_COUNT
        composeRule.textCount(holidayName(YEAR)) shouldBe 0
    }

    @Test
    fun `TC-ed6a3ba9-cfbc-4f01-9c1c-de26633cba97 1년에서 왼쪽 방향키를 눌러도 이전 년도로 이동하지 않는다`() {
        val state = HolidayHomeScaffoldState(initialYear = FIRST_YEAR, initialAnnualLeaveCount = 0)
        setHolidayHomeScaffold(state = state)

        pressKey(Key.DirectionLeft)

        state.year shouldBe FIRST_YEAR
        composeRule.textCount(holidayName(FIRST_YEAR)) shouldBe SUMMARY_AND_ONE_WEEK_COUNT
    }

    @Test
    fun `TC-105a5761-c99f-4c15-8a4a-52206b6a59fb 위쪽 방향키를 누르면 연차 개수가 하나 는다`() {
        val state = HolidayHomeScaffoldState(initialYear = YEAR, initialAnnualLeaveCount = 0)
        setHolidayHomeScaffold(state = state, uiStateFor = { _ -> HolidayHomeYearUiState.Content() })

        pressKey(Key.DirectionUp)

        composeRule.onNodeWithText("1").assertExists()
        state.annualLeaveCount shouldBe 1
    }

    @Test
    fun `TC-c4b992eb-91b9-4839-95a7-b585cd3d7df0 아래쪽 방향키를 누르면 연차 개수가 하나 준다`() {
        val state = HolidayHomeScaffoldState(initialYear = YEAR, initialAnnualLeaveCount = 2)
        setHolidayHomeScaffold(state = state, uiStateFor = { _ -> HolidayHomeYearUiState.Content() })

        pressKey(Key.DirectionDown)

        composeRule.onNodeWithText("1").assertExists()
        state.annualLeaveCount shouldBe 1
    }

    @Test
    fun `TC-20f7cbec-2f83-4a85-a0bb-4167819af0b7 연차 개수가 0이면 아래쪽 방향키를 눌러도 줄지 않는다`() {
        val state = HolidayHomeScaffoldState(initialYear = YEAR, initialAnnualLeaveCount = 0)
        setHolidayHomeScaffold(state = state, uiStateFor = { _ -> HolidayHomeYearUiState.Content() })

        pressKey(Key.DirectionDown)

        composeRule.onNodeWithText("0").assertExists()
        state.annualLeaveCount shouldBe 0
    }

    @Test
    fun `위쪽 방향키는 년도를, 오른쪽 방향키는 연차 개수를 바꾸지 않는다`() {
        val state = HolidayHomeScaffoldState(initialYear = YEAR, initialAnnualLeaveCount = 0)
        setHolidayHomeScaffold(state = state, uiStateFor = { _ -> HolidayHomeYearUiState.Content() })

        pressKey(Key.DirectionUp)
        state.year shouldBe YEAR

        pressKey(Key.DirectionRight)
        state.annualLeaveCount shouldBe 1
    }

    private fun pressKey(key: Key) {
        composeRule.onRoot().performKeyInput {
            keyDown(key)
            keyUp(key)
        }
        composeRule.waitForIdle()
    }

    private fun setHolidayHomeScaffold(
        state: HolidayHomeScaffoldState,
        uiStateFor: (Int) -> HolidayHomeYearUiState = ::goldenHolidayUiState,
    ) {
        composeRule.setContent {
            DiaryTheme {
                HolidayHomeScaffold(
                    onEvent = {},
                    state = state,
                    yearContent = { year, _ ->
                        GoldenHolidayYear(
                            uiStateProvider = { uiStateFor(year) },
                            onEvent = {},
                            modifier = Modifier.fillMaxSize(),
                        )
                    },
                )
            }
        }
    }

    private companion object {
        private const val FIRST_YEAR = 1

        // 이름은 요약 줄에 한 번, 겹치는 주마다 한 번씩 표시된다.
        private const val SUMMARY_AND_ONE_WEEK_COUNT = 2

        private fun holidayName(year: Int): String = "$year 공휴일"

        private fun goldenHolidayUiState(year: Int): HolidayHomeYearUiState {
            val holiday = holiday(name = holidayName(year), start = LocalDate(year = year, month = Month.FEBRUARY, day = 6))

            return HolidayHomeYearUiState.Content(
                goldenHolidayGroupList =
                    listOf(
                        goldenHolidayGroup(
                            optionList =
                                listOf(
                                    goldenHoliday(
                                        holidayList = listOf(holiday),
                                        start = holiday.dateRange.start,
                                        endInclusive = holiday.dateRange.start.plus(1, DateTimeUnit.DAY),
                                    ),
                                ),
                        ),
                    ),
            )
        }
    }
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.textCount(text: String): Int = onAllNodesWithText(text).fetchSemanticsNodes().size
