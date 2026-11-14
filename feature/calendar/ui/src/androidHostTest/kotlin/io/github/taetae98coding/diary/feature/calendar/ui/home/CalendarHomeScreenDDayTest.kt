package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.core.model.dday.CalendarDDayLabel
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.testing.contact.calendarContactBirthday
import io.github.taetae98coding.diary.core.testing.dday.calendarDDay
import io.github.taetae98coding.diary.core.testing.memo.calendarMemo
import io.github.taetae98coding.diary.domain.dday.usecase.GetCalendarDDayUseCase
import io.github.taetae98coding.diary.feature.calendar.ui.home.dday.CalendarHomeDDayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h2000dp")
class CalendarHomeScreenDDayTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-71e7f51e-730e-4560-b1ca-08e50c77c4cb 디데이의 기준일 당일과 기념일이 제목과 그 날짜의 문구로 표시된다`() {
        setCalendarHomeScreen(
            ddayListFlow =
                MutableStateFlow(
                    listOf(
                        dday(title = EXAM_TITLE, date = july(day = 15), label = CalendarDDayLabel.BaseDay),
                        dday(title = US_TITLE, date = july(day = 9), label = CalendarDDayLabel.Days(days = 100)),
                    ),
                ),
        )

        composeRule.onNodeWithText(EXAM_BASE_DAY_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(US_HUNDRED_DAYS_TEXT).assertIsDisplayed()
        assertUnderDate(text = EXAM_BASE_DAY_TEXT, dateDescription = JULY_15_DESCRIPTION)
        assertUnderDate(text = US_HUNDRED_DAYS_TEXT, dateDescription = "7월 9일")
    }

    @Test
    fun `TC-084eadb7-3825-4071-9b79-77459040865a 캘린더에 보이는 주와 겹치지 않는 디데이는 표시되지 않는다`() {
        setCalendarHomeScreen(
            ddayListFlow =
                MutableStateFlow(
                    listOf(dday(title = SPRING_TITLE, date = LocalDate(year = 2026, month = Month.MAY, day = 1), label = CalendarDDayLabel.BaseDay)),
                ),
        )

        composeRule.onNodeWithText(CalendarHomeTestFixture.koreanTitle(JULY_2026)).assertIsDisplayed()
        composeRule.onNodeWithText(SPRING_TITLE, substring = true).assertDoesNotExist()
    }

    @Test
    fun `TC-5dd2ea53-a02b-4c38-8fe8-e725fcf4cc78 표시할 디데이가 없다가 준비되면 조작 없이 표시된다`() {
        val ddayListFlow = MutableStateFlow(emptyList<CalendarDDay>())
        setCalendarHomeScreen(
            ddayListFlow = ddayListFlow,
            memoList = listOf(memo(title = WALK_TITLE, date = july(day = 10))),
        )
        composeRule.onNodeWithText(WALK_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithText(DDAY_TEXT_PREFIX, substring = true).assertCountEquals(0)

        ddayListFlow.value = listOf(dday(title = EXAM_TITLE, date = july(day = 15), label = CalendarDDayLabel.BaseDay))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(EXAM_BASE_DAY_TEXT).assertIsDisplayed()
        assertUnderDate(text = EXAM_BASE_DAY_TEXT, dateDescription = JULY_15_DESCRIPTION)
    }

    @Test
    fun `TC-ad3b427f-e8dd-4c32-9ffa-1cab8a915d32 디데이를 선택하면 기준일 당일이든 기념일이든 같은 디데이의 DDayDetail 화면으로 이동한다`() {
        val babyId = Arb.kotlinUuid().next()
        val ddayList =
            listOf(
                dday(title = BABY_TITLE, date = july(day = 1), label = CalendarDDayLabel.BaseDay).copy(ddayId = babyId),
                dday(title = BABY_TITLE, date = LocalDate(year = 2026, month = Month.AUGUST, day = 1), label = CalendarDDayLabel.Months(months = 1))
                    .copy(ddayId = babyId),
            )

        val navigatedIdList = mutableListOf<Uuid>()
        setCalendarHomeScreen(
            ddayListFlow = MutableStateFlow(ddayList),
            navigateToDDayDetail = navigatedIdList::add,
        )

        listOf(BABY_BASE_DAY_TEXT, BABY_ONE_MONTH_TEXT).forEach { text ->
            navigatedIdList.clear()

            composeRule.onNodeWithText(text).performClick()
            composeRule.waitForIdle()

            navigatedIdList shouldBe listOf(babyId)
        }
    }

    @Test
    fun `TC-78293c9e-f80a-44d8-85bf-40b764d4eb9e 디데이가 다른 날짜로 바뀌면 조작 없이 바뀐 날짜에 표시된다`() {
        val exam = dday(title = EXAM_TITLE, date = july(day = 15), label = CalendarDDayLabel.BaseDay)
        val ddayListFlow = MutableStateFlow(listOf(exam))
        setCalendarHomeScreen(ddayListFlow = ddayListFlow)
        assertUnderDate(text = EXAM_BASE_DAY_TEXT, dateDescription = JULY_15_DESCRIPTION)

        ddayListFlow.value = listOf(exam.copy(date = july(day = 16)))
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(EXAM_BASE_DAY_TEXT).assertCountEquals(1)
        assertUnderDate(text = EXAM_BASE_DAY_TEXT, dateDescription = "7월 16일")
        assertNotUnderDate(text = EXAM_BASE_DAY_TEXT, dateDescription = JULY_15_DESCRIPTION)
    }

    @Test
    fun `TC-78293c9e-f80a-44d8-85bf-40b764d4eb9e 디데이의 결과가 모두 빠지면 조작 없이 캘린더에서 사라진다`() {
        val ddayListFlow = MutableStateFlow(listOf(dday(title = EXAM_TITLE, date = july(day = 15), label = CalendarDDayLabel.BaseDay)))
        setCalendarHomeScreen(ddayListFlow = ddayListFlow)
        composeRule.onNodeWithText(EXAM_BASE_DAY_TEXT).assertIsDisplayed()

        ddayListFlow.value = emptyList()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(EXAM_TITLE, substring = true).assertDoesNotExist()
    }

    @Test
    fun `TC-b1af5c1b-2acd-4c8b-bc61-b11928087a8d 디데이 조회가 실패해도 디데이 없이 표시하고 다른 표시를 막지 않는다`() {
        val errorMessage = Arb.nonBlankString().next()
        val getCalendarDDayUseCase = mockk<GetCalendarDDayUseCase>()
        every { getCalendarDDayUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(errorMessage)))

        setCalendarHomeScreen(
            ddayViewModel = CalendarHomeDDayViewModel(getCalendarDDayUseCase = getCalendarDDayUseCase),
            holidayList = listOf(Holiday(name = CONSTITUTION_DAY_NAME, isHoliday = true, dateRange = july(day = 17)..july(day = 17))),
            memoList = listOf(memo(title = APPOINTMENT_TITLE, date = july(day = 10))),
            birthdayList = listOf(Arb.calendarContactBirthday().next().copy(name = BIRTHDAY_NAME, nameQualifier = "", date = july(day = 8))),
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).assertIsDisplayed()
        composeRule.onNodeWithText(APPOINTMENT_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertIsDisplayed()
        composeRule.onAllNodesWithText(DDAY_TEXT_PREFIX, substring = true).assertCountEquals(0)
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        composeRule.onAllNodes(isPopup()).assertCountEquals(0)
        composeRule.onNodeWithText(errorMessage, substring = true).assertDoesNotExist()
    }

    private fun setCalendarHomeScreen(
        ddayListFlow: MutableStateFlow<List<CalendarDDay>> = MutableStateFlow(emptyList()),
        ddayViewModel: CalendarHomeDDayViewModel = ddayViewModel(ddayListFlow = ddayListFlow),
        holidayList: List<Holiday> = emptyList(),
        memoList: List<CalendarMemo> = emptyList(),
        birthdayList: List<CalendarContactBirthday> = emptyList(),
        navigateToDDayDetail: (Uuid) -> Unit = {},
    ) {
        val memoViewModel =
            mockk<CalendarHomeMemoViewModel>().also { viewModel ->
                every { viewModel.select(any()) } returns Unit
                every { viewModel.uiState } returns MutableStateFlow(CalendarHomeMemoUiState(memoList = memoList))
                every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
            }
        composeRule.setContent {
            val state = rememberCalendarHomeScaffoldState(calendarState = rememberCalendarState(initialYearMonth = JULY_2026))

            DiaryTheme {
                CalendarHomeScreen(
                    navigateToMemoDetail = {},
                    navigateToDDayDetail = navigateToDDayDetail,
                    navigateToMemoAdd = {},
                    navigateToContactDetail = {},
                    navigateToFilter = {},
                    navigateToTimetable = {},
                    state = state,
                    permissionManager = rememberPermissionManager(),
                    holidayViewModel = holidayViewModel(holidayListFlow = MutableStateFlow(holidayList)),
                    memoViewModel = memoViewModel,
                    ddayViewModel = ddayViewModel,
                    birthdayViewModel = birthdayViewModel(birthdayListFlow = MutableStateFlow(birthdayList)),
                    weatherViewModel = weatherViewModel(),
                    syncViewModel = syncViewModel(),
                )
            }
        }

        composeRule.waitForIdle()
    }

    private fun assertUnderDate(
        text: String,
        dateDescription: String,
    ) {
        isUnderDate(text = text, dateDescription = dateDescription) shouldBe true
    }

    private fun assertNotUnderDate(
        text: String,
        dateDescription: String,
    ) {
        isUnderDate(text = text, dateDescription = dateDescription) shouldBe false
    }

    private fun isUnderDate(
        text: String,
        dateDescription: String,
    ): Boolean {
        val dateBounds = composeRule.onNodeWithContentDescription(dateDescription).fetchSemanticsNode().boundsInRoot
        val itemBounds = composeRule.onNodeWithText(text).fetchSemanticsNode().boundsInRoot

        return itemBounds.center.x in dateBounds.left..dateBounds.right && itemBounds.top >= dateBounds.top
    }

    private fun dday(
        title: String,
        date: LocalDate,
        label: CalendarDDayLabel,
    ): CalendarDDay = Arb.calendarDDay().next().copy(title = title, date = date, label = label)

    private fun memo(
        title: String,
        date: LocalDate,
    ): CalendarMemo = Arb.calendarMemo(dateTime = MemoDateTime.AllDay(dateRange = date..date), title = title).next()

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    private companion object {
        val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
        const val JULY_15_DESCRIPTION = "7월 15일"
        const val DDAY_TEXT_PREFIX = "🎉"
        const val EXAM_TITLE = "수능"
        const val EXAM_BASE_DAY_TEXT = "🎉 수능 D-Day"
        const val US_TITLE = "우리"
        const val US_HUNDRED_DAYS_TEXT = "🎉 우리 100일"
        const val SPRING_TITLE = "봄"
        const val BABY_TITLE = "아기"
        const val BABY_BASE_DAY_TEXT = "🎉 아기 D-Day"
        const val BABY_ONE_MONTH_TEXT = "🎉 아기 1개월"
        const val WALK_TITLE = "산책"
        const val APPOINTMENT_TITLE = "약속"
        const val CONSTITUTION_DAY_NAME = "제헌절"
        const val BIRTHDAY_NAME = "홍길동"
        const val BIRTHDAY_TEXT = "🎂 홍길동"
    }
}
