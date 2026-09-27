package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.core.testing.memo.calendarMemo
import io.github.taetae98coding.diary.feature.calendar.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.search.holidaySearchUri
import io.github.taetae98coding.diary.feature.calendar.ui.home.search.weatherSearchUri
import io.github.taetae98coding.diary.feature.calendar.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScreenTimetableTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-103 날짜를 누르면 그 날짜의 하루 시간표 열기를 요청한다`() {
        val navKeyList = mutableListOf<CalendarTimetableNavKey>()
        setCalendarHomeScreen(navigateToTimetable = { navKeyList += it })

        composeRule.onNodeWithContentDescription("September 23").performClick()

        navKeyList shouldBe listOf(CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23)))
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-104 이웃 달 날짜를 누르면 그 실제 날짜의 하루 시간표 열기를 요청한다`() {
        val navKeyList = mutableListOf<CalendarTimetableNavKey>()
        setCalendarHomeScreen(navigateToTimetable = { navKeyList += it })

        composeRule.onAllNodesWithContentDescription("August 30")[0].performClick()

        navKeyList shouldBe
            listOf(CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.DAY, date = LocalDate(year = 2026, month = Month.AUGUST, day = 30)))
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-105 주를 누르면 그 주의 주간 시간표 열기를 요청한다`() {
        val caseList =
            listOf(
                "Week of September 20" to september(day = 20),
                "Week of August 30" to LocalDate(year = 2026, month = Month.AUGUST, day = 30),
            )
        val navKeyList = mutableListOf<CalendarTimetableNavKey>()
        setCalendarHomeScreen(navigateToTimetable = { navKeyList += it })

        caseList.forEach { (description, _) -> composeRule.onNodeWithContentDescription(description).performClick() }

        navKeyList shouldBe caseList.map { (_, startDate) -> CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.WEEK, date = startDate) }
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-106 아이템을 누르면 시간표를 열지 않고 아이템의 선택 결과가 실행된다`() {
        val memo = fixtureMonkey.calendarMemo(dateTime = allDay(day = 21), title = MEETING_TITLE)
        val birthday = CalendarContactBirthday(contactId = fixtureMonkey.giveMeOne<Uuid>(), name = BIRTHDAY_NAME, date = september(day = 22))
        val holiday = Holiday(name = HOLIDAY_NAME, isHoliday = true, dateRange = september(day = 24)..september(day = 24))
        val weather =
            calendarWeather(
                date = september(day = 25),
                temperature = CalendarWeatherTemperature.Current(value = 24.3),
                descriptionList = listOf("clear sky"),
            )
        val uriHandler = mockk<UriHandler>(relaxed = true)
        val navKeyList = mutableListOf<CalendarTimetableNavKey>()
        val memoIdList = mutableListOf<Uuid>()
        val contactIdList = mutableListOf<Uuid>()
        setCalendarHomeScreen(
            memoList = listOf(memo),
            holidayList = listOf(holiday),
            birthdayList = listOf(birthday),
            weatherReport = CalendarWeatherReport(weatherList = listOf(weather)),
            uriHandler = uriHandler,
            navigateToTimetable = { navKeyList += it },
            navigateToMemoDetail = { memoIdList += it },
            navigateToContactDetail = { contactIdList += it },
        )

        composeRule.onNodeWithText(MEETING_TITLE).performClick()
        composeRule.onNodeWithContentDescription("$BIRTHDAY_NAME's birthday").performClick()
        composeRule.onNodeWithText(HOLIDAY_NAME).performClick()
        composeRule.onNodeWithText("24.3°").performClick()
        composeRule.waitForIdle()

        memoIdList shouldBe listOf(memo.id)
        contactIdList shouldBe listOf(birthday.contactId)
        verify(exactly = 1) { uriHandler.openUri(holidaySearchUri(name = HOLIDAY_NAME)) }
        verify(exactly = 1) { uriHandler.openUri(weatherSearchUri(locationName = "")) }
        navKeyList shouldBe emptyList()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-107 날짜 칸을 길게 눌러 선택을 끝내면 시간표를 열지 않고 MemoAdd 화면 이동만 요청한다`() {
        val navKeyList = mutableListOf<CalendarTimetableNavKey>()
        val memoAddList = mutableListOf<LocalDateRange>()
        setCalendarHomeScreen(
            navigateToTimetable = { navKeyList += it },
            navigateToMemoAdd = { memoAddList += it },
        )

        performLongPress(position = dayCenter(day = 23))
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitForIdle()

        memoAddList shouldBe listOf(september(day = 23)..september(day = 23))
        navKeyList shouldBe emptyList()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-108 메모 제목을 길게 눌러 옮기면 시간표를 열지 않는다`() {
        val memo = fixtureMonkey.calendarMemo(dateTime = allDay(day = 23), title = MEETING_TITLE)
        val memoViewModel = memoViewModel(memoList = listOf(memo))
        val navKeyList = mutableListOf<CalendarTimetableNavKey>()
        setCalendarHomeScreen(
            memoViewModel = memoViewModel,
            navigateToTimetable = { navKeyList += it },
        )
        val memoBounds = composeRule.onNodeWithText(MEETING_TITLE).fetchSemanticsNode().boundsInRoot

        performLongPress(position = Offset(x = dayCenter(day = 23).x, y = memoBounds.center.y))
        composeRule.onRoot().performTouchInput {
            moveTo(Offset(x = dayCenter(day = 24).x, y = memoBounds.center.y))
            up()
        }
        composeRule.waitForIdle()

        verify(exactly = 1) {
            memoViewModel.move(id = memo.id, fromDateTime = memo.dateTime, toDateRange = september(day = 24)..september(day = 24))
        }
        navKeyList shouldBe emptyList()
    }

    private fun setCalendarHomeScreen(
        memoList: List<CalendarMemo> = emptyList(),
        memoViewModel: CalendarHomeMemoViewModel = memoViewModel(memoList = memoList),
        holidayList: List<Holiday> = emptyList(),
        birthdayList: List<CalendarContactBirthday> = emptyList(),
        weatherReport: CalendarWeatherReport = CalendarWeatherReport(),
        uriHandler: UriHandler = mockk(relaxed = true),
        navigateToTimetable: (CalendarTimetableNavKey) -> Unit,
        navigateToMemoDetail: (Uuid) -> Unit = {},
        navigateToMemoAdd: (LocalDateRange) -> Unit = {},
        navigateToContactDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = SEPTEMBER_2026),
                )

            DiaryTheme {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                    CalendarHomeScreen(
                        navigateToMemoDetail = navigateToMemoDetail,
                        navigateToMemoAdd = navigateToMemoAdd,
                        navigateToContactDetail = navigateToContactDetail,
                        navigateToFilter = {},
                        navigateToTimetable = navigateToTimetable,
                        state = state,
                        permissionManager = rememberPermissionManager(),
                        holidayViewModel = holidayViewModel(holidayListFlow = MutableStateFlow(holidayList)),
                        memoViewModel = memoViewModel,
                        birthdayViewModel = birthdayViewModel(birthdayListFlow = MutableStateFlow(birthdayList)),
                        weatherViewModel = weatherViewModel(weatherReportFlow = MutableStateFlow(weatherReport)),
                        syncViewModel = syncViewModel(),
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun memoViewModel(memoList: List<CalendarMemo>): CalendarHomeMemoViewModel =
        mockk<CalendarHomeMemoViewModel>().also { viewModel ->
            every { viewModel.fetch(any()) } returns Unit
            every { viewModel.memoList } returns MutableStateFlow(memoList)
            every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
            every { viewModel.move(any(), any(), any()) } returns Unit
        }

    private fun dayCenter(day: Int): Offset =
        composeRule
            .onAllNodes(CalendarHomeTestFixture.dateCell(day = day))[0]
            .fetchSemanticsNode()
            .boundsInRoot
            .center

    private fun performLongPress(position: Offset) {
        composeRule.onRoot().performTouchInput {
            down(position)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_MARGIN_MILLIS)
            moveBy(Offset.Zero)
        }
        composeRule.waitForIdle()
    }

    private fun september(day: Int): LocalDate = LocalDate(year = 2026, month = Month.SEPTEMBER, day = day)

    private fun allDay(day: Int): MemoDateTime = MemoDateTime.AllDay(dateRange = september(day = day)..september(day = day))

    private companion object {
        val SEPTEMBER_2026 = YearMonth(year = 2026, month = Month.SEPTEMBER)
        const val MEETING_TITLE = "Meeting"
        const val BIRTHDAY_NAME = "Hong"
        const val HOLIDAY_NAME = "Holiday"
        const val LONG_PRESS_MARGIN_MILLIS = 100L
    }
}
