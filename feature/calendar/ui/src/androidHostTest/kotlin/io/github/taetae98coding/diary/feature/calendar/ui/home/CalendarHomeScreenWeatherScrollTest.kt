package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToKey
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
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
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class CalendarHomeScreenWeatherScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val memoList = List(MEMO_COUNT) { index -> memo(title = memoTitle(index = index)) }

    @Test
    fun `TC-0fd84557-0d8c-4071-aefc-ce06e3f1ebfe 스크롤하지 않은 주에 날씨가 나중에 표시되어도 보던 메모 제목이 계속 보이고 위로 스크롤하면 날씨를 본다`() {
        assertWeatherArrivesAboveViewedMemo(viewedMemoIndex = 0)
    }

    @Test
    fun `TC-0fd84557-0d8c-4071-aefc-ce06e3f1ebfe 아래로 스크롤해 둔 주에 날씨가 나중에 표시되어도 보던 메모 제목이 계속 보이고 위로 스크롤하면 날씨를 본다`() {
        assertWeatherArrivesAboveViewedMemo(viewedMemoIndex = MEMO_COUNT - 1)
    }

    private fun assertWeatherArrivesAboveViewedMemo(viewedMemoIndex: Int) {
        val weatherReportFlow = MutableStateFlow(CalendarWeatherReport())
        setCalendarHomeScreen(weatherReportFlow = weatherReportFlow)
        if (viewedMemoIndex > 0) {
            memoWeek(containedMemoIndex = 0).performScrollToKey(memoList[viewedMemoIndex].id)
        }
        composeRule.onNodeWithText(memoTitle(index = viewedMemoIndex)).assertIsDisplayed()

        weatherReportFlow.value = weatherReport()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(memoTitle(index = viewedMemoIndex)).assertIsDisplayed()
        composeRule.onNodeWithText(TEMPERATURE_TEXT).isNotDisplayed() shouldBe true

        memoWeek(containedMemoIndex = viewedMemoIndex).performScrollToIndex(0)

        composeRule.onNodeWithText(TEMPERATURE_TEXT).assertIsDisplayed()
    }

    private fun memoWeek(containedMemoIndex: Int) = composeRule.onNode(isVerticallyScrollable() and hasAnyDescendant(hasText(text = memoTitle(index = containedMemoIndex))))

    private fun isVerticallyScrollable(): SemanticsMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)

    private fun weatherReport(): CalendarWeatherReport =
        CalendarWeatherReport(
            weatherList =
                listOf(
                    calendarWeather(
                        date = july(day = WEATHER_DAY),
                        temperature = CalendarWeatherTemperature.Current(value = TEMPERATURE),
                        descriptionList = listOf(SUNNY_DESCRIPTION),
                    ),
                ),
        )

    private fun setCalendarHomeScreen(weatherReportFlow: MutableStateFlow<CalendarWeatherReport>) {
        val memoViewModel =
            mockk<CalendarHomeMemoViewModel>().also { viewModel ->
                every { viewModel.select(any()) } returns Unit
                every { viewModel.uiState } returns MutableStateFlow(CalendarHomeMemoUiState(memoList = memoList))
                every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
            }

        composeRule.setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = JULY_2026),
                )

            DiaryTheme {
                CalendarHomeScreen(
                    navigateToMemoDetail = {},
                    navigateToMemoAdd = {},
                    navigateToContactDetail = {},
                    birthdayViewModel = birthdayViewModel(),
                    navigateToFilter = {},
                    navigateToTimetable = {},
                    state = state,
                    holidayViewModel = holidayViewModel(),
                    memoViewModel = memoViewModel,
                    weatherViewModel = weatherViewModel(weatherReportFlow = weatherReportFlow),
                    syncViewModel = syncViewModel(),
                    permissionManager = rememberPermissionManager(),
                )
            }
        }
    }

    private fun memoTitle(index: Int): String = "$MEMO_TITLE_PREFIX$index"

    private fun memo(title: String): CalendarMemo =
        CalendarMemo(
            id = Uuid.random(),
            title = title,
            color = fixtureMonkey.giveMeOne(),
            dateTime = MemoDateTime.AllDay(dateRange = july(day = MEMO_START_DAY)..july(day = WEATHER_DAY)),
        )

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    companion object {
        private const val MEMO_COUNT = 6
        private const val MEMO_TITLE_PREFIX = "메모"
        private const val MEMO_START_DAY = 12
        private const val WEATHER_DAY = 15
        private const val SUNNY_DESCRIPTION = "맑음"
        private const val TEMPERATURE = 24.3
        private const val TEMPERATURE_TEXT = "24.3°"
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
    }
}
