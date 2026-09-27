package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.domain.weather.usecase.FetchCurrentWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.GetCurrentCalendarWeatherUseCase
import io.github.taetae98coding.diary.domain.weather.usecase.RefreshCurrentWeatherUseCase
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
class CalendarHomeScreenWeatherFetchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-DATA-020 화면이 표시되면 현재 위치의 날씨를 동기화한다`() {
        val weatherViewModel = weatherViewModel()

        setCalendarHomeScreen(weatherViewModel = weatherViewModel)
        composeRule.waitForIdle()

        verify(exactly = 1) { weatherViewModel.fetch() }
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-022 날씨 동기화가 실패해도 날씨 없이 표시하고 다른 표시를 막지 않는다`() {
        val fetchCurrentWeatherUseCase = mockk<FetchCurrentWeatherUseCase>()
        coEvery { fetchCurrentWeatherUseCase(parameter = Unit) } returns
            Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
        val refreshCurrentWeatherUseCase = mockk<RefreshCurrentWeatherUseCase>()
        coEvery { refreshCurrentWeatherUseCase(parameter = Unit) } returns Result.success(Unit)
        val getCurrentCalendarWeatherUseCase = mockk<GetCurrentCalendarWeatherUseCase>()
        every { getCurrentCalendarWeatherUseCase(parameter = Unit) } returns
            flowOf(Result.success(CalendarWeatherReport()))
        val weatherViewModel =
            CalendarHomeWeatherViewModel(
                fetchCurrentWeatherUseCase = fetchCurrentWeatherUseCase,
                refreshCurrentWeatherUseCase = refreshCurrentWeatherUseCase,
                getCurrentCalendarWeatherUseCase = getCurrentCalendarWeatherUseCase,
            )
        val holiday =
            Holiday(
                name = CONSTITUTION_DAY_NAME,
                isHoliday = true,
                dateRange = july(day = 17)..july(day = 17),
            )

        setCalendarHomeScreen(
            weatherViewModel = weatherViewModel,
            holidayList = listOf(holiday),
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).assertIsDisplayed()
        composeRule.onAllNodesWithText(TEMPERATURE_UNIT, substring = true).assertCountEquals(0)
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-024 달을 이동해도 날씨를 다시 동기화하지 않는다`() {
        val weatherViewModel = weatherViewModel()

        setCalendarHomeScreen(weatherViewModel = weatherViewModel)
        composeRule.waitForIdle()

        composeRule.onRoot().performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }
        composeRule.waitForIdle()

        verify(exactly = 1) { weatherViewModel.fetch() }
    }

    private fun setCalendarHomeScreen(
        weatherViewModel: CalendarHomeWeatherViewModel,
        holidayList: List<Holiday> = emptyList(),
    ) {
        val holidayViewModel =
            mockk<CalendarHomeHolidayViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.holidayList } returns MutableStateFlow(holidayList)
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
                    weatherViewModel = weatherViewModel,
                    syncViewModel = syncViewModel(),
                    permissionManager = rememberPermissionManager(),
                )
            }
        }
    }

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    companion object {
        private const val CONSTITUTION_DAY_NAME = "제헌절"
        private const val TEMPERATURE_UNIT = "°"
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
    }
}
