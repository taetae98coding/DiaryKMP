package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TimetableTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TIMETABLE-FEATURE-001 하루 형식은 지정한 날짜 하나를 요일과 일로 표시한다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 23))

        composeRule.onNodeWithText("Wed").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertIsDisplayed()
        composeRule.onNodeWithText("Tue").assertDoesNotExist()
        composeRule.onNodeWithText("24").assertDoesNotExist()
    }

    @Test
    fun `TC-TIMETABLE-FEATURE-002 주간 형식은 지정한 날짜가 속한 주의 일요일부터 토요일까지 표시한다`() {
        setTimetable(type = TimetableType.WEEK, date = LocalDate(year = 2026, month = 10, day = 1))

        listOf(27, 28, 29, 30, 1, 2, 3).forEach { day -> composeRule.onNodeWithText(day.toString()).assertIsDisplayed() }
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { title -> composeRule.onNodeWithText(title).assertIsDisplayed() }
        composeRule.onNodeWithText("26").assertDoesNotExist()
    }

    @Test
    fun `TC-TIMETABLE-FEATURE-003 넘긴 방향에 따라 하루나 한 주씩 이동한다 - 하루 다음`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 30))

        swipePageLeft()

        composeRule.onNodeWithText("Thu").assertIsDisplayed()
        composeRule.onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun `TC-TIMETABLE-FEATURE-003 넘긴 방향에 따라 하루나 한 주씩 이동한다 - 하루 이전`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 23))

        swipePageRight()

        composeRule.onNodeWithText("Tue").assertIsDisplayed()
        composeRule.onNodeWithText("22").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertDoesNotExist()
    }

    @Test
    fun `TC-TIMETABLE-FEATURE-003 넘긴 방향에 따라 하루나 한 주씩 이동한다 - 주간 다음`() {
        setTimetable(type = TimetableType.WEEK, date = september(day = 20))

        swipePageLeft()

        listOf(27, 28, 29, 30, 1, 2, 3).forEach { day -> composeRule.onNodeWithText(day.toString()).assertIsDisplayed() }
        composeRule.onNodeWithText("26").assertDoesNotExist()
    }

    @Test
    fun `TC-TIMETABLE-FEATURE-003 넘긴 방향에 따라 하루나 한 주씩 이동한다 - 주간 이전`() {
        setTimetable(type = TimetableType.WEEK, date = september(day = 20))

        swipePageRight()

        for (day in 13..19) composeRule.onNodeWithText(day.toString()).assertIsDisplayed()
        composeRule.onNodeWithText("20").assertDoesNotExist()
    }

    @Test
    fun `TC-TIMETABLE-FEATURE-004 이동 범위의 처음에서 이전으로 넘어가지 않는다 - 하루`() {
        setTimetable(type = TimetableType.DAY, date = LocalDate(year = 0, month = 12, day = 31))

        swipePageRight()

        composeRule.onNodeWithText("Sun").assertIsDisplayed()
        composeRule.onNodeWithText("31").assertIsDisplayed()
        composeRule.onNodeWithText("30").assertDoesNotExist()
    }

    @Test
    fun `TC-TIMETABLE-FEATURE-004 이동 범위의 처음에서 이전으로 넘어가지 않는다 - 주간`() {
        setTimetable(type = TimetableType.WEEK, date = LocalDate(year = 1, month = 1, day = 1))

        swipePageRight()

        composeRule.onNodeWithText("31").assertIsDisplayed()
        for (day in 1..6) composeRule.onNodeWithText(day.toString()).assertIsDisplayed()
        composeRule.onNodeWithText("24").assertDoesNotExist()
    }

    @Test
    fun `TC-TIMETABLE-FEATURE-005 배치하는 화면이 이동을 요청하면 이동한 날짜가 표시된다`() {
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
    fun `TC-TIMETABLE-FEATURE-006 이동 범위의 처음에서 이전 이동을 요청하면 표시 날짜가 유지된다`() {
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
    fun `TC-TIMETABLE-FEATURE-007 현재 시각의 날짜가 표시되면 현재 시각 위치를 표시한다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 23), now = TEN_O_CLOCK_ON_23)

        composeRule.onNodeWithTag(TIMETABLE_NOW_INDICATOR_TEST_TAG, useUnmergedTree = true).assertExists()
    }

    @Test
    fun `TC-TIMETABLE-FEATURE-008 현재 시각의 날짜가 표시되지 않으면 현재 시각 위치를 표시하지 않는다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 24), now = TEN_O_CLOCK_ON_23)

        composeRule.onNodeWithTag(TIMETABLE_NOW_INDICATOR_TEST_TAG, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-TIMETABLE-FEATURE-008 현재 시각을 지정하지 않으면 현재 시각 위치를 표시하지 않는다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 24), now = null)

        composeRule.onNodeWithTag(TIMETABLE_NOW_INDICATOR_TEST_TAG, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-TIMETABLE-FEATURE-009 시각 아이템은 시간대 영역에, 종일 아이템은 종일 영역에 표시된다`() {
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
    fun `TC-TIMETABLE-FEATURE-010 표시 날짜와 겹치는 종일 아이템이 없으면 종일 영역을 두지 않는다`() {
        setTimetable(type = TimetableType.DAY, date = september(day = 23)) {
            meetingItem()
            allDayItem(dateRange = september(day = 1)..september(day = 1), key = PAST) { Text(text = PAST) }
        }

        composeRule.onNodeWithText(MEETING).assertIsDisplayed()
        composeRule.onNodeWithText(PAST).assertDoesNotExist()
        composeRule.onNodeWithTag(TIMETABLE_ALL_DAY_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-TIMETABLE-FEATURE-011 아이템 지정이 바뀌면 별도 조작 없이 표시가 갱신된다`() {
        var hasMeeting by mutableStateOf(false)
        setTimetable(type = TimetableType.DAY, date = september(day = 23)) {
            if (hasMeeting) meetingItem()
        }
        composeRule.onNodeWithText(MEETING).assertDoesNotExist()

        composeRule.runOnIdle { hasMeeting = true }

        composeRule.onNodeWithText(MEETING).assertIsDisplayed()
    }

    @Test
    fun `TC-TIMETABLE-FEATURE-012 아이템이 없으면 빈 상태 안내 없이 시간대 안내만 보여 준다`() {
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
    fun `TC-TIMETABLE-FEATURE-013 여러 날에 걸친 종일 아이템은 어느 날짜 부분을 눌러도 같은 아이템이 선택된다`() {
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
    fun `TC-TIMETABLE-FEATURE-014 화면이 재생성되어도 보던 형식과 날짜를 유지한다`() {
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
    fun `TC-TIMETABLE-FEATURE-015 화면이 재생성되어도 보던 시간대를 유지한다`() {
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
        date = september(day = 23),
        startTime = LocalTime(hour = 10, minute = 0),
        endTime = LocalTime(hour = 11, minute = 0),
        key = MEETING,
    ) {
        Text(text = MEETING)
    }
}

private fun september(day: Int): LocalDate = LocalDate(year = 2026, month = 9, day = day)

private const val MEETING = "Meeting"
private const val VACATION = "Vacation"
private const val PAST = "Past"
private const val LAST_HOUR_LABEL = "11 PM"
private const val SCROLL_SWIPE_COUNT = 5
private val TEN_O_CLOCK_ON_23 = LocalDateTime(year = 2026, month = 9, day = 23, hour = 10, minute = 0)
