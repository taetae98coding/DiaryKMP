package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
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
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarMemoUseCase
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
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
@Config(sdk = [36])
class CalendarHomeScreenMemoTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-028 표시 중인 달에 걸친 메모의 제목이 캘린더에 표시된다`() {
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            memoListFlow = MutableStateFlow(listOf(memo(title = TRIP_TITLE, start = july(day = 7), endInclusive = july(day = 9)))),
        )

        composeRule.onNodeWithText(TRIP_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-029 캘린더에 보이는 주와 겹치지 않는 메모는 표시되지 않는다`() {
        val mayDay = LocalDate(year = 2026, month = Month.MAY, day = 1)

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            memoListFlow = MutableStateFlow(listOf(memo(title = SPRING_TITLE, start = mayDay, endInclusive = mayDay))),
        )

        composeRule.onNodeWithText(SPRING_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-031 메모가 바뀌면 조작 없이 캘린더 표시가 반영된다`() {
        val memoListFlow = MutableStateFlow(emptyList<CalendarMemo>())
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            memoListFlow = memoListFlow,
        )
        composeRule.onNodeWithText(APPOINTMENT_TITLE).assertDoesNotExist()

        memoListFlow.value = listOf(memo(title = APPOINTMENT_TITLE, start = july(day = 10), endInclusive = july(day = 10)))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(APPOINTMENT_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-019 메모 조회가 실패해도 메모 없이 표시하고 공휴일 표시를 막지 않는다`() {
        val getCalendarMemoUseCase = mockk<GetCalendarMemoUseCase>()
        every { getCalendarMemoUseCase(parameter = any()) } returns
            flowOf(Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())))
        val getCalendarFilterUseCase = mockk<GetCalendarFilterUseCase>()
        every { getCalendarFilterUseCase(parameter = Unit) } returns flowOf(Result.success(emptyList()))
        val memoViewModel =
            CalendarHomeMemoViewModel(
                getCalendarFilterUseCase = getCalendarFilterUseCase,
                getCalendarMemoUseCase = getCalendarMemoUseCase,
                moveMemoUseCase = mockk(),
            )
        val holiday =
            Holiday(
                name = CONSTITUTION_DAY_NAME,
                isHoliday = true,
                dateRange = july(day = 17)..july(day = 17),
            )

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            holidayList = listOf(holiday),
            memoViewModel = memoViewModel,
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-032 메모 제목을 누르면 그 메모의 MemoDetail 화면으로 이동한다`() {
        val memo = memo(title = TRIP_TITLE, start = july(day = 7), endInclusive = july(day = 9))
        val navigatedIdList = mutableListOf<Uuid>()

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            memoListFlow = MutableStateFlow(listOf(memo)),
            navigateToMemoDetail = { id -> navigatedIdList.add(id) },
        )

        composeRule.onNodeWithText(TRIP_TITLE).performClick()

        navigatedIdList shouldBe listOf(memo.id)
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-033 여러 주에 나뉘어 표시된 메모 제목은 어느 조각을 눌러도 같은 메모의 MemoDetail 화면으로 이동한다`() {
        val memo =
            memo(
                title = LONG_TRIP_TITLE,
                start = LocalDate(year = 2026, month = Month.FEBRUARY, day = 13),
                endInclusive = LocalDate(year = 2026, month = Month.FEBRUARY, day = 15),
            )
        val navigatedIdList = mutableListOf<Uuid>()

        setCalendarHomeScreen(
            initialYearMonth = FEBRUARY_2026,
            memoListFlow = MutableStateFlow(listOf(memo)),
            navigateToMemoDetail = { id -> navigatedIdList.add(id) },
        )

        val titleNodes = composeRule.onAllNodesWithText(LONG_TRIP_TITLE)
        titleNodes.fetchSemanticsNodes().size shouldBe 2
        titleNodes[0].performClick()
        titleNodes[1].performClick()

        navigatedIdList shouldBe listOf(memo.id, memo.id)
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-060 하루 일정인 메모와 종일 기간인 메모의 제목이 모두 캘린더에 표시된다`() {
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            memoListFlow =
                MutableStateFlow(
                    listOf(
                        timedMemo(title = MEETING_TITLE, date = july(day = 7), startHour = 9, endHour = 10),
                        memo(title = TRIP_TITLE, start = july(day = 9), endInclusive = july(day = 9)),
                    ),
                ),
        )

        composeRule.onNodeWithText(MEETING_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(TRIP_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-061 하루 일정인 메모의 제목을 누르면 그 메모의 MemoDetail 화면으로 이동한다`() {
        val memo = timedMemo(title = MEETING_TITLE, date = july(day = 7), startHour = 9, endHour = 10)
        val navigatedIdList = mutableListOf<Uuid>()

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            memoListFlow = MutableStateFlow(listOf(memo)),
            navigateToMemoDetail = { id -> navigatedIdList.add(id) },
        )

        composeRule.onNodeWithText(MEETING_TITLE).performClick()

        navigatedIdList shouldBe listOf(memo.id)
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-064 여러 날에 걸친 시각이 있는 기간인 메모의 제목도 캘린더에 표시된다`() {
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            memoListFlow =
                MutableStateFlow(
                    listOf(
                        timedMemo(
                            title = WORKSHOP_TITLE,
                            date = july(day = 7),
                            startHour = 9,
                            endHour = 10,
                            endDate = july(day = 9),
                        ),
                    ),
                ),
        )

        composeRule.onNodeWithText(WORKSHOP_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-065 여러 날에 걸친 시각이 있는 기간인 메모의 제목을 누르면 그 메모의 MemoDetail 화면으로 이동한다`() {
        val memo =
            timedMemo(
                title = WORKSHOP_TITLE,
                date = july(day = 7),
                startHour = 9,
                endHour = 10,
                endDate = july(day = 9),
            )
        val navigatedIdList = mutableListOf<Uuid>()

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            memoListFlow = MutableStateFlow(listOf(memo)),
            navigateToMemoDetail = { id -> navigatedIdList.add(id) },
        )

        composeRule.onNodeWithText(WORKSHOP_TITLE).performClick()

        navigatedIdList shouldBe listOf(memo.id)
    }

    @Test
    fun `캘린더에 표시된 메모 제목은 버튼으로 알려진다`() {
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            memoListFlow = MutableStateFlow(listOf(memo(title = TRIP_TITLE, start = july(day = 7), endInclusive = july(day = 9)))),
        )

        val buttonRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)

        composeRule.onNodeWithText(TRIP_TITLE).assert(buttonRole)
    }

    private fun setCalendarHomeScreen(
        initialYearMonth: YearMonth,
        memoListFlow: StateFlow<List<CalendarMemo>>,
        navigateToMemoDetail: (Uuid) -> Unit = {},
    ) {
        val memoViewModel =
            mockk<CalendarHomeMemoViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.memoList } returns memoListFlow
                every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
            }

        setCalendarHomeScreen(
            initialYearMonth = initialYearMonth,
            holidayList = emptyList(),
            memoViewModel = memoViewModel,
            navigateToMemoDetail = navigateToMemoDetail,
        )
    }

    private fun setCalendarHomeScreen(
        initialYearMonth: YearMonth,
        holidayList: List<Holiday>,
        memoViewModel: CalendarHomeMemoViewModel,
        navigateToMemoDetail: (Uuid) -> Unit = {},
    ) {
        val holidayViewModel =
            mockk<CalendarHomeHolidayViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.holidayList } returns MutableStateFlow(holidayList)
                every { viewModel.isFetching } returns MutableStateFlow(false)
            }

        composeRule.setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = initialYearMonth),
                )

            DiaryTheme {
                CalendarHomeScreen(
                    navigateToMemoDetail = navigateToMemoDetail,
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

    private fun memo(
        title: String,
        start: LocalDate,
        endInclusive: LocalDate,
    ): CalendarMemo =
        CalendarMemo(
            id = Uuid.random(),
            title = title,
            color = fixtureMonkey.giveMeOne(),
            dateTime = MemoDateTime.AllDay(dateRange = start..endInclusive),
        )

    private fun timedMemo(
        title: String,
        date: LocalDate,
        startHour: Int,
        endHour: Int,
        endDate: LocalDate = date,
    ): CalendarMemo =
        CalendarMemo(
            id = Uuid.random(),
            title = title,
            color = fixtureMonkey.giveMeOne(),
            dateTime =
                MemoDateTime.DateTime(
                    start = LocalDateTime(date = date, time = LocalTime(hour = startHour, minute = 0)),
                    endInclusive = LocalDateTime(date = endDate, time = LocalTime(hour = endHour, minute = 0)),
                ),
        )

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    companion object {
        private const val TRIP_TITLE = "여행"
        private const val LONG_TRIP_TITLE = "연휴 여행"
        private const val SPRING_TITLE = "봄맞이"
        private const val APPOINTMENT_TITLE = "약속"
        private const val MEETING_TITLE = "회의"
        private const val WORKSHOP_TITLE = "워크샵"
        private const val CONSTITUTION_DAY_NAME = "제헌절"
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
        private val FEBRUARY_2026 = YearMonth(year = 2026, month = Month.FEBRUARY)
    }
}
