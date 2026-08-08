package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.APPOINTMENT_TITLE
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.HOLIDAY_NAME
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.NEXT_SUNDAY_TEMPERATURE_TEXT
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.SUNDAY_TEMPERATURE_TEXT
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.TRIP_TITLE
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.WORKSHOP_TITLE
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.holiday
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.july
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.memo
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.mondayWeather
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeWeatherGroupFixture.nextSundayWeather
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
    fun `날씨 날짜와 겹치지 않는 메모 제목은 날씨 아이템과 같은 줄에 놓인다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather(), mondayWeather()),
            memoList = listOf(memo(title = TRIP_TITLE, start = july(day = 16), endInclusive = july(day = 17))),
        )

        // 같은 줄을 공유하면 메모 제목은 줄 위쪽에 정렬되어 날씨의 온도 영역보다 위에 놓인다.
        (top(text = TRIP_TITLE) < top(text = SUNDAY_TEMPERATURE_TEXT)) shouldBe true
    }

    @Test
    fun `날씨 날짜와 겹치지 않는 공휴일 이름은 날씨 아이템과 같은 줄에 놓인다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather(), mondayWeather()),
            holidayList = listOf(holiday()),
        )

        (top(text = HOLIDAY_NAME) < top(text = SUNDAY_TEMPERATURE_TEXT)) shouldBe true
    }

    @Test
    fun `날씨 날짜와 겹치는 메모가 있으면 메모 제목과 공휴일 이름이 날씨 아이템보다 아래 줄에 놓인다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather(), mondayWeather()),
            memoList = listOf(memo(title = TRIP_TITLE, start = july(day = 13), endInclusive = july(day = 14))),
            holidayList = listOf(holiday()),
        )

        val temperatureBottom = bottom(text = SUNDAY_TEMPERATURE_TEXT)
        (temperatureBottom <= top(text = TRIP_TITLE)) shouldBe true
        (temperatureBottom <= top(text = HOLIDAY_NAME)) shouldBe true
    }

    @Test
    fun `날씨 날짜와 겹치는 공휴일이 있으면 겹치지 않는 메모 제목도 날씨 아이템보다 아래 줄에 놓인다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather(), mondayWeather()),
            memoList = listOf(memo(title = TRIP_TITLE, start = july(day = 16), endInclusive = july(day = 17))),
            holidayList = listOf(holiday(dateRange = july(day = 12)..july(day = 12))),
        )

        (bottom(text = SUNDAY_TEMPERATURE_TEXT) <= top(text = TRIP_TITLE)) shouldBe true
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h1600dp")
    fun `메모끼리 겹치면 날씨와 겹치지 않아도 메모 제목이 날씨 아이템보다 아래 줄에 놓인다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather()),
            memoList =
                listOf(
                    memo(title = TRIP_TITLE, start = july(day = 15), endInclusive = july(day = 17)),
                    memo(title = WORKSHOP_TITLE, start = july(day = 16), endInclusive = july(day = 18)),
                ),
        )

        // 한 줄에 모이지 않는 주는 날씨 높이에 맞춘 줄과 그렇지 않은 줄이 섞이므로 그룹을 나눈다.
        val temperatureBottom = bottom(text = SUNDAY_TEMPERATURE_TEXT)
        (temperatureBottom <= top(text = TRIP_TITLE)) shouldBe true
        (temperatureBottom <= top(text = WORKSHOP_TITLE)) shouldBe true
    }

    @Test
    fun `메모와 공휴일이 서로 겹치면 날씨와 겹치지 않아도 아래 줄에 놓인다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather()),
            memoList = listOf(memo(title = TRIP_TITLE, start = july(day = 16), endInclusive = july(day = 18))),
            holidayList = listOf(holiday()),
        )

        (bottom(text = SUNDAY_TEMPERATURE_TEXT) <= top(text = TRIP_TITLE)) shouldBe true
    }

    @Test
    fun `주마다 겹침을 따로 판단해 한 주는 같은 줄을 공유하고 다른 주는 아래 줄에 놓는다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather(), mondayWeather(), nextSundayWeather()),
            memoList =
                listOf(
                    memo(title = TRIP_TITLE, start = july(day = 16), endInclusive = july(day = 17)),
                    memo(title = APPOINTMENT_TITLE, start = july(day = 19), endInclusive = july(day = 20)),
                ),
        )

        (top(text = TRIP_TITLE) < top(text = SUNDAY_TEMPERATURE_TEXT)) shouldBe true
        (bottom(text = NEXT_SUNDAY_TEMPERATURE_TEXT) <= top(text = APPOINTMENT_TITLE)) shouldBe true
    }

    @Test
    fun `주 밖으로 벗어난 기간은 겹침 판단에 넣지 않는다`() {
        composeRule.setCalendarHomeWeatherGroupScreen(
            weatherList = listOf(sundayWeather(), nextSundayWeather()),
            memoList = listOf(memo(title = TRIP_TITLE, start = july(day = 16), endInclusive = july(day = 20))),
        )

        // 메모가 걸치는 7월 19일의 날씨는 다음 주에 속하므로 7월 12일이 있는 주의 겹침 판단에 넣지 않는다.
        (firstTop(text = TRIP_TITLE) < top(text = SUNDAY_TEMPERATURE_TEXT)) shouldBe true
    }

    // 온도는 날씨 아이템 하나로 병합되므로 병합 전 트리에서 온도 글자 자체의 위치를 읽는다.
    private fun top(text: String): Dp = composeRule.onNodeWithText(text, useUnmergedTree = true).getUnclippedBoundsInRoot().top

    private fun bottom(text: String): Dp = composeRule.onNodeWithText(text, useUnmergedTree = true).getUnclippedBoundsInRoot().bottom

    /** 여러 주에 조각으로 나뉜 아이템 중 가장 위쪽 조각의 위쪽 위치를 반환한다. */
    private fun firstTop(text: String): Dp =
        composeRule
            .onAllNodesWithText(text, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .indices
            .map { index -> composeRule.onAllNodesWithText(text, useUnmergedTree = true)[index].getUnclippedBoundsInRoot().top }
            .min()
}
