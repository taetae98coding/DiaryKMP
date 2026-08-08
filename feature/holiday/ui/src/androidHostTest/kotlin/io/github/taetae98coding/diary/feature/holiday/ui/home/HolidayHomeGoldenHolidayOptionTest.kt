package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.GoldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_NEXT_OPTION_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_PREVIOUS_OPTION_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_NEXT_OPTION_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_PREVIOUS_OPTION_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.february
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHolidayGroup
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.holiday
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 설날 2월 16일부터 18일까지에 연차 5일을 쓰면 앞으로 당긴 안과 뒤로 미룬 안이 함께 나온다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HolidayHomeGoldenHolidayOptionTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val targetYear = mutableStateOf<Int?>(null)

    @Test
    fun `기본 환경에서 대안 넘기기 동작에 접근성 이름을 제공한다`() {
        setGoldenHoliday()

        composeRule.onNodeWithContentDescription(DEFAULT_PREVIOUS_OPTION_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 대안 넘기기 동작에 접근성 이름을 제공한다`() {
        setGoldenHoliday()

        composeRule.onNodeWithContentDescription(KOREAN_PREVIOUS_OPTION_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_NEXT_OPTION_DESCRIPTION).assertExists()
    }

    @Test
    fun `카드에 지금 보고 있는 대안의 위치와 전체 대안 수가 표시된다`() {
        setGoldenHoliday()

        composeRule.onNodeWithText("1 / 2").assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-022 다음 대안으로 넘기면 카드 내용이 그 대안으로 바뀐다`() {
        setGoldenHoliday()

        composeRule.onNodeWithText(FORWARD_PERIOD).assertExists()

        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).performClick()

        composeRule.onNodeWithText(BACKWARD_PERIOD).assertExists()
        composeRule.onNodeWithText("12 days").assertExists()
        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertExists()
        composeRule.onNodeWithText(FORWARD_PERIOD).assertDoesNotExist()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-023 이전 대안으로 넘기면 앞 대안으로 되돌아간다`() {
        setGoldenHoliday()

        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).performClick()
        composeRule.onNodeWithContentDescription(DEFAULT_PREVIOUS_OPTION_DESCRIPTION).performClick()

        composeRule.onNodeWithText(FORWARD_PERIOD).assertExists()
        composeRule.onNodeWithText("1 / 2").assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-024 양 끝 대안에서는 그 방향의 넘기기 동작을 선택할 수 없다`() {
        setGoldenHoliday()

        composeRule.onNodeWithContentDescription(DEFAULT_PREVIOUS_OPTION_DESCRIPTION).assertIsNotEnabled()
        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).assertIsEnabled()

        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).performClick()

        composeRule.onNodeWithContentDescription(DEFAULT_PREVIOUS_OPTION_DESCRIPTION).assertIsEnabled()
        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).assertIsNotEnabled()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-025 대안이 하나뿐이면 두 넘기기 동작 모두 선택할 수 없다`() {
        setGoldenHoliday(optionList = listOf(forwardOption()))

        composeRule.onNodeWithText("1 / 1").assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PREVIOUS_OPTION_DESCRIPTION).assertIsNotEnabled()
        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).assertIsNotEnabled()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-032 다른 년도로 이동했다가 돌아와도 고른 대안은 유지된다`() {
        setGoldenHoliday()

        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).performClick()
        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertExists()

        scrollTo(year = YEAR + 1)
        scrollTo(year = YEAR)

        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertExists()
    }

    private fun scrollTo(year: Int) {
        composeRule.runOnIdle { targetYear.value = year }
        composeRule.waitForIdle()
    }

    private fun setGoldenHoliday(optionList: List<GoldenHoliday> = listOf(forwardOption(), backwardOption())) {
        val uiState =
            HolidayHomeYearUiState.Loaded(
                goldenHolidayGroupList = listOf(goldenHolidayGroup(optionList = optionList)),
            )
        val state = HolidayHomeScaffoldState(initialYear = YEAR, initialAnnualLeaveCount = 0)

        composeRule.setContent {
            ScrollToYearEffect(state = state)

            DiaryTheme {
                HolidayHomeScaffold(
                    onEvent = {},
                    state = state,
                    yearContent = { year ->
                        GoldenHolidayYear(
                            uiStateProvider = { if (year == YEAR) uiState else HolidayHomeYearUiState.Loaded() },
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
        val year = targetYear.value

        LaunchedEffect(year) {
            year?.let { state.animateScrollTo(year = it) }
        }
    }

    private companion object {
        private const val HOLIDAY_NAME = "설날"
        private const val FORWARD_PERIOD = "Feb 7 ~ Feb 18"
        private const val BACKWARD_PERIOD = "Feb 14 ~ Feb 25"
        private const val SECOND_OPTION_POSITION = "2 / 2"

        private fun lunarNewYear() =
            holiday(
                name = HOLIDAY_NAME,
                start = february(day = 16),
                endInclusive = february(day = 18),
            )

        // 연차를 설날 앞에 붙인 안이다.
        private fun forwardOption(): GoldenHoliday =
            goldenHoliday(
                holidayList = listOf(lunarNewYear()),
                start = february(day = 7),
                endInclusive = february(day = 18),
                annualLeaveDateRangeList = listOf(february(day = 9)..february(day = 13)),
            )

        // 연차를 설날 뒤에 붙인 안이다.
        private fun backwardOption(): GoldenHoliday =
            goldenHoliday(
                holidayList = listOf(lunarNewYear()),
                start = february(day = 14),
                endInclusive = february(day = 25),
                annualLeaveDateRangeList =
                    listOf(
                        february(day = 19)..february(day = 20),
                        february(day = 23)..february(day = 25),
                    ),
            )
    }
}
