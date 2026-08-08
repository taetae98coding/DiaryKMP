package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.HOLIDAY_NAME
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.MONDAY_TEMPERATURE_TEXT
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.SUNDAY_TEMPERATURE_TEXT
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.TRIP_TITLE
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.WORKSHOP_TITLE
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.holiday
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.july
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.memo
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.mondayWeather
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.sundayWeather
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class CalendarHomeScreenWeatherGroupTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-057 날씨 날짜와 겹치지 않는 메모 공휴일이 있는 주에서도 날씨와 메모 공휴일이 모두 표시된다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather(), mondayWeather()),
            memoList = listOf(memo(title = TRIP_TITLE, start = july(day = 16), endInclusive = july(day = 17))),
            holidayList = listOf(holiday()),
        )

        composeRule.onNodeWithText(SUNDAY_TEMPERATURE_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(MONDAY_TEMPERATURE_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(TRIP_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(HOLIDAY_NAME).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-058 날씨 날짜와 겹치는 메모가 있는 주에서도 날씨와 메모 공휴일이 모두 표시된다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather(), mondayWeather()),
            memoList = listOf(memo(title = TRIP_TITLE, start = july(day = 13), endInclusive = july(day = 14))),
            holidayList = listOf(holiday()),
        )

        composeRule.onNodeWithText(SUNDAY_TEMPERATURE_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(MONDAY_TEMPERATURE_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(TRIP_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(HOLIDAY_NAME).assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h1600dp")
    fun `TC-CALENDAR-HOME-FEATURE-059 메모끼리 겹치는 주에서도 날씨와 메모가 모두 표시된다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather()),
            memoList =
                listOf(
                    memo(title = TRIP_TITLE, start = july(day = 15), endInclusive = july(day = 17)),
                    memo(title = WORKSHOP_TITLE, start = july(day = 16), endInclusive = july(day = 18)),
                ),
        )

        composeRule.onNodeWithText(SUNDAY_TEMPERATURE_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(TRIP_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(WORKSHOP_TITLE).assertIsDisplayed()
    }
}
