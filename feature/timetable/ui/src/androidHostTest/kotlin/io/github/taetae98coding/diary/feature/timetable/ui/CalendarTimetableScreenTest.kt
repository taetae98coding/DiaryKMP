package io.github.taetae98coding.diary.feature.timetable.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
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
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.timetable.TIMETABLE_ALL_DAY_TEST_TAG
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.core.testing.holiday.holiday
import io.github.taetae98coding.diary.core.testing.memo.calendarMemo
import io.github.taetae98coding.diary.core.testing.weather.calendarWeather
import io.github.taetae98coding.diary.domain.contact.usecase.GetCalendarContactBirthdayUseCase
import io.github.taetae98coding.diary.domain.lunar.usecase.FetchLunarUseCase
import io.github.taetae98coding.diary.feature.timetable.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.matchers.collections.shouldBeStrictlyIncreasing
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
        composeRule.onNodeWithText(VACATION_TITLE).performScrollTo().performClick()

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
        val bounds =
            composeRule
                .onNodeWithText(VACATION_TITLE)
                .performScrollTo()
                .fetchSemanticsNode()
                .boundsInRoot
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

        composeRule.onNodeWithText(CHUSEOK).performScrollTo().assertIsDisplayed()
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

        composeRule.onNodeWithText(CHUSEOK).performScrollTo().performClick()

        verify(exactly = 1) { uriHandler.openUri("https://search.naver.com/search.naver?query=%EC%B6%94%EC%84%9D") }
        composeRule.onNodeWithText("25").assertIsDisplayed()
    }

    @Test
    fun `TC-2302faa8-0e50-44f8-8bcb-d637e6159ecc 브라우저를 열지 못하면 시간표를 유지하고 검색 결과를 열지 못했다고 알린다`() {
        val errorMessage = Arb.nonBlankString().next()
        val uriHandler = mockk<UriHandler>().also { every { it.openUri(any()) } throws IllegalStateException(errorMessage) }
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 25),
            holidayListFlow = MutableStateFlow(listOf(holiday(name = CHUSEOK, isHoliday = true, dateRange = september(day = 25)..september(day = 25)))),
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CHUSEOK).performScrollTo().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("25").assertIsDisplayed()
        composeRule.onNodeWithText(CHUSEOK).assertIsDisplayed()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        composeRule.onAllNodes(isPopup()).assertCountEquals(0)
        composeRule.onNodeWithText(errorMessage, substring = true).assertDoesNotExist()
        composeRule.onNodeWithText(SEARCH_OPEN_FAILED_MESSAGE).assertIsDisplayed()
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

    @Test
    fun `TC-4b857f00-b3a8-449c-940e-3afa61e12d7a 날짜별 날씨가 해당 날짜와 함께 표시된다`() {
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.WEEK,
            date = september(day = 20),
            weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(todayWeather(), tomorrowWeather()))),
        )

        listOf(CURRENT_TEMPERATURE_TEXT to 23, MIN_MAX_TEMPERATURE_TEXT to 24).forEach { (text, day) ->
            val textX =
                composeRule
                    .onNodeWithText(text)
                    .assertIsDisplayed()
                    .fetchSemanticsNode()
                    .boundsInRoot.center.x
            val dayX =
                composeRule
                    .onNodeWithText(day.toString())
                    .fetchSemanticsNode()
                    .boundsInRoot.center.x

            abs(textX - dayX) shouldBeLessThan COLUMN_ALIGNMENT_TOLERANCE_PX
        }
        composeRule.onNodeWithContentDescription(CLEAR_SKY, useUnmergedTree = true).assertExists()
        composeRule.onNodeWithContentDescription(FEW_CLOUDS, useUnmergedTree = true).assertExists()
        composeRule.onAllNodesWithContentDescription(CLEAR_SKY, useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithContentDescription(FEW_CLOUDS, useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun `TC-b6db93e7-1cfd-4b3d-b96f-f0f073768b0b 날씨가 준비되면 별도 조작 없이 표시된다`() {
        val weatherReportFlow = MutableStateFlow(CalendarWeatherReport())
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23), weatherReportFlow = weatherReportFlow)
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertDoesNotExist()

        composeRule.runOnIdle { weatherReportFlow.value = CalendarWeatherReport(weatherList = listOf(todayWeather())) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(CLEAR_SKY, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-5936fd22-9be9-41ae-8e1d-b3b6886cadd6 다른 날짜로 이동하면 이동한 날짜의 날씨를 보여 준다`() {
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(todayWeather(), tomorrowWeather()))),
        )
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertIsDisplayed()

        swipePageLeft()

        composeRule.onNodeWithText(MIN_MAX_TEMPERATURE_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertIsNotDisplayed()
    }

    @Test
    fun `TC-1f83f6a3-a49a-41f4-957e-6a0ad7886b28 날씨를 선택하면 지역명으로 검색하고 시간표가 유지된다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(todayWeather()), locationName = SEONGNAM)),
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { uriHandler.openUri("https://search.naver.com/search.naver?query=%EC%84%B1%EB%82%A8%EC%8B%9C%20%EB%82%A0%EC%94%A8") }
        composeRule.onNodeWithText("23").assertIsDisplayed()
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-95cba6cc-aa00-4126-8a7a-a73204ddc2c1 지역명이 준비되면 조작 없이 그 지역명으로 검색한다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        val weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(todayWeather())))
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            weatherReportFlow = weatherReportFlow,
            uriHandler = uriHandler,
        )

        weatherReportFlow.value = CalendarWeatherReport(weatherList = listOf(todayWeather()), locationName = SEONGNAM)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { uriHandler.openUri("https://search.naver.com/search.naver?query=%EC%84%B1%EB%82%A8%EC%8B%9C%20%EB%82%A0%EC%94%A8") }
    }

    @Test
    fun `TC-e19d9798-5498-46f7-bcaa-46c0aafab5c8 지역명이 없으면 날씨만으로 검색한다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(todayWeather()))),
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { uriHandler.openUri("https://search.naver.com/search.naver?query=%EB%82%A0%EC%94%A8") }
    }

    @Test
    fun `TC-0dd8ce50-ed06-4131-b2bf-9695c4148b36 날씨 검색을 열지 못하면 시간표를 유지하고 알린다`() {
        val uriHandler = mockk<UriHandler>().also { every { it.openUri(any()) } throws IllegalStateException(Arb.nonBlankString().next()) }
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(todayWeather()), locationName = SEONGNAM)),
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("23").assertIsDisplayed()
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertIsDisplayed()
        composeRule.onNodeWithText(SEARCH_OPEN_FAILED_MESSAGE).assertIsDisplayed()
    }

    @Test
    fun `TC-2c2c2d8c-35b6-4d4e-88f0-421db4021fa5 날씨를 길게 누르면 그 날짜로 MemoAdd 이동을 요청한다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        val requestList = mutableListOf<MemoDateTime>()
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            navigateToMemoAdd = { requestList += it },
            weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = listOf(todayWeather()), locationName = SEONGNAM)),
            uriHandler = uriHandler,
        )
        val position =
            composeRule
                .onNodeWithText(CURRENT_TEMPERATURE_TEXT)
                .fetchSemanticsNode()
                .boundsInRoot.center

        composeRule.onRoot().performTouchInput {
            down(position)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_MARGIN_MILLIS)
            moveBy(Offset.Zero)
            up()
        }
        composeRule.waitForIdle()

        verify(exactly = 0) { uriHandler.openUri(any()) }
        requestList shouldBe listOf(MemoDateTime.AllDay(dateRange = september(day = 23)..september(day = 23)))
    }

    @Test
    fun `TC-67e702c6-49f8-44c2-8e6a-c5add506e4f0 날씨 동기화가 실패해도 메모와 공휴일을 표시한다`() {
        assertShownWithoutWeather(weatherViewModel = calendarTimetableWeatherViewModel(fetchResult = weatherFailure()))
    }

    @Test
    fun `TC-67e702c6-49f8-44c2-8e6a-c5add506e4f0 날씨 조회가 실패해도 메모와 공휴일을 표시한다`() {
        assertShownWithoutWeather(
            weatherViewModel = calendarTimetableWeatherViewModel(getCurrentCalendarWeatherUseCase = getCurrentCalendarWeatherUseCase(flow = flowOf(weatherFailure()))),
        )
    }

    private fun weatherFailure(): Result<Nothing> = Result.failure(IllegalStateException(Arb.nonBlankString().next()))

    private fun assertShownWithoutWeather(weatherViewModel: CalendarTimetableWeatherViewModel) {
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            memoList = listOf(memo(title = MEETING_TITLE, dateTime = timed(day = 23, startHour = 10, endHour = 11))),
            holidayListFlow = MutableStateFlow(listOf(holiday(name = CHUSEOK, isHoliday = true, dateRange = september(day = 23)..september(day = 23)))),
            weatherViewModel = weatherViewModel,
        )

        composeRule.onNodeWithText(MEETING_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(CHUSEOK).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(SEARCH_OPEN_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
    }

    @Test
    fun `TC-e6a3d4de-9fd0-4aab-9e4b-abda7222dd52 날짜를 옮겨도 날짜별 날씨를 다시 조회하지 않는다`() {
        val getUseCase = getCurrentCalendarWeatherUseCase(flow = flowOf(Result.success(CalendarWeatherReport(weatherList = listOf(todayWeather(), tomorrowWeather())))))
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            weatherViewModel = calendarTimetableWeatherViewModel(getCurrentCalendarWeatherUseCase = getUseCase),
        )
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertIsDisplayed()

        swipePageLeft()

        composeRule.onNodeWithText(MIN_MAX_TEMPERATURE_TEXT).assertIsDisplayed()
        verify(exactly = 1) { getUseCase(parameter = Unit) }
    }

    @Test
    fun `TC-f6cbfda7-2501-4711-8bed-10ccd513f97a 생일이 그 날짜의 종일 영역에 표시된다`() {
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.WEEK,
            date = september(day = 20),
            birthdayListFlow = MutableStateFlow(listOf(birthday(day = 24))),
        )

        val bounds =
            composeRule
                .onNodeWithText(BIRTHDAY_TEXT)
                .performScrollTo()
                .assertIsDisplayed()
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                .fetchSemanticsNode()
                .boundsInRoot
        val allDayBounds = composeRule.onNodeWithTag(TIMETABLE_ALL_DAY_TEST_TAG).fetchSemanticsNode().boundsInRoot
        val dayX =
            composeRule
                .onNodeWithText("24")
                .fetchSemanticsNode()
                .boundsInRoot.center.x

        composeRule.onNodeWithContentDescription(BIRTHDAY_CONTENT_DESCRIPTION).assertExists()
        (bounds.top >= allDayBounds.top && bounds.bottom <= allDayBounds.bottom) shouldBe true
        abs(bounds.center.x - dayX) shouldBeLessThan COLUMN_ALIGNMENT_TOLERANCE_PX
    }

    @Test
    fun `TC-4d0c816f-b319-4d75-9d10-fbb52214e832 생일이 준비되면 별도 조작 없이 표시된다`() {
        val birthdayListFlow = MutableStateFlow<List<CalendarContactBirthday>>(emptyList())
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 24), birthdayListFlow = birthdayListFlow)
        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertDoesNotExist()

        composeRule.runOnIdle { birthdayListFlow.value = listOf(birthday(day = 24)) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(BIRTHDAY_TEXT).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `TC-fd73be8f-c05b-498c-a7e9-154116cdbc9a 생일을 선택하면 ContactDetail 이동을 요청한다`() {
        val birthday = birthday(day = 24)
        val uriHandler = mockk<UriHandler>(relaxed = true)
        val contactIdList = mutableListOf<Uuid>()
        val memoAddList = mutableListOf<MemoDateTime>()
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 24),
            birthdayListFlow = MutableStateFlow(listOf(birthday)),
            navigateToContactDetail = { contactIdList += it },
            navigateToMemoAdd = { memoAddList += it },
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(BIRTHDAY_TEXT).performScrollTo().performClick()
        composeRule.waitForIdle()

        contactIdList shouldBe listOf(birthday.contactId)
        memoAddList shouldBe emptyList()
        verify(exactly = 0) { uriHandler.openUri(any()) }
    }

    @Test
    fun `TC-61c3c9ea-c99d-4122-9be9-6c88069a3b61 생일을 길게 누르면 그 날짜로 MemoAdd 이동을 요청한다`() {
        val contactIdList = mutableListOf<Uuid>()
        val memoAddList = mutableListOf<MemoDateTime>()
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 24),
            birthdayListFlow = MutableStateFlow(listOf(birthday(day = 24))),
            navigateToContactDetail = { contactIdList += it },
            navigateToMemoAdd = { memoAddList += it },
        )
        val position =
            composeRule
                .onNodeWithText(BIRTHDAY_TEXT)
                .performScrollTo()
                .fetchSemanticsNode()
                .boundsInRoot.center

        composeRule.onRoot().performTouchInput {
            down(position)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_MARGIN_MILLIS)
            moveBy(Offset.Zero)
            up()
        }
        composeRule.waitForIdle()

        contactIdList shouldBe emptyList()
        memoAddList shouldBe listOf(MemoDateTime.AllDay(dateRange = september(day = 24)..september(day = 24)))
    }

    @Test
    fun `TC-2c7d150a-f579-4c23-bb3c-4dfa4b949c07 다른 날짜로 이동하면 이동한 날짜의 생일을 보여 준다`() {
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            birthdayListFlow = MutableStateFlow(listOf(birthday(day = 23), birthday(day = 24, name = OTHER_BIRTHDAY_NAME))),
        )
        composeRule.onNodeWithText(BIRTHDAY_TEXT).performScrollTo().assertIsDisplayed()

        swipePageLeft()

        composeRule.onNodeWithText(OTHER_BIRTHDAY_TEXT).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertIsNotDisplayed()
    }

    @Test
    fun `TC-9c8e653c-ad81-480b-99a2-dac567c94496 생일 조회에 실패해도 메모와 공휴일을 표시한다`() {
        val getUseCase = mockk<GetCalendarContactBirthdayUseCase>()
        every { getUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(Arb.nonBlankString().next())))
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 24),
            memoList = listOf(memo(title = MEETING_TITLE, dateTime = timed(day = 24, startHour = 10, endHour = 11))),
            holidayListFlow = MutableStateFlow(listOf(holiday(name = CHUSEOK, isHoliday = true, dateRange = september(day = 24)..september(day = 24)))),
            birthdayViewModel =
                CalendarTimetableBirthdayViewModel(
                    fetchLunarUseCase = mockk<FetchLunarUseCase>().also { useCase -> coEvery { useCase(parameter = any()) } returns Result.success(emptyList()) },
                    getCalendarContactBirthdayUseCase = getUseCase,
                ),
        )

        composeRule.onNodeWithText(MEETING_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(CHUSEOK).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(SEARCH_OPEN_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-53841678-2f11-4327-9b1f-ee8efd57ae6e 이름이 같은 연락처의 생일은 알게 된 경로와 함께 표시된다`() {
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 24),
            birthdayListFlow = MutableStateFlow(listOf(birthday(day = 24, nameQualifier = HOW_WE_MET))),
        )

        composeRule.onNodeWithText("$BIRTHDAY_TEXT($HOW_WE_MET)").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `TC-bf087088-ec80-4bcb-8701-ac814a9dbca3 음력 자료 동기화가 한 연도 실패해도 다른 연도를 요청하고 시간표를 보여 준다`() {
        val fetchUseCase = mockk<FetchLunarUseCase>()
        coEvery { fetchUseCase(parameter = 2026) } returns Result.failure(IllegalStateException(Arb.nonBlankString().next()))
        coEvery { fetchUseCase(parameter = 2027) } returns Result.success(emptyList())
        val getUseCase = mockk<GetCalendarContactBirthdayUseCase>()
        every { getUseCase(parameter = any()) } returns flowOf(Result.success(listOf(birthday(day = 31, month = 12))))
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = LocalDate(year = 2026, month = 12, day = 31),
            memoList = listOf(memo(title = MEETING_TITLE, dateTime = timed(day = 31, month = 12, startHour = 10, endHour = 11))),
            holidayListFlow =
                MutableStateFlow(
                    listOf(holiday(name = CHUSEOK, isHoliday = true, dateRange = LocalDate(year = 2026, month = 12, day = 31)..LocalDate(year = 2026, month = 12, day = 31))),
                ),
            birthdayViewModel = CalendarTimetableBirthdayViewModel(fetchLunarUseCase = fetchUseCase, getCalendarContactBirthdayUseCase = getUseCase),
        )

        coVerify(exactly = 1) { fetchUseCase(parameter = 2027) }
        composeRule.onNodeWithText(MEETING_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(CHUSEOK).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(BIRTHDAY_TEXT).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(SEARCH_OPEN_FAILED_MESSAGE).assertDoesNotExist()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
    }

    private fun birthday(
        day: Int,
        name: String = BIRTHDAY_NAME,
        nameQualifier: String = "",
        month: Int = 9,
    ): CalendarContactBirthday = CalendarContactBirthday(contactId = Arb.kotlinUuid().next(), name = name, nameQualifier = nameQualifier, date = LocalDate(year = 2026, month = month, day = day))

    private fun todayWeather(): CalendarWeather =
        Arb
            .calendarWeather(
                date = september(day = 23),
                temperature = CalendarWeatherTemperature.Current(value = 24.3),
                descriptionList = listOf(CLEAR_SKY),
            ).next()

    private fun tomorrowWeather(): CalendarWeather =
        Arb
            .calendarWeather(
                date = september(day = 24),
                temperature = CalendarWeatherTemperature.MinMax(min = 17.8, max = 28.2),
                descriptionList = listOf(FEW_CLOUDS),
            ).next()

    private fun setTimetableScreen(
        type: CalendarTimetableNavKey.Type,
        date: LocalDate,
        memoList: List<CalendarMemo> = emptyList(),
        memoListFlow: MutableStateFlow<List<CalendarMemo>> = MutableStateFlow(memoList),
        navigateUp: () -> Unit = {},
        navigateToMemoDetail: (Uuid) -> Unit = {},
        navigateToMemoAdd: (MemoDateTime) -> Unit = {},
        navigateToContactDetail: (Uuid) -> Unit = {},
        birthdayListFlow: MutableStateFlow<List<CalendarContactBirthday>> = MutableStateFlow(emptyList()),
        birthdayViewModel: CalendarTimetableBirthdayViewModel = timetableBirthdayViewModel(birthdayListFlow = birthdayListFlow),
        holidayListFlow: MutableStateFlow<List<Holiday>> = MutableStateFlow(emptyList()),
        weatherReportFlow: MutableStateFlow<CalendarWeatherReport> = MutableStateFlow(CalendarWeatherReport()),
        weatherViewModel: CalendarTimetableWeatherViewModel = timetableWeatherViewModel(weatherReportFlow = weatherReportFlow),
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
                        navigateToContactDetail = navigateToContactDetail,
                        state = state,
                        memoViewModel = viewModel,
                        holidayViewModel = holidayViewModel(holidayListFlow = holidayListFlow),
                        weatherViewModel = weatherViewModel,
                        birthdayViewModel = birthdayViewModel,
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
private const val SEARCH_OPEN_FAILED_MESSAGE = "Couldn't open the search results."
private const val BIRTHDAY_NAME = "홍길동"
private const val BIRTHDAY_TEXT = "🎂 홍길동"
private const val BIRTHDAY_CONTENT_DESCRIPTION = "홍길동's birthday"
private const val OTHER_BIRTHDAY_NAME = "김철수"
private const val OTHER_BIRTHDAY_TEXT = "🎂 김철수"
private const val SEONGNAM = "성남시"
private const val HOW_WE_MET = "친구 소개"
private const val CLEAR_SKY = "clear sky"
private const val FEW_CLOUDS = "few clouds"
private const val CURRENT_TEMPERATURE_TEXT = "24.3°"
private const val MIN_MAX_TEMPERATURE_TEXT = "17.8°/28.2°"

private fun september(day: Int): LocalDate = LocalDate(year = 2026, month = 9, day = day)

private val WEEKDAY_TITLE_LIST = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
private const val COLUMN_ALIGNMENT_TOLERANCE_PX = 1F

private fun timed(
    day: Int,
    startHour: Int,
    endHour: Int,
    month: Int = 9,
): MemoDateTime =
    MemoDateTime.DateTime(
        start = LocalDateTime(year = 2026, month = month, day = day, hour = startHour, minute = 0),
        endInclusive = LocalDateTime(year = 2026, month = month, day = day, hour = endHour, minute = 0),
    )

private fun memo(
    title: String,
    dateTime: MemoDateTime,
): CalendarMemo = Arb.calendarMemo(dateTime = dateTime, title = title).next()

private fun holiday(
    name: String,
    isHoliday: Boolean,
    dateRange: LocalDateRange,
): Holiday = Arb.holiday().next().copy(name = name, isHoliday = isHoliday, dateRange = dateRange)
