package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.GoldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_ANNUAL_LEAVE_ITEM_LABEL
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.february
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHolidayGroup
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.holiday
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HolidayHomeScaffoldGoldenHolidayTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-011 황금연휴 항목에 공휴일 이름과 기간과 일수가 표시된다`() {
        val holiday = holiday(name = HOLIDAY_NAME, start = february(day = 6))
        setGoldenHoliday(
            optionList =
                listOf(
                    goldenHoliday(
                        holidayList = listOf(holiday),
                        start = february(day = 6),
                        endInclusive = february(day = 8),
                    ),
                ),
        )

        composeRule.textCount(HOLIDAY_NAME) shouldBe SUMMARY_AND_ONE_WEEK_COUNT
        composeRule.onNodeWithText("Feb 6 ~ Feb 8").assertExists()
        composeRule.onNodeWithText("3 days").assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-012 연차를 사용하는 연휴에 사용 연차 일수와 연차 이름이 표시된다`() {
        val holiday = holiday(name = HOLIDAY_NAME, start = february(day = 5))
        setGoldenHoliday(
            optionList =
                listOf(
                    goldenHoliday(
                        holidayList = listOf(holiday),
                        start = february(day = 5),
                        endInclusive = february(day = 8),
                        annualLeaveDateRangeList = listOf(february(day = 6)..february(day = 6)),
                    ),
                ),
        )

        composeRule.onNodeWithText("1 leave").assertExists()
        composeRule.onNodeWithText(DEFAULT_ANNUAL_LEAVE_ITEM_LABEL).assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-013 연차를 사용하지 않는 연휴에는 연차 이름이 표시되지 않는다`() {
        val holiday = holiday(name = HOLIDAY_NAME, start = february(day = 6))
        setGoldenHoliday(
            optionList =
                listOf(
                    goldenHoliday(
                        holidayList = listOf(holiday),
                        start = february(day = 6),
                        endInclusive = february(day = 8),
                    ),
                ),
        )

        composeRule.onNodeWithText("1 leave").assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_ANNUAL_LEAVE_ITEM_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-014 연휴가 걸친 주가 모두 표시된다`() {
        val holiday =
            holiday(
                name = LONG_HOLIDAY_NAME,
                start = february(day = 13),
                endInclusive = february(day = 15),
            )
        setGoldenHoliday(
            optionList =
                listOf(
                    goldenHoliday(
                        holidayList = listOf(holiday),
                        start = february(day = 13),
                        endInclusive = february(day = 15),
                    ),
                ),
        )

        composeRule.textCount(LONG_HOLIDAY_NAME) shouldBe SUMMARY_AND_TWO_WEEK_COUNT
    }

    private fun setGoldenHoliday(optionList: List<GoldenHoliday>) {
        val uiState =
            HolidayHomeYearUiState.Loaded(
                goldenHolidayGroupList = listOf(goldenHolidayGroup(optionList = optionList)),
            )

        composeRule.setContent {
            DiaryTheme {
                HolidayHomeScaffold(
                    onEvent = {},
                    state = rememberHolidayHomeScaffoldState(initialYear = YEAR),
                    yearContent = { _ ->
                        GoldenHolidayYear(
                            uiStateProvider = { uiState },
                            onEvent = {},
                            modifier = Modifier.fillMaxSize(),
                        )
                    },
                )
            }
        }
    }

    private companion object {
        private const val HOLIDAY_NAME = "공휴일"
        private const val LONG_HOLIDAY_NAME = "연휴"

        // 이름은 요약 줄에 한 번, 겹치는 주마다 한 번씩 표시된다.
        private const val SUMMARY_AND_ONE_WEEK_COUNT = 2
        private const val SUMMARY_AND_TWO_WEEK_COUNT = 3
    }
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.textCount(text: String): Int = onAllNodesWithText(text).fetchSemanticsNodes().size
