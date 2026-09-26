package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.timetable.TIMETABLE_ALL_DAY_TEST_TAG
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.testing.memo.calendarMemo
import io.github.taetae98coding.diary.feature.calendar.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
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
class CalendarTimetableScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-001 하루 형식은 시작 날짜 하나를 표시한다`() {
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23))

        composeRule.onNodeWithText("Wed").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertIsDisplayed()
        composeRule.onNodeWithText("Tue").assertDoesNotExist()
        composeRule.onNodeWithText("24").assertDoesNotExist()
        composeRule.onNodeWithText(SEPTEMBER_2026_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-002 주간 형식은 시작 주의 일요일부터 토요일까지 표시한다`() {
        setTimetableScreen(type = CalendarTimetableNavKey.Type.WEEK, date = september(day = 20))

        for (day in 20..26) composeRule.onNodeWithText(day.toString()).assertIsDisplayed()
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { title -> composeRule.onNodeWithText(title).assertIsDisplayed() }
        composeRule.onNodeWithText("19").assertDoesNotExist()
        composeRule.onNodeWithText("27").assertDoesNotExist()
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-003 두 달에 걸친 주는 일요일이 속한 달을 안내한다`() {
        setTimetableScreen(type = CalendarTimetableNavKey.Type.WEEK, date = september(day = 27))

        composeRule.onNodeWithText(SEPTEMBER_2026_TITLE).assertIsDisplayed()
        listOf(27, 28, 29, 30, 1, 2, 3).forEach { day -> composeRule.onNodeWithText(day.toString()).assertIsDisplayed() }
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-004 다음 날로 넘기면 년도와 월 안내가 이동한 날짜 기준으로 바뀐다`() {
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 30))

        swipePageLeft()

        composeRule.onNodeWithText("Thu").assertIsDisplayed()
        composeRule.onNodeWithText("1").assertIsDisplayed()
        composeRule.onNodeWithText("October 2026").assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-008 하루 일정은 시간대 영역에, 그 밖의 메모는 종일 영역에 표시된다`() {
        val meeting = memo(title = MEETING_TITLE, dateTime = timed(day = 23, startHour = 10, endHour = 11))
        val vacation = memo(title = VACATION_TITLE, dateTime = MemoDateTime.AllDay(dateRange = september(day = 22)..september(day = 24)))
        val trip =
            memo(
                title = TRIP_TITLE,
                dateTime =
                    MemoDateTime.DateTime(
                        start = LocalDateTime(year = 2026, month = 9, day = 22, hour = 9, minute = 0),
                        endInclusive = LocalDateTime(year = 2026, month = 9, day = 23, hour = 18, minute = 0),
                    ),
            )
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            memoList = listOf(meeting, vacation, trip),
        )

        val allDayBottom =
            composeRule
                .onNodeWithTag(TIMETABLE_ALL_DAY_TEST_TAG)
                .fetchSemanticsNode()
                .boundsInRoot.bottom

        (
            composeRule
                .onNodeWithText(VACATION_TITLE)
                .fetchSemanticsNode()
                .boundsInRoot.bottom <= allDayBottom
        ) shouldBe true
        (
            composeRule
                .onNodeWithText(TRIP_TITLE)
                .fetchSemanticsNode()
                .boundsInRoot.bottom <= allDayBottom
        ) shouldBe true
        (
            composeRule
                .onNodeWithText(MEETING_TITLE)
                .fetchSemanticsNode()
                .boundsInRoot.top >= allDayBottom
        ) shouldBe true
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-010 메모를 선택하면 그 메모의 MemoDetail 화면으로 이동을 요청한다`() {
        val meeting = memo(title = MEETING_TITLE, dateTime = timed(day = 23, startHour = 10, endHour = 11))
        val vacation = memo(title = VACATION_TITLE, dateTime = MemoDateTime.AllDay(dateRange = september(day = 23)..september(day = 23)))
        val navigatedIdList = mutableListOf<Uuid>()
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            memoList = listOf(meeting, vacation),
            navigateToMemoDetail = { navigatedIdList += it },
        )

        composeRule.onNodeWithText(MEETING_TITLE).performClick()
        composeRule.onNodeWithText(VACATION_TITLE).performClick()

        navigatedIdList shouldBe listOf(meeting.id, vacation.id)
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-011 메모가 준비되면 별도 조작 없이 표시된다`() {
        val memoListFlow = MutableStateFlow(emptyList<CalendarMemo>())
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            memoListFlow = memoListFlow,
        )
        composeRule.onNodeWithText(MEETING_TITLE).assertDoesNotExist()

        memoListFlow.value = listOf(memo(title = MEETING_TITLE, dateTime = timed(day = 23, startHour = 10, endHour = 11)))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(MEETING_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-012 메모가 없어도 빈 상태 안내 없이 시간대 안내를 보여 준다`() {
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23))

        val hourLabelList = (1..11).map { hour -> "$hour AM" } + "12 PM" + (1..11).map { hour -> "$hour PM" }
        val textList =
            composeRule
                .onAllNodes(hasText("", substring = true), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .flatMap { node ->
                    node.config
                        .getOrNull(SemanticsProperties.Text)
                        .orEmpty()
                        .map { it.text }
                }

        composeRule.onNodeWithText("23").assertIsDisplayed()
        composeRule.onNodeWithText("10 AM").assertIsDisplayed()
        composeRule.onNodeWithTag(TIMETABLE_ALL_DAY_TEST_TAG).assertDoesNotExist()
        textList.toSet() shouldBe setOf(SEPTEMBER_2026_TITLE, "Wed", "23") + hourLabelList
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-015 뒤로가기를 누르면 이전 화면으로 돌아가기를 요청한다`() {
        var navigateUpCount = 0
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            navigateUp = { navigateUpCount += 1 },
        )

        composeRule.onNodeWithContentDescription("Navigate up").performClick()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-016 화면이 재생성되어도 보던 형식과 날짜를 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        var latestState: CalendarTimetableScaffoldState? = null
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            restorationTester = restorationTester,
            onState = { latestState = it },
        )
        swipePageLeft()
        composeRule.runOnIdle { latestState?.timetableState?.currentDateRange shouldBe september(day = 24)..september(day = 24) }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { latestState?.timetableState?.currentDateRange shouldBe september(day = 24)..september(day = 24) }
        composeRule.onNodeWithText("Thu").assertIsDisplayed()
        composeRule.onNodeWithText("24").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertDoesNotExist()
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-019 주간 형식에서 여러 날에 걸친 종일 메모는 어느 날짜 부분을 선택해도 같은 메모로 이동을 요청한다`() {
        val vacation = memo(title = VACATION_TITLE, dateTime = MemoDateTime.AllDay(dateRange = september(day = 21)..september(day = 24)))
        val navigatedIdList = mutableListOf<Uuid>()
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.WEEK,
            date = september(day = 20),
            memoList = listOf(vacation),
            navigateToMemoDetail = { navigatedIdList += it },
        )
        val bounds = composeRule.onNodeWithText(VACATION_TITLE).fetchSemanticsNode().boundsInRoot
        val firstDayX =
            composeRule
                .onNodeWithText("21")
                .fetchSemanticsNode()
                .boundsInRoot.center.x
        val lastDayX =
            composeRule
                .onNodeWithText("24")
                .fetchSemanticsNode()
                .boundsInRoot.center.x

        composeRule.onRoot().performTouchInput { click(Offset(x = firstDayX, y = bounds.center.y)) }
        composeRule.onRoot().performTouchInput { click(Offset(x = lastDayX, y = bounds.center.y)) }
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(vacation.id, vacation.id)
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-020 시간표에서는 태그 필터를 열 수 없다`() {
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23))

        composeRule.onAllNodesWithContentDescription(FILTER_CONTENT_DESCRIPTION, substring = true).assertCountEquals(0)
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().map { node -> node.config.getOrNull(SemanticsProperties.ContentDescription) } shouldBe
            listOf(listOf("Navigate up"))
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-021 메모의 기간이 바뀌어 자리가 달라지면 별도 조작 없이 바뀐 자리에 표시된다`() {
        val meeting = memo(title = MEETING_TITLE, dateTime = timed(day = 23, startHour = 10, endHour = 11))
        val memoListFlow = MutableStateFlow(listOf(meeting))
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            memoListFlow = memoListFlow,
        )
        composeRule.onNodeWithTag(TIMETABLE_ALL_DAY_TEST_TAG).assertDoesNotExist()

        memoListFlow.value = listOf(meeting.copy(dateTime = MemoDateTime.AllDay(dateRange = september(day = 23)..september(day = 23))))
        composeRule.waitForIdle()

        val allDayBounds = composeRule.onNodeWithTag(TIMETABLE_ALL_DAY_TEST_TAG).fetchSemanticsNode().boundsInRoot
        val meetingBounds = composeRule.onNodeWithText(MEETING_TITLE).fetchSemanticsNode().boundsInRoot

        (meetingBounds.top >= allDayBounds.top && meetingBounds.bottom <= allDayBounds.bottom) shouldBe true
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-022 다른 앱에 다녀와도 보던 형식과 날짜를 유지한다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            lifecycleOwner = lifecycleOwner,
        )
        swipePageLeft()

        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP) }
        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START) }
        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Thu").assertIsDisplayed()
        composeRule.onNodeWithText("24").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertDoesNotExist()
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-DATA-001 날짜를 옮기면 옮긴 날짜의 조회 기간으로 메모를 다시 조회한다`() {
        val viewModel = timetableViewModel(memoListFlow = MutableStateFlow(emptyList()))
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            viewModel = viewModel,
        )
        verify(exactly = 1) { viewModel.fetch(dateRange = september(day = 22)..september(day = 24)) }

        swipePageLeft()

        verify(exactly = 1) { viewModel.fetch(dateRange = september(day = 23)..september(day = 25)) }
    }

    @Test
    fun `TC-CALENDAR-TIMETABLE-FEATURE-023 시간표를 아래로 당겨도 새로고침이 시작되지 않는다`() {
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23))

        repeat(PULL_SWIPE_COUNT) { composeRule.onRoot().performTouchInput { swipeDown() } }
        composeRule.waitForIdle()

        composeRule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo), useUnmergedTree = true).assertCountEquals(0)
    }

    private fun setTimetableScreen(
        type: CalendarTimetableNavKey.Type,
        date: LocalDate,
        memoList: List<CalendarMemo> = emptyList(),
        memoListFlow: MutableStateFlow<List<CalendarMemo>> = MutableStateFlow(memoList),
        navigateUp: () -> Unit = {},
        navigateToMemoDetail: (Uuid) -> Unit = {},
        restorationTester: StateRestorationTester? = null,
        onState: (CalendarTimetableScaffoldState) -> Unit = {},
        lifecycleOwner: LifecycleOwner? = null,
        viewModel: CalendarTimetableViewModel = timetableViewModel(memoListFlow = memoListFlow),
    ) {
        val setContent: (@Composable () -> Unit) -> Unit =
            restorationTester?.let { tester -> { content -> tester.setContent(content) } } ?: composeRule::setContent

        setContent {
            val state = rememberCalendarTimetableScaffoldState(type = type, initialDate = date)
            onState(state)

            CompositionLocalProvider(LocalLifecycleOwner provides (lifecycleOwner ?: LocalLifecycleOwner.current)) {
                DiaryTheme {
                    CalendarTimetableScreen(
                        navigateUp = navigateUp,
                        navigateToMemoDetail = navigateToMemoDetail,
                        state = state,
                        viewModel = viewModel,
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun timetableViewModel(memoListFlow: MutableStateFlow<List<CalendarMemo>>): CalendarTimetableViewModel =
        mockk<CalendarTimetableViewModel>().also { viewModel ->
            every { viewModel.fetch(any()) } returns Unit
            every { viewModel.memoList } returns memoListFlow
        }

    private fun swipePageLeft() {
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun swipePageRight() {
        composeRule.onRoot().performTouchInput { swipeRight() }
        composeRule.waitForIdle()
    }
}

private const val PULL_SWIPE_COUNT = 5
private const val SEPTEMBER_2026_TITLE = "September 2026"
private const val MEETING_TITLE = "Meeting"
private const val VACATION_TITLE = "Vacation"
private const val TRIP_TITLE = "Trip"
private const val FILTER_CONTENT_DESCRIPTION = "Filter"

private fun september(day: Int): LocalDate = LocalDate(year = 2026, month = 9, day = day)

private fun timed(
    day: Int,
    startHour: Int,
    endHour: Int,
): MemoDateTime =
    MemoDateTime.DateTime(
        start = LocalDateTime(year = 2026, month = 9, day = day, hour = startHour, minute = 0),
        endInclusive = LocalDateTime(year = 2026, month = 9, day = day, hour = endHour, minute = 0),
    )

private fun memo(
    title: String,
    dateTime: MemoDateTime,
): CalendarMemo = fixtureMonkey.calendarMemo(dateTime = dateTime, title = title)
