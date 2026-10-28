package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.timetable.TIMETABLE_ALL_DAY_TEST_TAG
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.testing.memo.calendarMemo
import io.github.taetae98coding.diary.feature.calendar.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.home.mapState
import io.github.taetae98coding.diary.feature.calendar.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.matchers.collections.shouldBeStrictlyIncreasing
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs
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
    fun `TC-46a5627d-359f-4313-854b-2052367c8f39 하루 형식은 시작 날짜 하나를 표시한다`() {
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23))

        composeRule.onNodeWithText("Wed").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertIsDisplayed()
        composeRule.onNodeWithText("Tue").assertDoesNotExist()
        composeRule.onNodeWithText("24").assertDoesNotExist()
        composeRule.onNodeWithText(SEPTEMBER_2026_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-ded9b942-76a7-4b50-a149-3aa835fd9704 주간 형식은 시작 주의 일요일부터 토요일까지 표시한다`() {
        setTimetableScreen(type = CalendarTimetableNavKey.Type.WEEK, date = september(day = 20))

        for (day in 20..26) composeRule.onNodeWithText(day.toString()).assertIsDisplayed()
        WEEKDAY_TITLE_LIST.forEach { title -> composeRule.onNodeWithText(title).assertIsDisplayed() }
        assertWeekColumnOrder(dayList = (20..26).toList())
        composeRule.onNodeWithText("19").assertDoesNotExist()
        composeRule.onNodeWithText("27").assertDoesNotExist()
    }

    @Test
    fun `TC-1b31b8be-e0ee-42d0-95bb-64ffa8492261 두 달에 걸친 주는 일요일이 속한 달을 안내한다`() {
        setTimetableScreen(type = CalendarTimetableNavKey.Type.WEEK, date = september(day = 27))

        composeRule.onNodeWithText(SEPTEMBER_2026_TITLE).assertIsDisplayed()
        listOf(27, 28, 29, 30, 1, 2, 3).forEach { day -> composeRule.onNodeWithText(day.toString()).assertIsDisplayed() }
        assertWeekColumnOrder(dayList = listOf(27, 28, 29, 30, 1, 2, 3))
    }

    @Test
    fun `TC-888e8a2e-3dfb-4bb7-b24c-2e458230fcfc 다음 날로 넘기면 년도와 월 안내가 이동한 날짜 기준으로 바뀐다`() {
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 30))

        swipePageLeft()

        composeRule.onNodeWithText("Thu").assertIsDisplayed()
        composeRule.onNodeWithText("1").assertIsDisplayed()
        composeRule.onNodeWithText("October 2026").assertIsDisplayed()
    }

    @Test
    fun `TC-0fade908-39be-46d8-beda-d3efb5574f17 시각이 있는 기간의 메모는 여러 날에 걸쳐도 시간대 영역에, 종일 기간의 메모는 종일 영역에 표시된다`() {
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
                .boundsInRoot.top >= allDayBottom
        ) shouldBe true
        (
            composeRule
                .onNodeWithText(MEETING_TITLE)
                .fetchSemanticsNode()
                .boundsInRoot.top >= allDayBottom
        ) shouldBe true
    }

    @Test
    fun `TC-0e302b13-4099-4c35-8eb9-8d55a3a8bc8b 메모를 선택하면 그 메모의 MemoDetail 화면으로 이동을 요청한다`() {
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
    fun `TC-d1b86178-c85a-4c3f-ac73-776452a16211 메모가 준비되면 별도 조작 없이 표시된다`() {
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
    fun `TC-0d6e68b9-54e5-4c7b-b91f-da487984ec09 메모가 없어도 빈 상태 안내 없이 시간대 안내를 보여 준다`() {
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
        composeRule.onNodeWithText(TEN_AM_LABEL).assertIsDisplayed()
        composeRule.onNodeWithTag(TIMETABLE_ALL_DAY_TEST_TAG).assertDoesNotExist()
        textList.toSet() shouldBe setOf(SEPTEMBER_2026_TITLE, "Wed", "23") + hourLabelList
    }

    @Test
    fun `TC-a3d108ce-793d-40ec-8a34-52294a8e7a14 뒤로가기를 누르면 이전 화면으로 돌아가기를 요청한다`() {
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
    fun `TC-747c658f-80b2-49a1-8541-be4625f703f1 화면이 재생성되어도 보던 형식과 날짜를 유지한다`() {
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
    fun `TC-2bf85a4f-e7d6-4109-bd30-99363d6013c4 주간 형식에서 여러 날에 걸친 종일 메모는 어느 날짜 부분을 선택해도 같은 메모로 이동을 요청한다`() {
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
    fun `TC-fce59f00-84df-4193-84be-047a5bcd031d 주간 형식에서 여러 날에 걸친 시각 메모는 어느 날짜 부분을 선택해도 같은 메모로 이동을 요청한다`() {
        val night =
            memo(
                title = NIGHT_TITLE,
                dateTime =
                    MemoDateTime.DateTime(
                        start = LocalDateTime(year = 2026, month = 9, day = 21, hour = 22, minute = 0),
                        endInclusive = LocalDateTime(year = 2026, month = 9, day = 23, hour = 1, minute = 0),
                    ),
            )
        val navigatedIdList = mutableListOf<Uuid>()
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.WEEK,
            date = september(day = 20),
            memoList = listOf(night),
            navigateToMemoDetail = { navigatedIdList += it },
        )
        val nightNodes = composeRule.onAllNodesWithText(NIGHT_TITLE)

        nightNodes.assertCountEquals(3)
        nightNodes[0].performScrollTo().performClick()
        nightNodes[2].performScrollTo().performClick()
        composeRule.waitForIdle()

        navigatedIdList shouldBe listOf(night.id, night.id)
    }

    @Test
    fun `TC-af8ca7cb-3853-4b83-a38e-198f7f8b571f 시간표에서는 태그 필터를 열 수 없다`() {
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23))

        composeRule.onAllNodesWithContentDescription(FILTER_CONTENT_DESCRIPTION, substring = true).assertCountEquals(0)
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().map { node -> node.config.getOrNull(SemanticsProperties.ContentDescription) } shouldBe
            listOf(listOf("Navigate up"))
    }

    @Test
    fun `TC-d64011bb-0cdf-4822-be9d-794160bae919 메모의 기간이 바뀌어 자리가 달라지면 별도 조작 없이 바뀐 자리에 표시된다`() {
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
    fun `TC-9ed5b6e9-5878-4130-bc43-b14f4cd8818b 다른 앱에 다녀와도 보던 형식과 날짜를 유지한다`() {
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
    fun `TC-1fa8af84-2706-49ca-8383-77f1b734d6bc 날짜를 옮기면 옮긴 날짜의 조회 기간으로 메모를 다시 조회한다`() {
        val viewModel = timetableViewModel(memoListFlow = MutableStateFlow(emptyList()))
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            viewModel = viewModel,
        )
        verify(exactly = 1) { viewModel.select(dateRange = september(day = 22)..september(day = 24)) }

        swipePageLeft()

        verify(exactly = 1) { viewModel.select(dateRange = september(day = 23)..september(day = 25)) }
    }

    @Test
    fun `TC-eab72d0b-f584-4583-b169-338de9432b1b 공휴일 이름은 공휴일 여부와 관계없이 종일 영역에 표시된다`() {
        val chuseok = holiday(name = CHUSEOK, isHoliday = true, dateRange = september(day = 24)..september(day = 26))
        val anniversary = holiday(name = ANNIVERSARY, isHoliday = false, dateRange = september(day = 21)..september(day = 21))
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.WEEK,
            date = september(day = 20),
            holidayListFlow = MutableStateFlow(listOf(chuseok, anniversary)),
        )

        val allDayBounds = composeRule.onNodeWithTag(TIMETABLE_ALL_DAY_TEST_TAG).fetchSemanticsNode().boundsInRoot

        listOf(CHUSEOK, ANNIVERSARY).forEach { name ->
            val bounds = composeRule.onNodeWithText(name).fetchSemanticsNode().boundsInRoot

            (bounds.top >= allDayBounds.top && bounds.bottom <= allDayBounds.bottom) shouldBe true
        }
    }

    @Test
    fun `TC-e413ce3b-8a73-46ed-ad60-333797e1c268 공휴일이 준비되면 별도 조작 없이 이름이 표시된다`() {
        val holidayListFlow = MutableStateFlow<List<Holiday>>(emptyList())
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 25), holidayListFlow = holidayListFlow)
        composeRule.onNodeWithText(CHUSEOK).assertDoesNotExist()

        holidayListFlow.value = listOf(holiday(name = CHUSEOK, isHoliday = true, dateRange = september(day = 25)..september(day = 25)))
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CHUSEOK).assertIsDisplayed()
    }

    @Test
    fun `TC-63d0de0e-e3a4-4931-91b8-5b9b5e2d7808 공휴일 이름을 선택하면 그 이름의 검색 결과를 브라우저에서 연다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 25),
            holidayListFlow = MutableStateFlow(listOf(holiday(name = CHUSEOK, isHoliday = true, dateRange = september(day = 25)..september(day = 25)))),
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CHUSEOK).performClick()

        verify(exactly = 1) { uriHandler.openUri("https://search.naver.com/search.naver?query=%EC%B6%94%EC%84%9D") }
        composeRule.onNodeWithText("25").assertIsDisplayed()
    }

    @Test
    fun `TC-2302faa8-0e50-44f8-8bcb-d637e6159ecc 브라우저를 열지 못하면 시간표를 유지하고 검색 결과를 열지 못했다고 알린다`() {
        val errorMessage = fixtureMonkey.nonBlankString()
        val uriHandler = mockk<UriHandler>().also { every { it.openUri(any()) } throws IllegalStateException(errorMessage) }
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 25),
            holidayListFlow = MutableStateFlow(listOf(holiday(name = CHUSEOK, isHoliday = true, dateRange = september(day = 25)..september(day = 25)))),
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CHUSEOK).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("25").assertIsDisplayed()
        composeRule.onNodeWithText(CHUSEOK).assertIsDisplayed()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        composeRule.onAllNodes(isPopup()).assertCountEquals(0)
        composeRule.onNodeWithText(errorMessage, substring = true).assertDoesNotExist()
        composeRule.onNodeWithText("Couldn't open the search results.").assertIsDisplayed()
    }

    @Test
    fun `TC-fd9b33ec-ecd4-41ec-a73e-8dc35c017ef7 기간 선택을 완료하면 고른 기간으로 MemoAdd 화면 이동을 요청한다 - 시간대`() {
        val requestList = mutableListOf<MemoDateTime>()
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23), navigateToMemoAdd = { requestList += it })
        val tenY = hourLabelCenterY(label = TEN_AM_LABEL)
        val hourHeight = hourLabelCenterY(label = "11 AM") - tenY
        val x =
            composeRule
                .onRoot()
                .fetchSemanticsNode()
                .size.width * 3F / 4F

        composeRule.onRoot().performTouchInput {
            down(Offset(x = x, y = tenY + hourHeight / 6F))
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_MARGIN_MILLIS)
            moveTo(Offset(x = x, y = tenY + hourHeight * 7F / 6F))
            up()
        }
        composeRule.waitForIdle()

        requestList shouldBe
            listOf(
                MemoDateTime.DateTime(
                    start = LocalDateTime(year = 2026, month = 9, day = 23, hour = 10, minute = 0),
                    endInclusive = LocalDateTime(year = 2026, month = 9, day = 23, hour = 11, minute = 30),
                ),
            )
    }

    @Test
    fun `TC-fd9b33ec-ecd4-41ec-a73e-8dc35c017ef7 기간 선택을 완료하면 고른 기간으로 MemoAdd 화면 이동을 요청한다 - 날짜`() {
        val requestList = mutableListOf<MemoDateTime>()
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23), navigateToMemoAdd = { requestList += it })
        val position =
            composeRule
                .onNodeWithText("23")
                .fetchSemanticsNode()
                .boundsInRoot.center

        composeRule.onRoot().performTouchInput {
            down(position)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_MARGIN_MILLIS)
            moveBy(Offset.Zero)
            up()
        }
        composeRule.waitForIdle()

        requestList shouldBe listOf(MemoDateTime.AllDay(dateRange = september(day = 23)..september(day = 23)))
    }

    @Test
    fun `TC-68c831d5-cd30-4bc9-8790-09ce404ea2e7 시간대 선택 도중 조작이 끊기면 MemoAdd로 이동하지 않고 보던 날짜와 시간대를 유지한다`() {
        val requestList = mutableListOf<MemoDateTime>()
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23), navigateToMemoAdd = { requestList += it })
        val tenY = hourLabelCenterY(label = TEN_AM_LABEL)
        val hourHeight = hourLabelCenterY(label = "11 AM") - tenY
        val x =
            composeRule
                .onRoot()
                .fetchSemanticsNode()
                .size.width * 3F / 4F

        composeRule.onRoot().performTouchInput {
            down(Offset(x = x, y = tenY + hourHeight / 6F))
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_MARGIN_MILLIS)
            moveTo(Offset(x = x, y = tenY + hourHeight * 7F / 6F))
            cancel()
        }
        composeRule.waitForIdle()

        requestList shouldBe emptyList()
        composeRule.onNodeWithText("23").assertExists()
        hourLabelCenterY(label = TEN_AM_LABEL) shouldBe tenY
    }

    @Test
    fun `TC-afe6d0e6-7033-4671-b16b-4c119120ccfa 시간표를 아래로 당겨도 새로고침이 시작되지 않는다`() {
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
        navigateToMemoAdd: (MemoDateTime) -> Unit = {},
        holidayListFlow: MutableStateFlow<List<Holiday>> = MutableStateFlow(emptyList()),
        uriHandler: UriHandler? = null,
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

            CompositionLocalProvider(
                LocalLifecycleOwner provides (lifecycleOwner ?: LocalLifecycleOwner.current),
                LocalUriHandler provides (uriHandler ?: LocalUriHandler.current),
            ) {
                DiaryTheme {
                    CalendarTimetableScreen(
                        navigateUp = navigateUp,
                        navigateToMemoDetail = navigateToMemoDetail,
                        navigateToMemoAdd = navigateToMemoAdd,
                        state = state,
                        memoViewModel = viewModel,
                        holidayViewModel = holidayViewModel(holidayListFlow = holidayListFlow),
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun timetableViewModel(memoListFlow: MutableStateFlow<List<CalendarMemo>>): CalendarTimetableViewModel =
        mockk<CalendarTimetableViewModel>().also { viewModel ->
            every { viewModel.select(any()) } returns Unit
            every { viewModel.uiState } returns memoListFlow.mapState { memoList -> CalendarTimetableMemoUiState(memoList = memoList) }
        }

    private fun holidayViewModel(holidayListFlow: MutableStateFlow<List<Holiday>>): CalendarTimetableHolidayViewModel =
        mockk<CalendarTimetableHolidayViewModel>().also { viewModel ->
            every { viewModel.fetch(any()) } returns Unit
            every { viewModel.uiState } returns holidayListFlow.mapState { holidayList -> CalendarTimetableHolidayUiState(holidayList = holidayList) }
        }

    private fun assertWeekColumnOrder(dayList: List<Int>) {
        val weekdayCenterList =
            WEEKDAY_TITLE_LIST.map { title ->
                composeRule
                    .onNodeWithText(title)
                    .fetchSemanticsNode()
                    .boundsInRoot.center
            }
        val dayCenterList =
            dayList.map { day ->
                composeRule
                    .onNodeWithText(day.toString())
                    .fetchSemanticsNode()
                    .boundsInRoot.center
            }

        weekdayCenterList.map { center -> center.x }.shouldBeStrictlyIncreasing()
        weekdayCenterList.zip(dayCenterList).forEach { (weekdayCenter, dayCenter) ->
            abs(weekdayCenter.x - dayCenter.x) shouldBeLessThan COLUMN_ALIGNMENT_TOLERANCE_PX
        }
    }

    private fun swipePageLeft() {
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun hourLabelCenterY(label: String): Float =
        composeRule
            .onNodeWithText(label)
            .fetchSemanticsNode()
            .boundsInRoot.center.y

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
private const val NIGHT_TITLE = "Night"
private const val FILTER_CONTENT_DESCRIPTION = "Filter"
private const val CHUSEOK = "추석"
private const val ANNIVERSARY = "기념일"
private const val LONG_PRESS_MARGIN_MILLIS = 100L
private const val TEN_AM_LABEL = "10 AM"

private fun september(day: Int): LocalDate = LocalDate(year = 2026, month = 9, day = day)

private val WEEKDAY_TITLE_LIST = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
private const val COLUMN_ALIGNMENT_TOLERANCE_PX = 1F

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

private fun holiday(
    name: String,
    isHoliday: Boolean,
    dateRange: LocalDateRange,
): Holiday = fixtureMonkey.giveMeOne<Holiday>().copy(name = name, isHoliday = isHoliday, dateRange = dateRange)
