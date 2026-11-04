package io.github.taetae98coding.diary.feature.timetable.ui

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
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.timetable.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

// 시간표에서 연 화면을 닫고 돌아왔을 때 보던 날짜와 시간대가 남는지는 전환 이력과 entry별 저장 상태가 함께 정한다.
// TimetableEntry와 같은 구성으로 시간표를 두고, 다른 기능 모듈의 화면은 이 모듈의 의존이 아니므로 본문만 대역으로 둔다.
// 캘린더 홈이 시간표를 닫은 뒤 보던 달을 유지하는지는 캘린더 모듈의 CalendarNavigationReturnTest가 본다.
// 연 화면은 뒤로가기뿐 아니라 위로 가기와 삭제 완료로도 앱이 넘긴 navigateUp인 removeLastOrNull로 닫히므로 두 경로를 따로 본다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarTimetableNavigationReturnTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(CalendarHomeNavKey)

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-d774bdc4-ce95-4a92-994b-b802410f0435 시간표에서 연 MemoDetail 화면에서 뒤로 가면 보던 날짜와 시간대를 유지하고 시간표를 닫으면 캘린더 홈으로 돌아간다`() {
        assertTimetableMemoDetailReturn(returnToTimetable = ::pressBack)
    }

    @Test
    fun `TC-d774bdc4-ce95-4a92-994b-b802410f0435 메모를 삭제하고 닫히면 보던 날짜와 시간대를 유지하고 시간표를 닫으면 캘린더 홈으로 간다`() {
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

    private fun assertTimetableMemoDetailReturn(returnToTimetable: () -> Unit) {
        setTimetableNavDisplay()
        openTimetableOnSeptember24Evening()

        composeRule.runOnIdle { backStack.add(MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MEMO_DETAIL_CONTENT).assertIsDisplayed()
        returnToTimetable()

        assertTimetableOnSeptember24Evening()
        pressBack()
        backStack.toList() shouldBe listOf(CalendarHomeNavKey)
        composeRule.onNodeWithText(CALENDAR_HOME_CONTENT).assertIsDisplayed()
    }

    private fun assertTimetableMemoAddReturn(returnToTimetable: () -> Unit) {
        setTimetableNavDisplay()
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

    private fun setTimetableNavDisplay() {
        val timetableViewModel = calendarTimetableViewModel()
        val timetableHolidayViewModel = calendarTimetableHolidayViewModel()

        composeRule.setContent {
            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            entry<CalendarHomeNavKey> { Text(text = CALENDAR_HOME_CONTENT) }
                            entry<CalendarTimetableNavKey> { key ->
                                CalendarTimetableScreen(
                                    navigateUp = { backStack.removeLastOrNull() },
                                    navigateToMemoDetail = { id -> backStack.add(MemoDetailNavKey(id)) },
                                    navigateToMemoAdd = { dateTime -> backStack.add(dateTime.toMemoAddNavKey()) },
                                    navigateToContactDetail = { id -> backStack.add(ContactDetailNavKey(id)) },
                                    state = rememberCalendarTimetableScaffoldState(type = key.type, initialDate = key.date),
                                    memoViewModel = timetableViewModel,
                                    holidayViewModel = timetableHolidayViewModel,
                                    weatherViewModel = timetableWeatherViewModel(),
                                    birthdayViewModel = timetableBirthdayViewModel(),
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

    // 날짜를 한 번 넘기고 시간대를 내려 둬, 처음 연 상태로 다시 만들어지면 드러나게 한다.
    private fun openTimetableOnSeptember24Evening() {
        composeRule.onNodeWithText(CALENDAR_HOME_CONTENT).assertIsDisplayed()
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
        val SEPTEMBER_23 = LocalDate(year = 2026, month = Month.SEPTEMBER, day = 23)
        val SEPTEMBER_24 = LocalDate(year = 2026, month = Month.SEPTEMBER, day = 24)
        const val SCROLL_SWIPE_COUNT = 5
        const val LAST_HOUR_LABEL = "11 PM"
        const val LONG_PRESS_MARGIN_MILLIS = 100L
        const val CALENDAR_HOME_CONTENT = "CalendarHomeContent"
        const val MEMO_DETAIL_CONTENT = "MemoDetailContent"
        const val MEMO_ADD_CONTENT = "MemoAddContent"
        const val CONTACT_DETAIL_CONTENT = "ContactDetailContent"
    }
}
