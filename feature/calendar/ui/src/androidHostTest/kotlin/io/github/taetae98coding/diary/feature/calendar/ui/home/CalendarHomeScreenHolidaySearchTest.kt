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
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.search.holidaySearchUri
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
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
@Config(sdk = [36])
class CalendarHomeScreenHolidaySearchTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-022 공휴일 이름을 누르면 그 이름으로 검색한 결과가 브라우저에서 열린다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            holidayList =
                listOf(
                    holiday(name = CONSTITUTION_DAY_NAME, isHoliday = true, date = july(day = 17)),
                    holiday(name = MIDSUMMER_DAY_NAME, isHoliday = false, date = july(day = 20)),
                ),
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).performClick()
        composeRule.onNodeWithText(MIDSUMMER_DAY_NAME).performClick()

        composeRule.runOnIdle {
            verifySequence {
                uriHandler.openUri(holidaySearchUri(name = CONSTITUTION_DAY_NAME))
                uriHandler.openUri(holidaySearchUri(name = MIDSUMMER_DAY_NAME))
            }
        }
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-023 여러 주에 나뉘어 표시된 공휴일 이름은 어느 조각을 눌러도 같은 검색 결과가 열린다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)

        setCalendarHomeScreen(
            initialYearMonth = FEBRUARY_2026,
            holidayList =
                listOf(
                    Holiday(
                        name = LONG_HOLIDAY_NAME,
                        isHoliday = true,
                        dateRange = february(day = 13)..february(day = 15),
                    ),
                ),
            uriHandler = uriHandler,
        )

        val nameNodes = composeRule.onAllNodesWithText(LONG_HOLIDAY_NAME)
        nameNodes.fetchSemanticsNodes().size shouldBe 2

        nameNodes[0].performClick()
        nameNodes[1].performClick()

        composeRule.runOnIdle {
            verify(exactly = 2) {
                uriHandler.openUri(holidaySearchUri(name = LONG_HOLIDAY_NAME))
            }
        }
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-024 공휴일 이름을 눌러도 보던 달과 공휴일 표시가 유지된다`() {
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            holidayList = listOf(holiday(name = CONSTITUTION_DAY_NAME, isHoliday = true, date = july(day = 17))),
            uriHandler = mockk(relaxed = true),
        )

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).performClick()

        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(JULY_2026)).assertIsDisplayed()
        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-025 브라우저를 열지 못해도 화면이 유지되고 별도 안내가 표시되지 않는다`() {
        val uriHandler =
            mockk<UriHandler> {
                every { openUri(any()) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>())
            }

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            holidayList = listOf(holiday(name = CONSTITUTION_DAY_NAME, isHoliday = true, date = july(day = 17))),
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).performClick()

        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(JULY_2026)).assertIsDisplayed()
        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).assertIsDisplayed()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
    }

    @Test
    fun `캘린더에 표시된 공휴일 이름은 버튼으로 알려진다`() {
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            holidayList =
                listOf(
                    holiday(name = CONSTITUTION_DAY_NAME, isHoliday = true, date = july(day = 17)),
                    holiday(name = MIDSUMMER_DAY_NAME, isHoliday = false, date = july(day = 20)),
                ),
            uriHandler = mockk(relaxed = true),
        )

        val buttonRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).assert(buttonRole)
        composeRule.onNodeWithText(MIDSUMMER_DAY_NAME).assert(buttonRole)
    }

    private fun setCalendarHomeScreen(
        initialYearMonth: YearMonth,
        holidayList: List<Holiday>,
        uriHandler: UriHandler,
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
                    calendarState = rememberCalendarState(initialYearMonth = initialYearMonth),
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
                        weatherViewModel = weatherViewModel(),
                        syncViewModel = syncViewModel(),
                        permissionManager = rememberPermissionManager(),
                    )
                }
            }
        }
    }

    private fun holiday(
        name: String,
        isHoliday: Boolean,
        date: LocalDate,
    ): Holiday =
        Holiday(
            name = name,
            isHoliday = isHoliday,
            dateRange = date..date,
        )

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    private fun february(day: Int): LocalDate = LocalDate(year = 2026, month = Month.FEBRUARY, day = day)

    companion object {
        private const val CONSTITUTION_DAY_NAME = "제헌절"
        private const val MIDSUMMER_DAY_NAME = "초복"
        private const val LONG_HOLIDAY_NAME = "연휴"
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
        private val FEBRUARY_2026 = YearMonth(year = 2026, month = Month.FEBRUARY)
    }
}
