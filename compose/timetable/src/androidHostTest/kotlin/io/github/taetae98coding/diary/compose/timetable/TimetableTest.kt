package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.text.TextLayoutResult
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.collections.shouldBeStrictlyIncreasing
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TimetableTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var onPrimary = Color.Unspecified
    private var sundayAndHolidayColor = Color.Unspecified

    @Test
    fun `TC-e08e0804-45e1-4f11-b1cf-6d2977588800 하루 형식은 지정한 날짜 하나를 요일과 일로 표시한다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 23))

        composeRule.onNodeWithText("Wed").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertIsDisplayed()
        composeRule.onNodeWithText("Tue").assertDoesNotExist()
        composeRule.onNodeWithText("24").assertDoesNotExist()
    }

    @Test
    fun `TC-43786974-590d-4977-811f-b0342b352001 주간 형식은 지정한 날짜가 속한 주의 일요일부터 토요일까지 표시한다`() {
        setTimetable(type = TimetableType.WEEK, date = LocalDate(year = 2026, month = 10, day = 1))

        listOf(27, 28, 29, 30, 1, 2, 3).forEach { day -> composeRule.onNodeWithText(day.toString()).assertIsDisplayed() }
        WEEKDAY_TITLE_LIST.forEach { title -> composeRule.onNodeWithText(title).assertIsDisplayed() }
        assertWeekColumnOrder(dayList = listOf(27, 28, 29, 30, 1, 2, 3))
        composeRule.onNodeWithText("26").assertDoesNotExist()
    }

    @Test
    fun `TC-b1fca7ba-7181-49e7-9a9f-1c6e02139298 넘긴 방향에 따라 하루나 한 주씩 이동한다 - 하루 다음`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 30))

        swipePageLeft()

        composeRule.onNodeWithText("Thu").assertIsDisplayed()
        composeRule.onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun `TC-b1fca7ba-7181-49e7-9a9f-1c6e02139298 넘긴 방향에 따라 하루나 한 주씩 이동한다 - 하루 이전`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 23))

        swipePageRight()

        composeRule.onNodeWithText("Tue").assertIsDisplayed()
        composeRule.onNodeWithText("22").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertDoesNotExist()
    }

    @Test
    fun `TC-b1fca7ba-7181-49e7-9a9f-1c6e02139298 넘긴 방향에 따라 하루나 한 주씩 이동한다 - 주간 다음`() {
        setTimetable(type = TimetableType.WEEK, date = september(day = 20))

        swipePageLeft()

        listOf(27, 28, 29, 30, 1, 2, 3).forEach { day -> composeRule.onNodeWithText(day.toString()).assertIsDisplayed() }
        composeRule.onNodeWithText("26").assertDoesNotExist()
    }

    @Test
    fun `TC-b1fca7ba-7181-49e7-9a9f-1c6e02139298 넘긴 방향에 따라 하루나 한 주씩 이동한다 - 주간 이전`() {
        setTimetable(type = TimetableType.WEEK, date = september(day = 20))

        swipePageRight()

        for (day in 13..19) composeRule.onNodeWithText(day.toString()).assertIsDisplayed()
        composeRule.onNodeWithText("20").assertDoesNotExist()
    }

    @Test
    fun `TC-f2db1905-c0d5-4f73-a2c1-fcb5d8645d0d 이동 범위의 처음에서 이전으로 넘어가지 않는다 - 하루`() {
        setTimetable(type = TimetableType.DAY, date = LocalDate(year = 0, month = 12, day = 31))

        swipePageRight()

        composeRule.onNodeWithText("Sun").assertIsDisplayed()
        composeRule.onNodeWithText("31").assertIsDisplayed()
        composeRule.onNodeWithText("30").assertDoesNotExist()
    }

    @Test
    fun `TC-f2db1905-c0d5-4f73-a2c1-fcb5d8645d0d 이동 범위의 처음에서 이전으로 넘어가지 않는다 - 주간`() {
        setTimetable(type = TimetableType.WEEK, date = LocalDate(year = 1, month = 1, day = 1))

        swipePageRight()

        composeRule.onNodeWithText("31").assertIsDisplayed()
        for (day in 1..6) composeRule.onNodeWithText(day.toString()).assertIsDisplayed()
        composeRule.onNodeWithText("24").assertDoesNotExist()
    }

    @Test
    fun `TC-73e30ad8-77ec-417b-8178-61cd160d9dc1 배치하는 화면이 이동을 요청하면 이동한 날짜가 표시된다`() {
        val state = TimetableState(type = TimetableType.DAY, initialDate = september(day = 23))
        lateinit var scope: CoroutineScope
        setTimetable(state = state, onScope = { scope = it })

        composeRule.runOnIdle { scope.launch { state.animateScrollToNext() } }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("24").assertIsDisplayed()
        state.currentDateRange shouldBe september(day = 24)..september(day = 24)

        composeRule.runOnIdle { scope.launch { state.animateScrollToPrevious() } }
        composeRule.waitForIdle()
        composeRule.runOnIdle { scope.launch { state.animateScrollToPrevious() } }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("22").assertIsDisplayed()
        state.currentDateRange shouldBe september(day = 22)..september(day = 22)
    }

    @Test
    fun `TC-05c37ae9-dae8-42aa-b8d7-5ebf5782be25 이동 범위의 처음에서 이전 이동을 요청하면 표시 날짜가 유지된다`() {
        val firstDate = LocalDate(year = 0, month = 12, day = 31)
        val state = TimetableState(type = TimetableType.DAY, initialDate = firstDate)
        lateinit var scope: CoroutineScope
        setTimetable(state = state, onScope = { scope = it })

        composeRule.runOnIdle { scope.launch { state.animateScrollToPrevious() } }
        composeRule.waitForIdle()

        state.currentDateRange shouldBe firstDate..firstDate
        composeRule.onNodeWithText("31").assertIsDisplayed()
    }

    @Test
    fun `TC-3615b3eb-deb8-477c-9994-a9cd2509e779 현재 시각의 날짜가 표시되면 현재 시각 위치를 표시한다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 23), now = TEN_O_CLOCK_ON_23)

        composeRule.onNodeWithTag(TIMETABLE_NOW_INDICATOR_TEST_TAG, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-1d4ea0a9-c1af-432a-98ad-fe8cc4bc6624 현재 시각의 날짜가 표시되지 않으면 현재 시각 위치를 표시하지 않는다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 24), now = TEN_O_CLOCK_ON_23)

        composeRule.onNodeWithTag(TIMETABLE_NOW_INDICATOR_TEST_TAG, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-1d4ea0a9-c1af-432a-98ad-fe8cc4bc6624 현재 시각을 지정하지 않으면 현재 시각 위치를 표시하지 않는다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 24), now = null)

        composeRule.onNodeWithTag(TIMETABLE_NOW_INDICATOR_TEST_TAG, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-ac825704-89df-4c43-ae12-5338c92b37a9 시간대 안내, 시각 아이템과 현재 시각 표시가 같은 높이의 시간대에 놓인다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 23), now = TEN_O_CLOCK_ON_23) { meetingItem() }
        val initialTenHourY = hourLineY(label = TEN_AM_LABEL)
        assertMeetingAndNowPlacedOnTenHour()

        composeRule.onRoot().performTouchInput { swipeUp() }
        composeRule.waitForIdle()

        hourLineY(label = TEN_AM_LABEL) shouldNotBe initialTenHourY
        assertMeetingAndNowPlacedOnTenHour()
    }

    @Test
    fun `TC-06d8f06c-9ccd-4403-9dfd-8e6c6e35bc2f 지정한 현재 시각이 바뀌면 현재 시각 위치가 따라 움직인다`() {
        var now by mutableStateOf(TEN_O_CLOCK_ON_23)
        composeRule.setContent {
            DiaryTheme {
                Timetable(
                    state = TimetableState(type = TimetableType.DAY, initialDate = september(day = 23)),
                    nowProvider = { now },
                ) {}
            }
        }
        composeRule.waitForIdle()
        abs(minuteFrom(hourLabel = TEN_AM_LABEL, y = nowIndicatorCenterY())) shouldBeLessThan PLACEMENT_TOLERANCE_MINUTES

        composeRule.runOnIdle { now = TWO_O_CLOCK_PM_ON_23 }
        composeRule.waitForIdle()

        abs(minuteFrom(hourLabel = TWO_PM_LABEL, y = nowIndicatorCenterY())) shouldBeLessThan PLACEMENT_TOLERANCE_MINUTES
    }

    @Test
    fun `TC-225e47cd-831f-4aed-b084-d9cccb15feb8 시각 아이템은 시간대 영역에, 종일 아이템은 종일 영역에 표시된다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 23)) {
            meetingItem()
            allDayItem(dateRange = september(day = 22)..september(day = 24), key = VACATION) { Text(text = VACATION) }
        }

        val allDayBounds = composeRule.onNodeWithTag(TIMETABLE_ALL_DAY_TEST_TAG).fetchSemanticsNode().boundsInRoot
        val vacationBounds = composeRule.onNodeWithText(VACATION).fetchSemanticsNode().boundsInRoot
        val meetingBounds = composeRule.onNodeWithText(MEETING).fetchSemanticsNode().boundsInRoot

        (vacationBounds.top >= allDayBounds.top && vacationBounds.bottom <= allDayBounds.bottom) shouldBe true
        (meetingBounds.top >= allDayBounds.bottom) shouldBe true
    }

    @Test
    fun `TC-2167dec8-4497-4bb1-805e-991813f66ea7 표시 날짜와 겹치는 종일 아이템이 없으면 종일 영역을 두지 않는다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 23)) {
            meetingItem()
            allDayItem(dateRange = september(day = 1)..september(day = 1), key = PAST) { Text(text = PAST) }
        }

        composeRule.onNodeWithText(MEETING).assertIsDisplayed()
        composeRule.onNodeWithText(PAST).assertDoesNotExist()
        composeRule.onNodeWithTag(TIMETABLE_ALL_DAY_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-b17c6c70-edc9-499a-911f-78edebac4c84 아이템 지정이 바뀌면 별도 조작 없이 표시가 갱신된다`() {
        var hasMeeting by mutableStateOf(false)
        setTimetable(type = TimetableType.DAY, date = september(day = 23)) {
            if (hasMeeting) meetingItem()
        }
        composeRule.onNodeWithText(MEETING).assertDoesNotExist()

        composeRule.runOnIdle { hasMeeting = true }

        composeRule.onNodeWithText(MEETING).assertIsDisplayed()
    }

    @Test
    fun `TC-6c394a1f-d977-4e52-acdb-acdc324d99ac 아이템이 없으면 빈 상태 안내 없이 시간대 안내만 보여 준다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 23))

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

        textList.toSet() shouldBe setOf("Wed", "23") + hourLabelList
    }

    @Test
    fun `TC-0d163ec3-bdce-4005-9445-63a33373b8c8 여러 날에 걸친 시각 아이템은 날짜마다 시간대 영역에 표시되고 어느 날짜 부분을 눌러도 같은 아이템이 선택된다`() {
        var clickCount = 0
        setTimetable(type = TimetableType.WEEK, date = september(day = 20)) {
            timeItem(
                start = LocalDateTime(date = september(day = 21), time = LocalTime(hour = 22, minute = 0)),
                endInclusive = LocalDateTime(date = september(day = 23), time = LocalTime(hour = 1, minute = 0)),
                key = NIGHT,
            ) {
                Text(text = NIGHT, modifier = Modifier.fillMaxSize().clickable { clickCount += 1 })
            }
        }
        val dayXList =
            listOf("21", "22", "23").map { day ->
                composeRule
                    .onNodeWithText(day)
                    .fetchSemanticsNode()
                    .boundsInRoot.center.x
            }
        val nightNodes = composeRule.onAllNodesWithText(NIGHT)

        composeRule.onNodeWithTag(TIMETABLE_ALL_DAY_TEST_TAG).assertDoesNotExist()
        nightNodes.assertCountEquals(3)
        List(3) { index ->
            val x =
                nightNodes[index]
                    .performScrollTo()
                    .fetchSemanticsNode()
                    .boundsInRoot.center.x
            dayXList.indices.minBy { dayIndex -> abs(dayXList[dayIndex] - x) }
        } shouldBe listOf(0, 1, 2)
        nightNodes[0].performScrollTo().performClick()
        nightNodes[2].performScrollTo().performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 2
    }

    @Test
    fun `TC-28bab72e-9732-4b10-9962-e6dd28ef5ac2 여러 날에 걸친 종일 아이템은 어느 날짜 부분을 눌러도 같은 아이템이 선택된다`() {
        var clickCount = 0
        setTimetable(type = TimetableType.WEEK, date = september(day = 20)) {
            allDayItem(dateRange = september(day = 21)..september(day = 24), key = VACATION) {
                Text(text = VACATION, modifier = Modifier.clickable { clickCount += 1 })
            }
        }
        val bounds = composeRule.onNodeWithText(VACATION).fetchSemanticsNode().boundsInRoot
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

        clickCount shouldBe 2
    }

    @Test
    fun `TC-ba8c8c7d-4066-4c8e-ae0c-f4fe2065c346 화면이 재생성되어도 보던 형식과 날짜를 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                Timetable(state = rememberTimetableState(type = TimetableType.DAY, initialDate = september(day = 23))) {}
            }
        }
        swipePageLeft()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Thu").assertIsDisplayed()
        composeRule.onNodeWithText("24").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertDoesNotExist()
    }

    @Test
    fun `TC-ffd99729-644c-4a05-9ce7-d328c2aa52a3 화면이 재생성되어도 보던 시간대를 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                Timetable(state = rememberTimetableState(type = TimetableType.DAY, initialDate = september(day = 23))) {}
            }
        }
        composeRule.onNodeWithText(LAST_HOUR_LABEL).assertIsNotDisplayed()
        repeat(SCROLL_SWIPE_COUNT) { composeRule.onRoot().performTouchInput { swipeUp() } }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(LAST_HOUR_LABEL).assertIsDisplayed()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(LAST_HOUR_LABEL).assertIsDisplayed()
    }

    @Test
    fun `TC-aa805b54-9195-4caf-9c67-e00bb487c927 처음 표시되면 오전 8시부터 보인다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 23))

        composeRule.onNodeWithText(INITIAL_HOUR_LABEL).assertIsDisplayed()
        composeRule.onNodeWithText(BEFORE_INITIAL_HOUR_LABEL).assertIsNotDisplayed()
    }

    @Test
    fun `TC-7782d5cb-4f87-4c66-93e4-a213c9abd888 다른 날짜로 넘겨도 보던 시간대를 이어서 본다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 23))
        composeRule.onNodeWithText(LAST_HOUR_LABEL).assertIsNotDisplayed()
        repeat(SCROLL_SWIPE_COUNT) { composeRule.onRoot().performTouchInput { swipeUp() } }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(LAST_HOUR_LABEL).assertIsDisplayed()

        swipePageLeft()

        composeRule.onNodeWithText("24").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertDoesNotExist()
        composeRule.onNodeWithText(LAST_HOUR_LABEL).assertIsDisplayed()
    }

    // Timetable은 보던 형식과 날짜, 시간대를 저장 상태로만 들고 있으므로, 시스템이 앱을 정리했다가 다시 만드는 경우도 저장 상태의 복원으로 재현한다.
    @Test
    fun `TC-f1524a94-cf84-4890-9ba1-7fb99c0ec513 시스템이 앱을 정리했다가 다시 만들어도 보던 형식과 날짜, 시간대를 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                Timetable(state = rememberTimetableState(type = TimetableType.DAY, initialDate = september(day = 23))) {}
            }
        }
        swipePageLeft()
        repeat(SCROLL_SWIPE_COUNT) { composeRule.onRoot().performTouchInput { swipeUp() } }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(LAST_HOUR_LABEL).assertIsDisplayed()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Thu").assertIsDisplayed()
        composeRule.onNodeWithText("24").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertDoesNotExist()
        composeRule.onNodeWithText(LAST_HOUR_LABEL).assertIsDisplayed()
    }

    @Test
    fun `TC-0df196ee-33f7-4cfc-8f30-a674a99d1512 현재 시각이 속한 날짜를 주요 날짜로 강조한다`() {
        var now by mutableStateOf<LocalDateTime?>(TEN_O_CLOCK_ON_23)
        setColoredTimetable(nowProvider = { now })

        WEEK_DAY_LIST.forEach { day -> coloredTextCount(text = day.toString(), color = onPrimary) shouldBe if (day == 23) 1 else 0 }

        composeRule.runOnIdle { now = null }
        composeRule.waitForIdle()

        WEEK_DAY_LIST.forEach { day -> coloredTextCount(text = day.toString(), color = onPrimary) shouldBe 0 }
    }

    @Test
    fun `TC-74b4b09b-dd03-4651-acd5-a13ab7074f1c 지정한 공휴일 날짜를 일요일과 같은 방식으로 강조한다`() {
        var holidayList by mutableStateOf(listOf(september(day = 22)..september(day = 22)))
        var now by mutableStateOf<LocalDateTime?>(null)
        setColoredTimetable(nowProvider = { now }, holidayProvider = { holidayList })

        coloredTextCount(text = "22", color = sundayAndHolidayColor) shouldBe 1
        coloredTextCount(text = TUESDAY_TITLE, color = sundayAndHolidayColor) shouldBe 1
        coloredTextCount(text = "20", color = sundayAndHolidayColor) shouldBe 1
        coloredTextCount(text = "24", color = sundayAndHolidayColor) shouldBe 0

        composeRule.runOnIdle { holidayList = listOf(september(day = 24)..september(day = 24)) }
        composeRule.waitForIdle()

        coloredTextCount(text = "22", color = sundayAndHolidayColor) shouldBe 0
        coloredTextCount(text = TUESDAY_TITLE, color = sundayAndHolidayColor) shouldBe 0
        coloredTextCount(text = "24", color = sundayAndHolidayColor) shouldBe 1
        coloredTextCount(text = THURSDAY_TITLE, color = sundayAndHolidayColor) shouldBe 1

        composeRule.runOnIdle {
            holidayList = listOf(september(day = 23)..september(day = 23))
            now = TEN_O_CLOCK_ON_23
        }
        composeRule.waitForIdle()

        coloredTextCount(text = "23", color = onPrimary) shouldBe 1
        coloredTextCount(text = "23", color = sundayAndHolidayColor) shouldBe 0
    }

    private fun setTimetable(
        type: TimetableType = TimetableType.DAY,
        date: LocalDate = september(day = 23),
        now: LocalDateTime? = null,
        state: TimetableState = TimetableState(type = type, initialDate = date),
        onScope: (CoroutineScope) -> Unit = {},
        content: TimetableScope.() -> Unit = {},
    ) {
        composeRule.setContent {
            onScope(rememberCoroutineScope())
            TimetableContent(state = state, now = now, content = content)
        }
        composeRule.waitForIdle()
    }

    private fun setColoredTimetable(
        nowProvider: () -> LocalDateTime?,
        holidayProvider: () -> List<LocalDateRange> = { emptyList() },
    ) {
        val state = TimetableState(type = TimetableType.WEEK, initialDate = september(day = 20))
        composeRule.setContent {
            DiaryTheme {
                onPrimary = DiaryTheme.colorScheme.onPrimary
                sundayAndHolidayColor = CalendarDefaults.colors().sundayAndHolidayColor

                Timetable(
                    state = state,
                    nowProvider = nowProvider,
                    holidayProvider = holidayProvider,
                ) {}
            }
        }
        composeRule.waitForIdle()
    }

    private fun coloredTextCount(
        text: String,
        color: Color,
    ): Int {
        val nodes = composeRule.onAllNodesWithText(text, useUnmergedTree = true)

        return List(nodes.fetchSemanticsNodes().size) { index -> nodes[index].textColor() }
            .count { textColor -> textColor == color }
    }

    private fun SemanticsNodeInteraction.textColor(): Color {
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
            action(textLayoutResults)
        }

        return textLayoutResults
            .single()
            .layoutInput.style.color
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

    // 시간대 안내는 가리키는 시각의 선 위에 가운데를 맞춰 놓이므로, 안내의 가운데 높이를 그 시각의 높이로 삼는다.
    private fun hourLineY(label: String): Float {
        val bounds = composeRule.onNodeWithText(label).getUnclippedBoundsInRoot()

        return (bounds.top.value + bounds.bottom.value) / 2
    }

    private fun nowIndicatorCenterY(): Float {
        val bounds = composeRule.onNodeWithTag(TIMETABLE_NOW_INDICATOR_TEST_TAG, useUnmergedTree = true).getUnclippedBoundsInRoot()

        return (bounds.top.value + bounds.bottom.value) / 2
    }

    // 화면의 높이를 이웃한 두 시간대 안내 사이의 간격으로 나누어, 기준 시각에서 몇 분 떨어진 시각인지로 환산한다.
    private fun minuteFrom(
        hourLabel: String,
        y: Float,
    ): Float {
        val hourY = hourLineY(label = hourLabel)
        val hourHeight = hourLineY(label = ELEVEN_AM_LABEL) - hourLineY(label = TEN_AM_LABEL)

        return (y - hourY) / hourHeight * MINUTES_PER_HOUR
    }

    private fun assertMeetingAndNowPlacedOnTenHour() {
        val meetingBounds = composeRule.onNodeWithText(MEETING).getUnclippedBoundsInRoot()

        abs(minuteFrom(hourLabel = TEN_AM_LABEL, y = meetingBounds.top.value)) shouldBeLessThan PLACEMENT_TOLERANCE_MINUTES
        abs(minuteFrom(hourLabel = ELEVEN_AM_LABEL, y = meetingBounds.bottom.value)) shouldBeLessThan PLACEMENT_TOLERANCE_MINUTES
        abs(minuteFrom(hourLabel = TEN_AM_LABEL, y = nowIndicatorCenterY())) shouldBeLessThan PLACEMENT_TOLERANCE_MINUTES
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

@Composable
private fun TimetableContent(
    state: TimetableState,
    now: LocalDateTime?,
    content: TimetableScope.() -> Unit,
) {
    DiaryTheme {
        Timetable(
            state = state,
            nowProvider = { now },
            content = content,
        )
    }
}

private fun TimetableScope.meetingItem() {
    timeItem(
        start = LocalDateTime(date = september(day = 23), time = LocalTime(hour = 10, minute = 0)),
        endInclusive = LocalDateTime(date = september(day = 23), time = LocalTime(hour = 11, minute = 0)),
        key = MEETING,
    ) {
        Text(text = MEETING)
    }
}

private fun september(day: Int): LocalDate = LocalDate(year = 2026, month = 9, day = day)

private val WEEKDAY_TITLE_LIST = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
private val WEEK_DAY_LIST = 20..26
private const val TUESDAY_TITLE = "Tue"
private const val THURSDAY_TITLE = "Thu"
private const val COLUMN_ALIGNMENT_TOLERANCE_PX = 1F

private const val MEETING = "Meeting"
private const val NIGHT = "Night"
private const val VACATION = "Vacation"
private const val PAST = "Past"
private const val LAST_HOUR_LABEL = "11 PM"
private const val INITIAL_HOUR_LABEL = "8 AM"
private const val BEFORE_INITIAL_HOUR_LABEL = "7 AM"
private const val SCROLL_SWIPE_COUNT = 5
private val TEN_O_CLOCK_ON_23 = LocalDateTime(year = 2026, month = 9, day = 23, hour = 10, minute = 0)
private val TWO_O_CLOCK_PM_ON_23 = LocalDateTime(year = 2026, month = 9, day = 23, hour = 14, minute = 0)
private const val TEN_AM_LABEL = "10 AM"
private const val ELEVEN_AM_LABEL = "11 AM"
private const val TWO_PM_LABEL = "2 PM"

// 아이템은 시간대 선에서 조금 떨어져 놓이고 좌표는 화소 단위로 반올림되므로, 그만큼을 분으로 환산한 오차를 허용한다.
private const val PLACEMENT_TOLERANCE_MINUTES = 3F
