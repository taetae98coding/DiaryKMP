package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.APPOINTMENT_TITLE
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.BIRTHDAY_TEXT
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.HOLIDAY_NAME
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.SUNDAY_TEMPERATURE_TEXT
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.TRIP_TITLE
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.birthday
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.holiday
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.july
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.memo
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.mondayWeather
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.sundayWeather
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class CalendarHomeScreenWeatherGroupLayoutTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-99b7b166-3e09-42a1-8f0d-63cdf07f7fdc 날씨 날짜와 겹치지 않는 메모 제목도 날씨보다 아래 줄에 놓인다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather(), mondayWeather()),
            memoList = listOf(memo(title = TRIP_TITLE, start = july(day = 16), endInclusive = july(day = 17))),
        )

        (bottom(text = SUNDAY_TEMPERATURE_TEXT) <= top(text = TRIP_TITLE)) shouldBe true
    }

    @Test
    fun `TC-99b7b166-3e09-42a1-8f0d-63cdf07f7fdc 날씨 날짜와 겹치는 메모 제목은 날씨보다 아래 줄에 놓인다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather(), mondayWeather()),
            memoList = listOf(memo(title = TRIP_TITLE, start = july(day = 13), endInclusive = july(day = 14))),
        )

        (bottom(text = SUNDAY_TEMPERATURE_TEXT) <= top(text = TRIP_TITLE)) shouldBe true
    }

    @Test
    fun `TC-99b7b166-3e09-42a1-8f0d-63cdf07f7fdc 날씨 날짜와 겹치지 않는 공휴일 이름도 날씨보다 아래 줄에 놓인다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather(), mondayWeather()),
            holidayList = listOf(holiday()),
        )

        (bottom(text = SUNDAY_TEMPERATURE_TEXT) <= top(text = HOLIDAY_NAME)) shouldBe true
    }

    @Test
    fun `TC-99b7b166-3e09-42a1-8f0d-63cdf07f7fdc 날씨 날짜와 겹치지 않는 생일도 날씨보다 아래 줄에 놓인다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather(), mondayWeather()),
            birthdayList = listOf(birthday(date = july(day = 16))),
        )

        (bottom(text = SUNDAY_TEMPERATURE_TEXT) <= top(text = BIRTHDAY_TEXT)) shouldBe true
    }

    @Test
    fun `TC-5b18d05c-3d10-4972-8486-db60c896c193 날씨가 없는 주에서는 메모 제목이 첫 줄부터 놓인다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather()),
            memoList = listOf(memo(title = APPOINTMENT_TITLE, start = july(day = 20), endInclusive = july(day = 21))),
        )

        val weatherRowHeight = bottom(text = SUNDAY_TEMPERATURE_TEXT) - bottom(text = "12")
        val appointmentOffset = top(text = APPOINTMENT_TITLE) - bottom(text = "19")
        (appointmentOffset < weatherRowHeight) shouldBe true
    }

    // 온도는 날씨 아이템 하나로 병합되므로 병합 전 트리에서 온도 글자 자체의 위치를 읽는다.
    private fun top(text: String): Dp = composeRule.onNodeWithText(text, useUnmergedTree = true).getUnclippedBoundsInRoot().top

    private fun bottom(text: String): Dp = composeRule.onNodeWithText(text, useUnmergedTree = true).getUnclippedBoundsInRoot().bottom
}
