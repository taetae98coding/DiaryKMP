package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.feature.calendar.ui.permission.rememberLocationPermissionRequester
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class CalendarHomeScreenWeatherTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-037 날씨가 준비되면 조작 없이 캘린더 표시가 반영된다`() {
        val weatherReportFlow = MutableStateFlow(CalendarWeatherReport())
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            weatherViewModel = weatherViewModel(weatherReportFlow = weatherReportFlow),
        )
        composeRule.onNodeWithContentDescription(SUNNY_DESCRIPTION, useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertDoesNotExist()

        weatherReportFlow.value =
            CalendarWeatherReport(
                weatherList =
                    listOf(
                        calendarWeather(
                            date = july(day = 15),
                            temperature = CalendarWeatherTemperature.Current(value = 24.3),
                            descriptionList = listOf(SUNNY_DESCRIPTION),
                        ),
                    ),
            )
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(SUNNY_DESCRIPTION, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-038 캘린더에 보이는 주와 겹치지 않는 날짜의 날씨는 표시되지 않는다`() {
        val weather =
            calendarWeather(
                date = july(day = 15),
                temperature = CalendarWeatherTemperature.Current(value = 24.3),
                descriptionList = listOf(SUNNY_DESCRIPTION),
            )
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            weatherViewModel = weatherViewModel(weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(weather)))),
        )
        composeRule.onNodeWithContentDescription(SUNNY_DESCRIPTION, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertIsDisplayed()

        repeat(MONTH_MOVE_COUNT_TO_OCTOBER) {
            composeRule.onRoot().performKeyInput {
                keyDown(Key.DirectionRight)
                keyUp(Key.DirectionRight)
            }
            composeRule.waitForIdle()
        }

        composeRule.onNodeWithContentDescription(SUNNY_DESCRIPTION, useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertDoesNotExist()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w840dp-h900dp")
    fun `TC-CALENDAR-HOME-FEATURE-040 오늘 날짜의 모든 날씨 아이콘과 현재 기온이 표시된다`() {
        val weather =
            calendarWeather(
                date = july(day = 15),
                temperature = CalendarWeatherTemperature.Current(value = 24.3),
                descriptionList = listOf(SUNNY_DESCRIPTION, CLOUDY_DESCRIPTION, RAINY_DESCRIPTION),
            )

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            weatherViewModel = weatherViewModel(weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(weather)))),
        )

        val sunnyLeft = composeRule.iconLeft(description = SUNNY_DESCRIPTION)
        val cloudyLeft = composeRule.iconLeft(description = CLOUDY_DESCRIPTION)
        val rainyLeft = composeRule.iconLeft(description = RAINY_DESCRIPTION)
        (sunnyLeft < cloudyLeft) shouldBe true
        (cloudyLeft < rainyLeft) shouldBe true
        val iconGroupCenter =
            (
                sunnyLeft +
                    composeRule
                        .onNodeWithContentDescription(RAINY_DESCRIPTION, useUnmergedTree = true)
                        .fetchSemanticsNode()
                        .boundsInRoot
                        .right
            ) / 2
        val iconAreaCenter =
            composeRule
                .onNodeWithTag(CALENDAR_HOME_WEATHER_ICONS_TEST_TAG, useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot
                .center
                .x
        (abs(iconGroupCenter - iconAreaCenter) <= CENTER_ALIGNMENT_TOLERANCE_PX) shouldBe true
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-041 오늘이 아닌 날짜의 모든 날씨 아이콘과 최저 최고 기온이 표시된다`() {
        val weather =
            calendarWeather(
                date = july(day = 16),
                temperature = CalendarWeatherTemperature.MinMax(min = 17.8, max = 28.2),
                descriptionList = listOf(CLOUDY_DESCRIPTION, RAINY_DESCRIPTION),
            )

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            weatherViewModel = weatherViewModel(weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(weather)))),
        )

        val cloudyLeft = composeRule.iconLeft(description = CLOUDY_DESCRIPTION)
        val rainyLeft = composeRule.iconLeft(description = RAINY_DESCRIPTION)
        (cloudyLeft < rainyLeft) shouldBe true
        composeRule.onNodeWithText(MIN_MAX_TEMPERATURE_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-062 온도 표시가 없는 날짜는 날씨 아이콘만 표시된다`() {
        val weather =
            calendarWeather(
                date = july(day = 16),
                temperature = CalendarWeatherTemperature.None,
                descriptionList = listOf(CLOUDY_DESCRIPTION),
            )

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            weatherViewModel = weatherViewModel(weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(weather)))),
        )

        composeRule.onNodeWithContentDescription(CLOUDY_DESCRIPTION, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onAllNodes(hasText(text = TEMPERATURE_UNIT, substring = true)).assertCountEquals(0)
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-042 한 번에 보이지 않는 날씨 아이콘을 가로로 스크롤해 확인한다`() {
        val descriptionList = List(size = WEATHER_ICON_COUNT) { index -> "$WEATHER_DESCRIPTION_PREFIX$index" }
        val weather =
            calendarWeather(
                date = july(day = 15),
                temperature = CalendarWeatherTemperature.Current(value = 24.3),
                descriptionList = descriptionList,
            )

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            weatherViewModel = weatherViewModel(weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(weather)))),
        )

        composeRule.onNodeWithContentDescription(descriptionList.last(), useUnmergedTree = true).assertIsNotDisplayed()

        repeat(HORIZONTAL_SWIPE_COUNT) {
            composeRule.onNodeWithTag(CALENDAR_HOME_WEATHER_ICONS_TEST_TAG, useUnmergedTree = true).performTouchInput { swipeLeft() }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(descriptionList.last(), useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-043 연속된 같은 날씨 이미지만 하나로 표시한다`() {
        val firstSunnyDescription = "첫 번째 맑음"
        val consecutiveSunnyDescription = "연속 맑음"
        val cloudyDescription = "흐림"
        val lastSunnyDescription = "다시 맑음"
        val sunnyImageUrl = "https://openweathermap.org/img/wn/01d.png"
        val cloudyImageUrl = "https://openweathermap.org/img/wn/02d.png"
        val weather =
            calendarWeather(
                date = july(day = 15),
                temperature = CalendarWeatherTemperature.Current(value = 24.3),
                descriptionList =
                    listOf(
                        firstSunnyDescription,
                        consecutiveSunnyDescription,
                        cloudyDescription,
                        lastSunnyDescription,
                    ),
                imageUrlList =
                    listOf(
                        sunnyImageUrl,
                        sunnyImageUrl,
                        cloudyImageUrl,
                        sunnyImageUrl,
                    ),
            )

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            weatherViewModel = weatherViewModel(weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(weather)))),
        )

        composeRule.onNodeWithContentDescription(consecutiveSunnyDescription, useUnmergedTree = true).assertDoesNotExist()
        val firstSunnyLeft = composeRule.iconLeft(description = firstSunnyDescription)
        val cloudyLeft = composeRule.iconLeft(description = cloudyDescription)
        val lastSunnyLeft = composeRule.iconLeft(description = lastSunnyDescription)
        (firstSunnyLeft < cloudyLeft) shouldBe true
        (cloudyLeft < lastSunnyLeft) shouldBe true
    }

    private fun setCalendarHomeScreen(
        initialYearMonth: YearMonth,
        weatherViewModel: CalendarHomeWeatherViewModel,
    ) {
        val holidayViewModel =
            mockk<CalendarHomeHolidayViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.holidayList } returns MutableStateFlow(emptyList())
                every { viewModel.isFetching } returns MutableStateFlow(false)
            }
        val memoViewModel =
            mockk<CalendarHomeMemoViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.memoList } returns MutableStateFlow(emptyList())
                every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
            }

        composeRule.setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = initialYearMonth),
                )

            DiaryTheme {
                CalendarHomeScreen(
                    navigateToMemoDetail = {},
                    navigateToMemoAdd = {},
                    navigateToContactDetail = {},
                    birthdayViewModel = birthdayViewModel(),
                    navigateToFilter = {},
                    state = state,
                    holidayViewModel = holidayViewModel,
                    memoViewModel = memoViewModel,
                    weatherViewModel = weatherViewModel,
                    syncViewModel = syncViewModel(),
                    locationPermissionRequester = rememberLocationPermissionRequester(),
                )
            }
        }
    }

    companion object {
        private const val SUNNY_DESCRIPTION = "맑음"
        private const val CLOUDY_DESCRIPTION = "흐림"
        private const val RAINY_DESCRIPTION = "비"
        private const val WEATHER_DESCRIPTION_PREFIX = "날씨"
        private const val CURRENT_TEMPERATURE_TEXT = "24.3°"
        private const val MIN_MAX_TEMPERATURE_TEXT = "17.8°/28.2°"
        private const val TEMPERATURE_UNIT = "°"
        private const val MONTH_MOVE_COUNT_TO_OCTOBER = 3
        private const val WEATHER_ICON_COUNT = 8
        private const val HORIZONTAL_SWIPE_COUNT = 4
        private const val CENTER_ALIGNMENT_TOLERANCE_PX = 1F
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
    }
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.iconLeft(description: String): Float =
    onNodeWithContentDescription(description, useUnmergedTree = true)
        .assertExists()
        .fetchSemanticsNode()
        .boundsInRoot
        .left

private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)
