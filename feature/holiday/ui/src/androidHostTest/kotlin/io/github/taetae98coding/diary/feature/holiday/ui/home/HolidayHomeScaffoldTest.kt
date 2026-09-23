package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_ANNUAL_LEAVE_LABEL
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_DECREASE_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_ERROR_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_INCREASE_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_NAVIGATE_UP_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_ANNUAL_LEAVE_LABEL
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_DECREASE_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_ERROR_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_INCREASE_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_NAVIGATE_UP_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.february
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHolidayGroup
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.holiday
import io.github.taetae98coding.diary.feature.holiday.ui.home.goldenholiday.GoldenHolidayYear
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HolidayHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `기본 환경에서 상단 바에 표시 중인 년도가 표시된다`() {
        setHolidayHomeScaffold()

        composeRule.onNodeWithText("2026").assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 상단 바에 표시 중인 년도가 표시된다`() {
        setHolidayHomeScaffold()

        composeRule.onNodeWithText("2026년").assertExists()
    }

    @Test
    fun `기본 환경에서 뒤로가기 동작에 접근성 이름을 제공한다`() {
        setHolidayHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertIsEnabled()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 뒤로가기 동작에 접근성 이름을 제공한다`() {
        setHolidayHomeScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assertIsEnabled()
    }

    private fun setHolidayHomeScaffold(uiState: HolidayHomeYearUiState = HolidayHomeYearUiState.Loading) {
        composeRule.setContent {
            DiaryTheme {
                HolidayHomeScaffold(
                    onEvent = {},
                    state = rememberHolidayHomeScaffoldState(initialYear = YEAR),
                    yearContent = { _, _ ->
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
    }
}
