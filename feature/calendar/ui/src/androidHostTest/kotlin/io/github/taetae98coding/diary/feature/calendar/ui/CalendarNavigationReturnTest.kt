package io.github.taetae98coding.diary.feature.calendar.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeScaffoldFilterUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeScaffoldState
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeScreen
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture
import io.github.taetae98coding.diary.feature.calendar.ui.home.ScrollToTodayOnReselectEffect
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthdayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.holidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.rememberCalendarHomeScaffoldState
import io.github.taetae98coding.diary.feature.calendar.ui.home.syncViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.weatherViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.timetable.CalendarTimetableHolidayUiState
import io.github.taetae98coding.diary.feature.calendar.ui.timetable.CalendarTimetableHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.timetable.CalendarTimetableMemoUiState
import io.github.taetae98coding.diary.feature.calendar.ui.timetable.CalendarTimetableScreen
import io.github.taetae98coding.diary.feature.calendar.ui.timetable.CalendarTimetableViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.timetable.rememberCalendarTimetableScaffoldState
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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

// 캘린더 홈과 시간표에서 연 화면을 닫고 돌아왔을 때 보던 자리가 남는지는 전환 이력과 entry별 저장 상태가 함께 정한다.
// CalendarEntry와 같은 구성으로 캘린더 홈과 시간표를 두고, 다른 기능 모듈의 화면은 이 모듈의 의존이 아니므로 본문만 대역으로 둔다.
// 연 화면은 뒤로가기뿐 아니라 위로 가기와 삭제 완료로도 앱이 넘긴 navigateUp인 removeLastOrNull로 닫히므로 두 경로를 따로 본다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarNavigationReturnTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(CalendarHomeNavKey)

    private var calendarHomeState: CalendarHomeScaffoldState? = null

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-99e1591e-4730-461b-a862-03dab8a810a8 MemoDetail 화면에서 뒤로 가면 보던 달을 유지한다`() {
        setCalendarNavDisplay()
        moveCalendarToSeptember()

        composeRule.runOnIdle { backStack.add(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MEMO_DETAIL_CONTENT).assertIsDisplayed()
        pressBack()

        backStack.toList() shouldBe listOf(CalendarHomeNavKey)
        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(SEPTEMBER_2026)).assertIsDisplayed()
    }

    @Test
    fun `TC-7e5262b1-33cd-4ff3-b91a-8f66c55d6c9c MemoAdd 화면에서 뒤로 가면 선택 없이 보던 달을 유지한다`() {
        assertMemoAddReturnKeepsMonthWithoutSelection(returnToCalendar = ::pressBack)
    }

    @Test
    fun `TC-7e5262b1-33cd-4ff3-b91a-8f66c55d6c9c MemoAdd 화면을 위로 가기로 닫으면 선택 없이 보던 달을 유지한다`() {
        assertMemoAddReturnKeepsMonthWithoutSelection(returnToCalendar = ::closeTopScreen)
    }

    @Test
    fun `TC-0f42ef17-d670-4f8a-8a3f-d0c674d0588a ContactDetail 화면에서 뒤로 가면 보던 달을 유지한다`() {
        assertContactDetailReturnKeepsMonth(returnToCalendar = ::pressBack)
    }

    @Test
    fun `TC-0f42ef17-d670-4f8a-8a3f-d0c674d0588a ContactDetail 화면이 연락처를 삭제하고 닫히면 보던 달을 유지한다`() {
        assertContactDetailReturnKeepsMonth(returnToCalendar = ::closeTopScreen)
    }

    @Test
    fun `TC-d774bdc4-ce95-4a92-994b-b802410f0435 시간표에서 연 MemoDetail 화면에서 뒤로 가면 보던 날짜와 시간대를 유지하고 시간표를 닫으면 보던 달이 보인다`() {
        assertTimetableMemoDetailReturn(returnToTimetable = ::pressBack)
    }

    @Test
    fun `TC-d774bdc4-ce95-4a92-994b-b802410f0435 시간표에서 연 MemoDetail 화면이 메모를 삭제하고 닫히면 보던 날짜와 시간대를 유지하고 시간표를 닫으면 보던 달이 보인다`() {
        assertTimetableMemoDetailReturn(returnToTimetable = ::closeTopScreen)
    }

    @Test
    fun `TC-36703e4f-3db4-4371-bf5d-d0fa4023fe54 시간표에서 연 MemoAdd 화면에서 뒤로 가면 보던 날짜와 시간대를 유지한다`() {
        assertTimetableMemoAddReturn(returnToTimetable = ::pressBack)
    }

    @Test
    fun `TC-36703e4f-3db4-4371-bf5d-d0fa4023fe54 시간표에서 연 MemoAdd 화면을 위로 가기로 닫으면 보던 날짜와 시간대를 유지한다`() {
        assertTimetableMemoAddReturn(returnToTimetable = ::closeTopScreen)
    }

    private fun assertMemoAddReturnKeepsMonthWithoutSelection(returnToCalendar: () -> Unit) {
        setCalendarNavDisplay()
        moveCalendarToSeptember()

        longPressAndReleaseOnCalendar(visibleDayCenter(day = SELECT_DAY))
        composeRule.onNodeWithText(MEMO_ADD_CONTENT).assertIsDisplayed()
        (backStack.last() is MemoAddNavKey) shouldBe true
        returnToCalendar()

        backStack.toList() shouldBe listOf(CalendarHomeNavKey)
        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(SEPTEMBER_2026)).assertIsDisplayed()
        calendarHomeState
            .shouldNotBeNull()
            .calendarSelectState.dateRange
            .shouldBeNull()
    }

    private fun assertContactDetailReturnKeepsMonth(returnToCalendar: () -> Unit) {
        setCalendarNavDisplay()
        moveCalendarToSeptember()

        composeRule.runOnIdle { backStack.add(ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(CONTACT_DETAIL_CONTENT).assertIsDisplayed()
        returnToCalendar()

        backStack.toList() shouldBe listOf(CalendarHomeNavKey)
        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(SEPTEMBER_2026)).assertIsDisplayed()
    }

    private fun assertTimetableMemoDetailReturn(returnToTimetable: () -> Unit) {
        setCalendarNavDisplay()
        moveCalendarToSeptember()
        openTimetableOnSeptember24Evening()

        composeRule.runOnIdle { backStack.add(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MEMO_DETAIL_CONTENT).assertIsDisplayed()
        returnToTimetable()

        assertTimetableOnSeptember24Evening()
        pressBack()
        backStack.toList() shouldBe listOf(CalendarHomeNavKey)
        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(SEPTEMBER_2026)).assertIsDisplayed()
    }

    private fun assertTimetableMemoAddReturn(returnToTimetable: () -> Unit) {
        setCalendarNavDisplay()
        moveCalendarToSeptember()
        openTimetableOnSeptember24Evening()

        longPressAndReleaseOnTimetable(
            composeRule
                .onNodeWithText(SEPTEMBER_24.day.toString())
                .fetchSemanticsNode()
                .boundsInRoot.center,
        )
        composeRule.onNodeWithText(MEMO_ADD_CONTENT).assertIsDisplayed()
        (backStack.last() is MemoAddNavKey) shouldBe true
        returnToTimetable()

        assertTimetableOnSeptember24Evening()
    }

    private fun setCalendarNavDisplay() {
        val memoViewModel = calendarHomeMemoViewModel()
        val homeHolidayViewModel = holidayViewModel()
        val birthdayViewModel = birthdayViewModel()
        val weatherViewModel = weatherViewModel()
        val syncViewModel = syncViewModel()
        val timetableViewModel = calendarTimetableViewModel()
        val timetableHolidayViewModel = calendarTimetableHolidayViewModel()

        composeRule.setContent {
            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<CalendarHomeNavKey> {
                                val state = rememberCalendarHomeScaffoldState(calendarState = rememberCalendarState(initialYearMonth = AUGUST_2026))
                                calendarHomeState = state

                                ScrollToTodayOnReselectEffect(
                                    reselectEvent = emptyFlow(),
                                    state = state,
                                )
                                CalendarHomeScreen(
                                    navigateToMemoDetail = { id -> backStack.add(MemoDetailNavKey(id)) },
                                    navigateToMemoAdd = { dateRange -> backStack.add(dateRange.toMemoAddNavKey()) },
                                    navigateToContactDetail = { contactId -> backStack.add(ContactDetailNavKey(contactId)) },
                                    navigateToFilter = {},
                                    navigateToTimetable = backStack::add,
                                    state = state,
                                    permissionManager = rememberPermissionManager(),
                                    holidayViewModel = homeHolidayViewModel,
                                    memoViewModel = memoViewModel,
                                    birthdayViewModel = birthdayViewModel,
                                    weatherViewModel = weatherViewModel,
                                    syncViewModel = syncViewModel,
                                )
                            }
                            entry<CalendarTimetableNavKey> { key ->
                                CalendarTimetableScreen(
                                    navigateUp = { backStack.removeLastOrNull() },
                                    navigateToMemoDetail = { id -> backStack.add(MemoDetailNavKey(id)) },
                                    navigateToMemoAdd = { dateTime -> backStack.add(dateTime.toMemoAddNavKey()) },
                                    state = rememberCalendarTimetableScaffoldState(type = key.type, initialDate = key.date),
                                    memoViewModel = timetableViewModel,
                                    holidayViewModel = timetableHolidayViewModel,
                                )
                            }
                            entry<MemoDetailNavKey> { Text(text = MEMO_DETAIL_CONTENT) }
                            entry<MemoAddNavKey> { Text(text = MEMO_ADD_CONTENT) }
                            entry<ContactDetailNavKey> { Text(text = CONTACT_DETAIL_CONTENT) }
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun calendarHomeMemoViewModel(): CalendarHomeMemoViewModel =
        mockk<CalendarHomeMemoViewModel>().also { viewModel ->
            every { viewModel.select(any()) } returns Unit
            every { viewModel.uiState } returns MutableStateFlow(CalendarHomeMemoUiState())
            every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
        }

    private fun calendarTimetableViewModel(): CalendarTimetableViewModel =
        mockk<CalendarTimetableViewModel>().also { viewModel ->
            every { viewModel.select(any()) } returns Unit
            every { viewModel.uiState } returns MutableStateFlow(CalendarTimetableMemoUiState())
        }

    private fun calendarTimetableHolidayViewModel(): CalendarTimetableHolidayViewModel =
        mockk<CalendarTimetableHolidayViewModel>().also { viewModel ->
            every { viewModel.fetch(any()) } returns Unit
            every { viewModel.uiState } returns MutableStateFlow(CalendarTimetableHolidayUiState())
        }

    private fun LocalDateRange.toMemoAddNavKey(): MemoAddNavKey =
        MemoAddNavKey(
            initialDateRange = MemoAddNavKey.InitialDateRange(start = start, endInclusive = endInclusive),
        )

    private fun moveCalendarToSeptember() {
        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(AUGUST_2026)).assertIsDisplayed()
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(CalendarHomeTestFixture.englishTitle(SEPTEMBER_2026)).assertIsDisplayed()
    }

    // 날짜를 한 번 넘기고 시간대를 내려 둬, 처음 연 상태로 다시 만들어지면 드러나게 한다.
    private fun openTimetableOnSeptember24Evening() {
        composeRule.runOnIdle { backStack.add(CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.DAY, date = SEPTEMBER_23)) }
        composeRule.waitForIdle()
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        repeat(SCROLL_SWIPE_COUNT) { composeRule.onRoot().performTouchInput { swipeUp() } }
        composeRule.waitForIdle()
        assertTimetableOnSeptember24Evening()
    }

    private fun assertTimetableOnSeptember24Evening() {
        composeRule.onNodeWithText(SEPTEMBER_24.day.toString()).assertIsDisplayed()
        composeRule.onNodeWithText(SEPTEMBER_23.day.toString()).assertDoesNotExist()
        composeRule.onNodeWithText(LAST_HOUR_LABEL).assertIsDisplayed()
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun closeTopScreen() {
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()
    }

    // 넘기기 전후의 달이 함께 구성되어 있어도 화면 안에 보이는 날짜 칸만 고른다.
    private fun visibleDayCenter(day: Int): Offset {
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot

        return composeRule
            .onAllNodes(CalendarHomeTestFixture.dateCell(day = day))
            .fetchSemanticsNodes()
            .map { node -> node.boundsInRoot.center }
            .first { center -> rootBounds.contains(center) }
    }

    private fun longPressAndReleaseOnCalendar(position: Offset) {
        composeRule.onRoot().performTouchInput {
            down(position)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_MARGIN_MILLIS)
            moveBy(Offset.Zero)
        }
        composeRule.waitForIdle()
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitForIdle()
    }

    private fun longPressAndReleaseOnTimetable(position: Offset) {
        composeRule.onRoot().performTouchInput {
            down(position)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_MARGIN_MILLIS)
            moveBy(Offset.Zero)
            up()
        }
        composeRule.waitForIdle()
    }

    private fun MemoDateTime.toMemoAddNavKey(): MemoAddNavKey =
        when (this) {
            is MemoDateTime.AllDay ->
                MemoAddNavKey(
                    initialDateRange = MemoAddNavKey.InitialDateRange(start = dateRange.start, endInclusive = dateRange.endInclusive),
                )

            is MemoDateTime.DateTime ->
                MemoAddNavKey(
                    initialDateTimeRange = MemoAddNavKey.InitialDateTimeRange(start = start, endInclusive = endInclusive),
                )
        }

    private companion object {
        val AUGUST_2026 = YearMonth(year = 2026, month = Month.AUGUST)
        val SEPTEMBER_2026 = YearMonth(year = 2026, month = Month.SEPTEMBER)
        val SEPTEMBER_23 = LocalDate(year = 2026, month = Month.SEPTEMBER, day = 23)
        val SEPTEMBER_24 = LocalDate(year = 2026, month = Month.SEPTEMBER, day = 24)
        const val SELECT_DAY = 15
        const val SCROLL_SWIPE_COUNT = 5
        const val LAST_HOUR_LABEL = "11 PM"
        const val LONG_PRESS_MARGIN_MILLIS = 100L
        const val MEMO_DETAIL_CONTENT = "MemoDetailContent"
        const val MEMO_ADD_CONTENT = "MemoAddContent"
        const val CONTACT_DETAIL_CONTENT = "ContactDetailContent"
    }
}
