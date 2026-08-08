package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_INCREASE_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHolidayGroup
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.holiday
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
class HolidayHomeScaffoldYearTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var targetYear by mutableStateOf<Int?>(null)

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-016 년도를 이동하면 제목과 목록이 이동한 년도로 바뀐다`() {
        val state = HolidayHomeScaffoldState(initialYear = YEAR, initialAnnualLeaveCount = 0)
        setHolidayHomeScaffold(state = state, uiStateFor = ::goldenHolidayUiState)

        composeRule.onNodeWithText("2026").assertExists()
        composeRule.textCount(THIS_YEAR_HOLIDAY_NAME) shouldBe SUMMARY_AND_ONE_WEEK_COUNT

        scrollTo(year = YEAR + 1)

        composeRule.onNodeWithText("2027").assertExists()
        composeRule.textCount(NEXT_YEAR_HOLIDAY_NAME) shouldBe SUMMARY_AND_ONE_WEEK_COUNT
        composeRule.textCount(THIS_YEAR_HOLIDAY_NAME) shouldBe 0
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-017 년도를 이동해도 연차 개수는 유지된다`() {
        val state = HolidayHomeScaffoldState(initialYear = YEAR, initialAnnualLeaveCount = 0)
        setHolidayHomeScaffold(state = state, uiStateFor = { _ -> HolidayHomeYearUiState.Loaded() })

        repeat(2) { composeRule.onNodeWithContentDescription(DEFAULT_INCREASE_DESCRIPTION).performClick() }
        composeRule.onNodeWithText("2").assertExists()

        scrollTo(year = YEAR + 1)

        composeRule.onNodeWithText("2027").assertExists()
        composeRule.onNodeWithText("2").assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-DOMAIN-013 1년보다 이전 년도로는 이동할 수 없다`() {
        val state = HolidayHomeScaffoldState(initialYear = FIRST_YEAR, initialAnnualLeaveCount = 0)
        setHolidayHomeScaffold(state = state, uiStateFor = { _ -> HolidayHomeYearUiState.Loaded() })

        composeRule.onNodeWithText("1").assertExists()

        scrollTo(year = FIRST_YEAR - 1)

        composeRule.onNodeWithText("1").assertExists()
    }

    private fun scrollTo(year: Int) {
        composeRule.runOnIdle { targetYear = year }
        composeRule.waitForIdle()
    }

    private fun setHolidayHomeScaffold(
        state: HolidayHomeScaffoldState,
        uiStateFor: (Int) -> HolidayHomeYearUiState,
    ) {
        composeRule.setContent {
            ScrollToYearEffect(state = state)

            DiaryTheme {
                HolidayHomeScaffold(
                    onEvent = {},
                    state = state,
                    yearContent = { year ->
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

    @Composable
    private fun ScrollToYearEffect(state: HolidayHomeScaffoldState) {
        val year = targetYear

        LaunchedEffect(year) {
            year?.let { state.animateScrollTo(year = it) }
        }
    }

    private companion object {
        private const val THIS_YEAR_HOLIDAY_NAME = "올해 공휴일"
        private const val NEXT_YEAR_HOLIDAY_NAME = "내년 공휴일"
        private const val FIRST_YEAR = 1

        // 이름은 요약 줄에 한 번, 겹치는 주마다 한 번씩 표시된다.
        private const val SUMMARY_AND_ONE_WEEK_COUNT = 2

        private fun goldenHolidayUiState(year: Int): HolidayHomeYearUiState {
            val holiday = yearHoliday(year = year) ?: return HolidayHomeYearUiState.Loaded()

            return HolidayHomeYearUiState.Loaded(
                goldenHolidayGroupList =
                    listOf(
                        goldenHolidayGroup(
                            optionList =
                                listOf(
                                    goldenHoliday(
                                        holidayList = listOf(holiday),
                                        start = holiday.dateRange.start,
                                        endInclusive = holiday.dateRange.start.plus(2, DateTimeUnit.DAY),
                                    ),
                                ),
                        ),
                    ),
            )
        }

        private fun yearHoliday(year: Int): Holiday? =
            when (year) {
                YEAR -> holiday(name = THIS_YEAR_HOLIDAY_NAME, start = LocalDate(year = YEAR, month = Month.FEBRUARY, day = 6))
                YEAR + 1 -> holiday(name = NEXT_YEAR_HOLIDAY_NAME, start = LocalDate(year = YEAR + 1, month = Month.FEBRUARY, day = 5))
                else -> null
            }
    }
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.textCount(text: String): Int = onAllNodesWithText(text).fetchSemanticsNodes().size
