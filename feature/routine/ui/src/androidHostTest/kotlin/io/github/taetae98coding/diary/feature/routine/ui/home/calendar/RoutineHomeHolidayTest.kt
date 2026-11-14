package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetCalendarHolidayUseCase
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.JULY_15_2026
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.JULY_2026
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.KOREAN_LIST_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.holiday
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.july
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.koreanTitle
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.record
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h2000dp")
class RoutineHomeHolidayTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var holidayColor = Color.Unspecified
    private lateinit var scaffoldState: RoutineHomeScaffoldState

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-e027a476-9087-40e4-ba44-0671c2468720 달력 보기에 공휴일 이름을 표시하고 공휴일 여부가 참인 날짜만 강조한다`() {
        setRoutineHomeScreen(
            holidayList =
                listOf(
                    holiday(name = CONSTITUTION_DAY_NAME, date = july(day = 17), isHoliday = true),
                    holiday(name = MIDSUMMER_DAY_NAME, date = july(day = 20), isHoliday = false),
                ),
        )

        composeRule.routineHomeCalendarItemBoundsOn(text = CONSTITUTION_DAY_NAME, date = july(day = 17), yearMonth = JULY_2026) shouldHaveSize 1
        composeRule.routineHomeCalendarItemBoundsOn(text = MIDSUMMER_DAY_NAME, date = july(day = 20), yearMonth = JULY_2026) shouldHaveSize 1
        composeRule.routineHomeCalendarColoredDateCount(day = 17, color = holidayColor) shouldBe 1
        composeRule.routineHomeCalendarColoredDateCount(day = 20, color = holidayColor) shouldBe 0
    }

    @Test
    fun `TC-e711a815-288c-4f22-9283-2418f2d46824 공휴일이 준비되면 조작 없이 이름과 날짜 강조가 반영된다`() {
        val holidayUiState = MutableStateFlow(RoutineHomeHolidayUiState())
        setRoutineHomeScreen(holidayViewModel = RoutineHomeCalendarTestFixture.holidayViewModel(uiState = holidayUiState))
        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).assertDoesNotExist()
        composeRule.routineHomeCalendarColoredDateCount(day = 17, color = holidayColor) shouldBe 0

        composeRule.runOnIdle {
            holidayUiState.value = RoutineHomeHolidayUiState(holidayList = listOf(holiday(name = CONSTITUTION_DAY_NAME, date = july(day = 17))))
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).assertIsDisplayed()
        composeRule.routineHomeCalendarColoredDateCount(day = 17, color = holidayColor) shouldBe 1
    }

    @Test
    fun `TC-5fe075ae-c61f-47db-a75d-c6709f5123f5 공휴일 이름을 선택하면 그 이름으로 검색한 결과를 앱 밖의 브라우저로 연다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        setRoutineHomeScreen(
            holidayList = listOf(holiday(name = CONSTITUTION_DAY_NAME, date = july(day = 17))),
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).performClick()

        composeRule.runOnIdle {
            verify(exactly = 1) { uriHandler.openUri(CONSTITUTION_DAY_SEARCH_URI) }
            verify(exactly = 1) { uriHandler.openUri(any()) }
        }
    }

    @Test
    fun `공휴일 이름을 길게 누른 뒤 떼어도 브라우저를 열지 않는다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        setRoutineHomeScreen(
            holidayList = listOf(holiday(name = CONSTITUTION_DAY_NAME, date = july(day = 17))),
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).performTouchInput { longClick() }

        composeRule.runOnIdle { verify(exactly = 0) { uriHandler.openUri(any()) } }
    }

    @Test
    fun `TC-44cf2bcc-b5c7-47ea-9cd8-d74103335b86 공휴일 이름을 선택해 브라우저에 다녀와도 달력 보기와 보던 달을 그대로 본다`() {
        val october = YearMonth(year = 2026, month = Month.OCTOBER)
        val uriHandler = mockk<UriHandler>(relaxed = true)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        setRoutineHomeScreen(
            holidayList = listOf(holiday(name = HANGUL_DAY_NAME, date = LocalDate(year = 2026, month = Month.OCTOBER, day = 9))),
            uriHandler = uriHandler,
            lifecycleOwner = lifecycleOwner,
        )
        repeat(october.month.ordinal - JULY_2026.month.ordinal) { composeRule.routineHomeCalendarMoveToNextMonth() }
        composeRule.onNodeWithText(koreanTitle(october)).assertIsDisplayed()

        composeRule.onNodeWithText(HANGUL_DAY_NAME).performClick()
        composeRule.runOnIdle { verify(exactly = 1) { uriHandler.openUri(any()) } }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(KOREAN_LIST_VIEW_MODE_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithText(koreanTitle(october)).assertIsDisplayed()
        composeRule.routineHomeCalendarItemBoundsOn(text = HANGUL_DAY_NAME, date = LocalDate(year = 2026, month = Month.OCTOBER, day = 9), yearMonth = october) shouldHaveSize 1
    }

    @Test
    fun `TC-1a13e707-47be-4d45-8c5f-ead9273961cb 공휴일 선택에서 브라우저를 열지 못하면 화면이 유지되고 검색 결과를 열지 못했다고 알린다`() {
        val uriHandler =
            mockk<UriHandler> {
                every { openUri(any()) } throws IllegalStateException(Arb.nonBlankString().next())
            }
        setRoutineHomeScreen(
            holidayList = listOf(holiday(name = CONSTITUTION_DAY_NAME, date = july(day = 17))),
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(KOREAN_LIST_VIEW_MODE_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithText(koreanTitle(JULY_2026)).assertIsDisplayed()
        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).assertIsDisplayed()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        composeRule.onNodeWithText(SEARCH_OPEN_FAILED_MESSAGE).assertIsDisplayed()
    }

    // 숨김으로 고른 공휴일은 달력 공휴일 조회가 빼고 내주므로, 화면이 그 결과에 없는 공휴일을 이름으로도 날짜 강조로도 드러내지 않는지 본다.
    @Test
    fun `TC-6634424a-9197-4734-9b5e-20f24b07aa77 숨김으로 고른 공휴일은 달력 보기에 이름도 날짜 강조도 표시하지 않는다`() {
        val august = YearMonth(year = 2026, month = Month.AUGUST)
        val liberationDay = LocalDate(year = 2026, month = Month.AUGUST, day = 15)
        setRoutineHomeScreen(
            holidayViewModel = holidayViewModel(holidayListFlowByYear = mapOf(2026 to flowOf(listOf(holiday(name = LIBERATION_DAY_NAME, date = liberationDay))))),
        )

        composeRule.onNodeWithText(koreanTitle(JULY_2026)).assertIsDisplayed()
        composeRule.onAllNodesWithText(CONSTITUTION_DAY_NAME).assertCountEquals(0)
        composeRule.routineHomeCalendarColoredDateCount(day = 17, color = holidayColor) shouldBe 0

        composeRule.routineHomeCalendarMoveToNextMonth()

        composeRule.onNodeWithText(koreanTitle(august)).assertIsDisplayed()
        composeRule.routineHomeCalendarItemBoundsOn(text = LIBERATION_DAY_NAME, date = liberationDay, yearMonth = august) shouldHaveSize 1
        composeRule.routineHomeCalendarColoredDateCount(day = 15, color = holidayColor) shouldBe 1
    }

    @Test
    fun `TC-ed96a045-837e-4f19-9c3a-95312032f3c6 표시 대상 연도에 저장된 공휴일을 달력 보기에 표시하고 바뀌면 조작 없이 반영한다`() {
        val holidayListFlow = MutableStateFlow(listOf(holiday(name = CONSTITUTION_DAY_NAME, date = july(day = 17))))
        setRoutineHomeScreen(holidayViewModel = holidayViewModel(holidayListFlowByYear = mapOf(2026 to holidayListFlow)))
        composeRule.routineHomeCalendarItemBoundsOn(text = CONSTITUTION_DAY_NAME, date = july(day = 17), yearMonth = JULY_2026) shouldHaveSize 1

        composeRule.runOnIdle { holidayListFlow.value = listOf(holiday(name = TEMPORARY_HOLIDAY_NAME, date = july(day = 20))) }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(CONSTITUTION_DAY_NAME).assertCountEquals(0)
        composeRule.routineHomeCalendarItemBoundsOn(text = TEMPORARY_HOLIDAY_NAME, date = july(day = 20), yearMonth = JULY_2026) shouldHaveSize 1
    }

    @Test
    fun `TC-83a0068c-783e-4745-bf07-e2136d93ef45 대상 연도에 저장된 공휴일이 없으면 공휴일 없이 달력을 표시한다`() {
        setRoutineHomeScreen(
            recordList = listOf(record(title = WORKOUT_TITLE, date = july(day = 10))),
            holidayViewModel = holidayViewModel(getCalendarHolidayUseCase = getCalendarHolidayUseCase(result = Result.success(emptyList()))),
        )

        assertCalendarWithoutHoliday(errorMessage = null)
    }

    @Test
    fun `TC-83a0068c-783e-4745-bf07-e2136d93ef45 대상 연도의 공휴일 조회에 실패하면 공휴일 없이 달력을 표시한다`() {
        val errorMessage = Arb.nonBlankString().next()
        setRoutineHomeScreen(
            recordList = listOf(record(title = WORKOUT_TITLE, date = july(day = 10))),
            holidayViewModel = holidayViewModel(getCalendarHolidayUseCase = getCalendarHolidayUseCase(result = Result.failure(IllegalStateException(errorMessage)))),
        )

        assertCalendarWithoutHoliday(errorMessage = errorMessage)
    }

    @Test
    fun `TC-90d5f4db-fa74-483b-9b4b-988d84dc6669 공휴일 동기화가 실패해도 알리지 않고 다른 대상 연도의 동기화와 달력 표시를 계속한다`() {
        val december = YearMonth(year = 2026, month = Month.DECEMBER)
        val recordDate = LocalDate(year = 2026, month = Month.DECEMBER, day = 10)
        val errorMessage = Arb.nonBlankString().next()
        val fetchHolidayUseCase = mockk<FetchHolidayUseCase>()
        coEvery { fetchHolidayUseCase(parameter = 2026) } returns Result.failure(IllegalStateException(errorMessage))
        coEvery { fetchHolidayUseCase(parameter = 2027) } returns Result.success(emptyList())
        setRoutineHomeScreen(
            initialYearMonth = december,
            recordList = listOf(record(title = WORKOUT_TITLE, date = recordDate)),
            holidayViewModel = holidayViewModel(fetchHolidayUseCase = fetchHolidayUseCase),
        )

        coVerify(exactly = 1) { fetchHolidayUseCase(parameter = 2026) }
        coVerify(exactly = 1) { fetchHolidayUseCase(parameter = 2027) }
        composeRule.onNodeWithText(koreanTitle(december)).assertIsDisplayed()
        composeRule.routineHomeCalendarItemBoundsOn(text = WORKOUT_ONCE_TEXT, date = recordDate, yearMonth = december) shouldHaveSize 1
        assertNoNotice(errorMessage = errorMessage)
    }

    private fun assertCalendarWithoutHoliday(errorMessage: String?) {
        composeRule.onNodeWithText(koreanTitle(JULY_2026)).assertIsDisplayed()
        composeRule.routineHomeCalendarItemBoundsOn(text = WORKOUT_ONCE_TEXT, date = july(day = 10), yearMonth = JULY_2026) shouldHaveSize 1
        // 2026년 7월 달력에서 일요일이 아닌 날짜는 공휴일일 때만 강조 색상으로 그려진다.
        composeRule.routineHomeCalendarColoredDateCount(day = 17, color = holidayColor) shouldBe 0
        composeRule.onAllNodesWithText(CONSTITUTION_DAY_NAME).assertCountEquals(0)
        assertNoNotice(errorMessage = errorMessage)
    }

    private fun assertNoNotice(errorMessage: String?) {
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        composeRule.onAllNodes(isPopup()).assertCountEquals(0)
        errorMessage?.let { message -> composeRule.onAllNodes(hasText(text = message, substring = true)).assertCountEquals(0) }
        scaffoldState.snackbarHostState.currentSnackbarData shouldBe null
    }

    private fun setRoutineHomeScreen(
        initialYearMonth: YearMonth = JULY_2026,
        recordList: List<CalendarRoutineRecord> = emptyList(),
        holidayList: List<Holiday> = emptyList(),
        holidayViewModel: RoutineHomeHolidayViewModel =
            RoutineHomeCalendarTestFixture.holidayViewModel(uiState = MutableStateFlow(RoutineHomeHolidayUiState(holidayList = holidayList))),
        uriHandler: UriHandler = mockk(relaxed = true),
        lifecycleOwner: TestLifecycleOwner? = null,
    ) {
        val calendarViewModel = RoutineHomeCalendarTestFixture.calendarViewModel(uiState = MutableStateFlow(RoutineHomeCalendarUiState(recordList = recordList)))

        composeRule.setContent {
            RoutineHomeCalendarColorCapture { holidayColor, _ -> this.holidayColor = holidayColor }
            CompositionLocalProvider(
                LocalUriHandler provides uriHandler,
                LocalLifecycleOwner provides (lifecycleOwner ?: LocalLifecycleOwner.current),
            ) {
                RoutineHomeCalendarTestScreen(
                    today = JULY_15_2026,
                    calendarViewModel = calendarViewModel,
                    holidayViewModel = holidayViewModel,
                    initialYearMonth = initialYearMonth,
                    onState = { state -> scaffoldState = state },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun holidayViewModel(holidayListFlowByYear: Map<Int, Flow<List<Holiday>>>): RoutineHomeHolidayViewModel {
        val getCalendarHolidayUseCase = mockk<GetCalendarHolidayUseCase>()
        every { getCalendarHolidayUseCase(parameter = any()) } answers {
            (holidayListFlowByYear[firstArg<Int>()] ?: flowOf(emptyList())).map { holidayList -> Result.success(holidayList) }
        }

        return holidayViewModel(getCalendarHolidayUseCase = getCalendarHolidayUseCase)
    }

    private fun holidayViewModel(
        fetchHolidayUseCase: FetchHolidayUseCase = mockk<FetchHolidayUseCase>().also { useCase -> coEvery { useCase(parameter = any()) } returns Result.success(emptyList()) },
        getCalendarHolidayUseCase: GetCalendarHolidayUseCase = getCalendarHolidayUseCase(result = Result.success(emptyList())),
    ): RoutineHomeHolidayViewModel =
        RoutineHomeHolidayViewModel(
            fetchHolidayUseCase = fetchHolidayUseCase,
            getCalendarHolidayUseCase = getCalendarHolidayUseCase,
        )

    private fun getCalendarHolidayUseCase(result: Result<List<Holiday>>): GetCalendarHolidayUseCase =
        mockk<GetCalendarHolidayUseCase>().also { useCase ->
            every { useCase(parameter = any()) } returns flowOf(result)
        }

    private companion object {
        const val CONSTITUTION_DAY_NAME = "제헌절"
        const val CONSTITUTION_DAY_SEARCH_URI = "https://search.naver.com/search.naver?query=%EC%A0%9C%ED%97%8C%EC%A0%88"
        const val MIDSUMMER_DAY_NAME = "초복"
        const val HANGUL_DAY_NAME = "한글날"
        const val LIBERATION_DAY_NAME = "광복절"
        const val TEMPORARY_HOLIDAY_NAME = "임시공휴일"
        const val WORKOUT_TITLE = "운동"
        const val WORKOUT_ONCE_TEXT = "운동 1회"
        const val SEARCH_OPEN_FAILED_MESSAGE = "검색 결과를 열지 못했습니다."
    }
}
