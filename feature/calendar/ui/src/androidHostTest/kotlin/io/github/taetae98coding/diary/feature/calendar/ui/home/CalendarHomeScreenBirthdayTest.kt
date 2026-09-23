package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.domain.contact.usecase.GetCalendarContactBirthdayUseCase
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.CalendarHomeBirthdayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScreenBirthdayTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-075 연락처의 생일이 캘린더에 이름으로 표시된다`() {
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            birthdayListFlow = MutableStateFlow(listOf(birthday(name = NAME, date = july(day = 8)))),
        )

        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-076 캘린더에 보이는 주와 겹치지 않는 생일은 표시되지 않는다`() {
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            birthdayListFlow =
                MutableStateFlow(
                    listOf(birthday(name = SUMMER_NAME, date = LocalDate(year = 2026, month = Month.AUGUST, day = 20))),
                ),
        )

        composeRule.onNodeWithText(SUMMER_BIRTHDAY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-077 연락처가 저장되면 조작 없이 생일 표시가 반영된다`() {
        val birthdayListFlow = MutableStateFlow(emptyList<CalendarContactBirthday>())
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            birthdayListFlow = birthdayListFlow,
        )
        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertDoesNotExist()

        birthdayListFlow.value = listOf(birthday(name = NAME, date = july(day = 10)))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-079 다른 연도의 달로 이동해도 같은 연락처의 생일이 표시된다`() {
        val birthdayListFlow = MutableStateFlow(listOf(birthday(name = NAME, date = july(day = 8))))
        val moveTarget = mutableStateOf<YearMonth?>(null)
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            birthdayListFlow = birthdayListFlow,
            moveTarget = moveTarget,
        )
        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertIsDisplayed()

        composeRule.runOnIdle {
            birthdayListFlow.value = listOf(birthday(name = NAME, date = LocalDate(year = 2027, month = Month.JULY, day = 8)))
            moveTarget.value = JULY_2027
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-080 생일을 누르면 그 연락처의 ContactDetail 화면으로 이동한다`() {
        val birthday = birthday(name = NAME, date = july(day = 8))
        val navigatedIdList = mutableListOf<Uuid>()

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            birthdayListFlow = MutableStateFlow(listOf(birthday)),
            navigateToContactDetail = { contactId -> navigatedIdList.add(contactId) },
        )

        composeRule.onNodeWithText(BIRTHDAY_TEXT).performClick()

        navigatedIdList shouldBe listOf(birthday.contactId)
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-081 태그를 선택해도 생일 표시는 달라지지 않는다`() {
        val filterUiStateFlow = MutableStateFlow(CalendarHomeScaffoldFilterUiState())
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            birthdayListFlow = MutableStateFlow(listOf(birthday(name = NAME, date = july(day = 8)))),
            filterUiStateFlow = filterUiStateFlow,
        )
        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertIsDisplayed()

        filterUiStateFlow.value = CalendarHomeScaffoldFilterUiState(isApplied = true)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-DATA-034 생일 조회가 실패해도 생일 없이 표시하고 공휴일 표시를 막지 않는다`() {
        val getCalendarContactBirthdayUseCase = mockk<GetCalendarContactBirthdayUseCase>()
        every { getCalendarContactBirthdayUseCase(parameter = any()) } returns
            flowOf(Result.failure(IllegalStateException("birthday get failed")))
        val birthdayViewModel =
            CalendarHomeBirthdayViewModel(getCalendarContactBirthdayUseCase = getCalendarContactBirthdayUseCase)
        val holiday =
            Holiday(
                name = CONSTITUTION_DAY_NAME,
                isHoliday = true,
                dateRange = july(day = 17)..july(day = 17),
            )

        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            holidayList = listOf(holiday),
            birthdayViewModel = birthdayViewModel,
        )
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).assertIsDisplayed()
    }

    @Test
    fun `캘린더에 표시된 생일은 이름과 생일임을 함께 알리는 버튼으로 알려진다`() {
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            birthdayListFlow = MutableStateFlow(listOf(birthday(name = NAME, date = july(day = 8)))),
        )

        val buttonRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)

        composeRule.onNodeWithContentDescription(BIRTHDAY_DESCRIPTION).assert(buttonRole)
    }

    @Test
    fun `생일과 메모 제목은 같은 주에 함께 표시된다`() {
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            birthdayListFlow = MutableStateFlow(listOf(birthday(name = NAME, date = july(day = 8)))),
            memoList = listOf(memo(title = TRIP_TITLE, date = july(day = 10))),
        )

        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(TRIP_TITLE).assertIsDisplayed()
    }

    private fun setCalendarHomeScreen(
        initialYearMonth: YearMonth,
        birthdayListFlow: StateFlow<List<CalendarContactBirthday>>,
        memoList: List<CalendarMemo> = emptyList(),
        filterUiStateFlow: StateFlow<CalendarHomeScaffoldFilterUiState> = MutableStateFlow(CalendarHomeScaffoldFilterUiState()),
        navigateToContactDetail: (Uuid) -> Unit = {},
        moveTarget: MutableState<YearMonth?> = mutableStateOf(null),
    ) {
        setCalendarHomeScreen(
            initialYearMonth = initialYearMonth,
            holidayList = emptyList(),
            birthdayViewModel = birthdayViewModel(birthdayListFlow = birthdayListFlow),
            memoList = memoList,
            filterUiStateFlow = filterUiStateFlow,
            navigateToContactDetail = navigateToContactDetail,
            moveTarget = moveTarget,
        )
    }

    private fun setCalendarHomeScreen(
        initialYearMonth: YearMonth,
        holidayList: List<Holiday>,
        birthdayViewModel: CalendarHomeBirthdayViewModel,
        memoList: List<CalendarMemo> = emptyList(),
        filterUiStateFlow: StateFlow<CalendarHomeScaffoldFilterUiState> = MutableStateFlow(CalendarHomeScaffoldFilterUiState()),
        navigateToContactDetail: (Uuid) -> Unit = {},
        moveTarget: MutableState<YearMonth?> = mutableStateOf(null),
    ) {
        val memoViewModel =
            mockk<CalendarHomeMemoViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.memoList } returns MutableStateFlow(memoList)
                every { viewModel.filterUiState } returns filterUiStateFlow
            }

        composeRule.setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = initialYearMonth),
                )

            LaunchedEffect(moveTarget.value) {
                moveTarget.value?.let { yearMonth -> state.calendarState.animateScrollTo(yearMonth) }
            }

            DiaryTheme {
                CalendarHomeScreen(
                    navigateToMemoDetail = {},
                    navigateToMemoAdd = {},
                    navigateToContactDetail = navigateToContactDetail,
                    navigateToFilter = {},
                    state = state,
                    holidayViewModel = holidayViewModel(holidayListFlow = MutableStateFlow(holidayList)),
                    memoViewModel = memoViewModel,
                    birthdayViewModel = birthdayViewModel,
                    weatherViewModel = weatherViewModel(),
                    syncViewModel = syncViewModel(),
                    permissionManager = rememberPermissionManager(),
                )
            }
        }
    }

    private fun birthday(
        name: String,
        date: LocalDate,
    ): CalendarContactBirthday =
        CalendarContactBirthday(
            contactId = Uuid.random(),
            name = name,
            date = date,
        )

    private fun memo(
        title: String,
        date: LocalDate,
    ): CalendarMemo =
        CalendarMemo(
            id = Uuid.random(),
            title = title,
            color = 0xFF3A7BD5,
            dateTime = MemoDateTime.AllDay(dateRange = date..date),
        )

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    companion object {
        private const val NAME = "홍길동"
        private const val SUMMER_NAME = "여름"
        private const val BIRTHDAY_TEXT = "🎂 홍길동"
        private const val SUMMER_BIRTHDAY_TEXT = "🎂 여름"
        private const val TRIP_TITLE = "여행"
        private const val CONSTITUTION_DAY_NAME = "제헌절"
        private const val BIRTHDAY_DESCRIPTION = "홍길동's birthday"
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
        private val JULY_2027 = YearMonth(year = 2027, month = Month.JULY)
    }
}
