package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.search.weatherSearchUri
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class CalendarHomeScreenWeatherSearchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-067 날씨를 누르면 지역명과 함께 검색한 결과가 브라우저에서 열린다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)

        setCalendarHomeScreen(
            weatherList = listOf(todayWeather()),
            locationName = SEONGNAM,
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).performClick()

        composeRule.runOnIdle {
            verify(exactly = 1) { uriHandler.openUri(weatherSearchUri(locationName = SEONGNAM)) }
        }
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-068 지역명이 없으면 날씨만으로 검색한 결과가 열린다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)

        setCalendarHomeScreen(
            weatherList = listOf(todayWeather()),
            locationName = "",
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).performClick()

        composeRule.runOnIdle {
            verify(exactly = 1) { uriHandler.openUri(weatherSearchUri(locationName = "")) }
        }
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-069 어느 날짜의 날씨를 눌러도 같은 검색 결과가 열린다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)

        setCalendarHomeScreen(
            weatherList = listOf(todayWeather(), tomorrowWeather()),
            locationName = SEONGNAM,
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).performClick()
        composeRule.onNodeWithText(MIN_MAX_TEMPERATURE_TEXT).performClick()

        composeRule.runOnIdle {
            verify(exactly = 2) { uriHandler.openUri(weatherSearchUri(locationName = SEONGNAM)) }
        }
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-070 날씨를 눌러도 보던 달과 날씨 표시가 유지된다`() {
        setCalendarHomeScreen(
            weatherList = listOf(todayWeather()),
            locationName = SEONGNAM,
            uriHandler = mockk(relaxed = true),
        )

        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).performClick()

        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(JULY_2026)).assertIsDisplayed()
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-071 브라우저를 열지 못해도 화면이 유지되고 별도 안내가 표시되지 않는다`() {
        val uriHandler =
            mockk<UriHandler> {
                every { openUri(any()) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>())
            }

        setCalendarHomeScreen(
            weatherList = listOf(todayWeather()),
            locationName = SEONGNAM,
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).performClick()

        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(JULY_2026)).assertIsDisplayed()
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertIsDisplayed()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-072 지역명이 준비되면 조작 없이 그 지역명으로 검색한다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        val weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(todayWeather())))

        setCalendarHomeScreen(
            weatherReportFlow = weatherReportFlow,
            uriHandler = uriHandler,
        )

        weatherReportFlow.value = weatherReportFlow.value.copy(locationName = SEONGNAM)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).performClick()

        composeRule.runOnIdle {
            verify(exactly = 1) { uriHandler.openUri(weatherSearchUri(locationName = SEONGNAM)) }
        }
    }

    @Test
    fun `캘린더에 표시된 날씨는 버튼으로 알려진다`() {
        setCalendarHomeScreen(
            weatherList = listOf(todayWeather()),
            locationName = SEONGNAM,
            uriHandler = mockk(relaxed = true),
        )

        composeRule
            .onNodeWithText(CURRENT_TEMPERATURE_TEXT)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }

    private fun setCalendarHomeScreen(
        uriHandler: UriHandler,
        weatherList: List<CalendarWeather> = emptyList(),
        locationName: String = "",
        weatherReportFlow: MutableStateFlow<CalendarWeatherReport> =
            MutableStateFlow(CalendarWeatherReport(weatherList = weatherList, locationName = locationName)),
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
                    calendarState = rememberCalendarState(initialYearMonth = JULY_2026),
                )

            DiaryTheme {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                    CalendarHomeScreen(
                        navigateToMemoDetail = {},
                        navigateToMemoAdd = {},
                        navigateToContactDetail = {},
                        birthdayViewModel = birthdayViewModel(),
                        navigateToFilter = {},
                        navigateToTimetable = {},
                        state = state,
                        holidayViewModel = holidayViewModel,
                        memoViewModel = memoViewModel,
                        weatherViewModel = weatherViewModel(weatherReportFlow = weatherReportFlow),
                        syncViewModel = syncViewModel(),
                        permissionManager = rememberPermissionManager(),
                    )
                }
            }
        }
    }

    private fun todayWeather(): CalendarWeather =
        calendarWeather(
            date = july(day = 15),
            temperature = CalendarWeatherTemperature.Current(value = 24.3),
            descriptionList = listOf(SUNNY_DESCRIPTION),
        )

    private fun tomorrowWeather(): CalendarWeather =
        calendarWeather(
            date = july(day = 16),
            temperature = CalendarWeatherTemperature.MinMax(min = 17.8, max = 28.2),
            descriptionList = listOf(CLOUDY_DESCRIPTION),
        )

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    companion object {
        private const val SEONGNAM = "성남시"
        private const val SUNNY_DESCRIPTION = "맑음"
        private const val CLOUDY_DESCRIPTION = "흐림"
        private const val CURRENT_TEMPERATURE_TEXT = "24.3°"
        private const val MIN_MAX_TEMPERATURE_TEXT = "17.8°/28.2°"
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
    }
}
