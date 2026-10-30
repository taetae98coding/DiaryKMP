package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.mockk
import io.mockk.verify
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
class TimetableSelectTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val eventList = mutableListOf<TimetableEvent>()

    @Test
    fun `TC-f37277dc-4612-468a-b15c-05e43cdfafad 시간대 영역을 길게 누르고 떼면 그 자리의 30분 칸 기간이 전달된다`() {
        setTimetable()

        performLongPress(timePosition(hour = 10, minute = 10))
        performUp()

        eventList shouldBe listOf(selectTime(start = september(day = 23, hour = 10), endInclusive = september(day = 23, hour = 10, minute = 30)))
    }

    @Test
    fun `TC-57db82aa-c934-49b1-b13d-3fbcfcda65e4 시간대 영역에서 드래그하면 처음 칸과 지금 칸을 포함하는 기간이 전달된다`() {
        val caseList =
            listOf(
                11 to selectTime(start = september(day = 23, hour = 10), endInclusive = september(day = 23, hour = 11, minute = 30)),
                9 to selectTime(start = september(day = 23, hour = 9), endInclusive = september(day = 23, hour = 10, minute = 30)),
            )
        setTimetable()

        caseList.forEach { (hour, expected) ->
            eventList.clear()
            performLongPress(timePosition(hour = 10, minute = 10))
            performMoveTo(timePosition(hour = hour, minute = 10))
            performUp()

            eventList shouldBe listOf(expected)
        }
    }

    @Test
    fun `TC-ed972a5f-10b1-429e-88d9-aa19e0d9c5a0 주간 형식에서 여러 날에 걸쳐 드래그하면 여러 날의 시각 기간이 전달된다`() {
        setTimetable(type = TimetableType.WEEK, date = september(day = 20))

        performLongPress(timePosition(hour = 10, minute = 10, day = 21))
        performMoveTo(timePosition(hour = 9, minute = 10, day = 23))
        performUp()

        eventList shouldBe listOf(selectTime(start = september(day = 21, hour = 10), endInclusive = september(day = 23, hour = 9, minute = 30)))
    }

    @Test
    fun `TC-0ef6e614-dfe2-4b64-b274-a07e5d22d6ff 날짜 머리줄에서 드래그하면 날짜 기간이 전달된다`() {
        val caseList =
            listOf(
                (22 to 24) to september(day = 22)..september(day = 24),
                (24 to 22) to september(day = 22)..september(day = 24),
                (22 to 22) to september(day = 22)..september(day = 22),
            )
        setTimetable(type = TimetableType.WEEK, date = september(day = 20))

        caseList.forEach { (dayPair, expected) ->
            eventList.clear()
            performLongPress(dayCenter(day = dayPair.first))
            performMoveTo(dayCenter(day = dayPair.second))
            performUp()

            eventList shouldBe listOf(TimetableEvent.SelectDate(dateRange = expected))
        }
    }

    @Test
    fun `TC-d423318f-51a8-4ab4-8ffa-0f5bcd1b68e7 종일 영역의 아이템 위를 길게 누르면 그 자리의 날짜에서 선택이 시작된다`() {
        var clickCount = 0
        setTimetable(type = TimetableType.WEEK, date = september(day = 20)) {
            allDayItem(dateRange = september(day = 22)..september(day = 22), key = VACATION) {
                Text(text = VACATION, modifier = Modifier.clickable { clickCount += 1 })
            }
        }

        performLongPress(
            composeRule
                .onNodeWithText(VACATION)
                .fetchSemanticsNode()
                .boundsInRoot.center,
        )
        performUp()

        eventList shouldBe listOf(TimetableEvent.SelectDate(dateRange = september(day = 22)..september(day = 22)))
        clickCount shouldBe 0
    }

    @Test
    fun `TC-695b8b32-ecb4-459d-973a-1dbefc0fbecd 시각 아이템 위를 길게 누르면 그 자리의 칸에서 선택이 시작된다`() {
        var clickCount = 0
        setTimetable { meetingItem(onClick = { clickCount += 1 }) }

        performLongPress(timePosition(hour = 10, minute = 10))
        performUp()

        eventList shouldBe listOf(selectTime(start = september(day = 23, hour = 10), endInclusive = september(day = 23, hour = 10, minute = 30)))
        clickCount shouldBe 0
    }

    @Test
    fun `TC-bfae5b1e-308c-44a4-801f-48cbaf95ea61 아이템을 짧게 누르면 기간이 전달되지 않는다`() {
        var clickCount = 0
        setTimetable { meetingItem(onClick = { clickCount += 1 }) }

        composeRule.onRoot().performTouchInput { click(timePosition(hour = 10, minute = 10)) }
        composeRule.waitForIdle()

        clickCount shouldBe 1
        eventList shouldBe emptyList()
    }

    @Test
    fun `TC-6403f47e-bb86-498f-9240-7685f73b182d 기간 선택을 사용하지 않으면 길게 눌러도 기간이 전달되지 않는다`() {
        setTimetable(isSelectEnabled = false)

        performLongPress(timePosition(hour = 10, minute = 10))
        performUp()
        performLongPress(dayCenter(day = 23))
        performUp()

        eventList shouldBe emptyList()
    }

    @Test
    fun `TC-235b6098-0209-48f9-9782-c4ff764294b6 선택하는 동안 좌우로 끌어도 날짜가 이동하지 않는다`() {
        val state = TimetableState(type = TimetableType.DAY, initialDate = september(day = 23))
        setTimetable(state = state)
        val start = timePosition(hour = 10, minute = 10)

        performLongPress(start)
        performMoveTo(start.copy(x = 1F))
        performUp()

        composeRule.runOnIdle { state.currentDateRange shouldBe september(day = 23)..september(day = 23) }
        composeRule.onNodeWithText("23").assertExists()
        eventList shouldBe listOf(selectTime(start = september(day = 23, hour = 10), endInclusive = september(day = 23, hour = 10, minute = 30)))
    }

    @Test
    fun `TC-3b456f60-32db-4bd5-96db-951abbd52451 시간대 선택을 날짜 머리줄로 끌어도 시간대 선택으로 유지된다`() {
        setTimetable()

        performLongPress(timePosition(hour = 10, minute = 10))
        performMoveTo(dayCenter(day = 23))
        performUp()

        eventList shouldBe listOf(selectTime(start = september(day = 23, hour = 8), endInclusive = september(day = 23, hour = 10, minute = 30)))
    }

    @Test
    fun `TC-80c8b320-ef53-4d15-b64d-378c2cfe8107 날짜 선택을 시간대 영역으로 끌어도 날짜 선택으로 유지된다`() {
        setTimetable(type = TimetableType.WEEK, date = september(day = 20))

        performLongPress(dayCenter(day = 22))
        performMoveTo(timePosition(hour = 10, minute = 10, day = 24))
        performUp()

        eventList shouldBe listOf(TimetableEvent.SelectDate(dateRange = september(day = 22)..september(day = 24)))
    }

    @Test
    fun `TC-298704cd-7a4e-402f-821d-9a8879b7722c 주간 형식에서 선택 기간은 표시 중인 주를 넘지 않는다`() {
        val state = TimetableState(type = TimetableType.WEEK, initialDate = september(day = 20))
        setTimetable(state = state)
        val start = dayCenter(day = 24)
        val rootWidth =
            composeRule
                .onRoot()
                .fetchSemanticsNode()
                .size.width

        performLongPress(start)
        performMoveTo(start.copy(x = rootWidth + OUTSIDE_DRAG_DISTANCE_PX))
        performUp()

        eventList shouldBe listOf(TimetableEvent.SelectDate(dateRange = september(day = 24)..september(day = 26)))
        composeRule.runOnIdle { state.currentDateRange shouldBe september(day = 20)..september(day = 26) }
    }

    @Test
    fun `TC-316365e6-e847-486a-bdcd-12ea6c823b1d 선택하는 동안 시스템이 조작을 끊으면 선택이 취소되고 기간이 전달되지 않는다`() {
        val state = TimetableState(type = TimetableType.DAY, initialDate = september(day = 23))
        setTimetable(state = state)

        performLongPress(timePosition(hour = 10, minute = 10))
        performMoveTo(timePosition(hour = 11, minute = 10))
        composeRule.onRoot().performTouchInput { cancel() }
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
        state.selectState.selection.shouldBeNull()
    }

    @Test
    fun `TC-316365e6-e847-486a-bdcd-12ea6c823b1d 선택하는 동안 앱이 백그라운드로 이동해 조작이 끊기면 선택이 취소되고 기간이 전달되지 않는다`() {
        // 백그라운드 이동은 화면 상태를 저장한 뒤 화면을 그대로 둔 채 제스처를 끊는 것으로 재현한다.
        val registry = SaveableStateRegistry(restoredValues = null) { true }
        val state = TimetableState(type = TimetableType.DAY, initialDate = september(day = 23))
        composeRule.setContent {
            CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
                DiaryTheme {
                    Timetable(modifier = Modifier.fillMaxSize(), state = state, onEvent = { eventList += it }) {}
                }
            }
        }
        composeRule.waitForIdle()

        performLongPress(timePosition(hour = 10, minute = 10))
        performMoveTo(timePosition(hour = 11, minute = 10))
        composeRule.runOnIdle { registry.performSave() }
        composeRule.onRoot().performTouchInput { cancel() }
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
        state.selectState.selection.shouldBeNull()
    }

    @Test
    fun `TC-316365e6-e847-486a-bdcd-12ea6c823b1d 선택하는 동안 화면을 벗어나 Timetable이 사라지면 선택이 취소되고 기간이 전달되지 않는다`() {
        var isShown by mutableStateOf(true)
        val state = TimetableState(type = TimetableType.DAY, initialDate = september(day = 23))
        composeRule.setContent {
            DiaryTheme {
                if (isShown) {
                    Timetable(modifier = Modifier.fillMaxSize(), state = state, onEvent = { eventList += it }) {}
                }
            }
        }
        composeRule.waitForIdle()

        performLongPress(timePosition(hour = 10, minute = 10))
        performMoveTo(timePosition(hour = 11, minute = 10))
        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
        state.selectState.selection.shouldBeNull()
    }

    @Test
    fun `TC-987fb756-658c-4f39-bbf8-0b2d53919e96 기간 선택을 시작하면 촉각 피드백을 받는다`() {
        val hapticFeedback = mockk<HapticFeedback>(relaxed = true)
        setTimetable(hapticFeedback = hapticFeedback)

        performLongPress(timePosition(hour = 10, minute = 10))

        verify(exactly = 1) { hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) }
        verify(exactly = 0) { hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick) }
    }

    @Test
    fun `TC-a1ed1ba5-950b-472a-a677-42a80056d8df 드래그로 선택 기간이 다른 칸으로 바뀔 때만 추가 촉각 피드백을 받는다`() {
        val caseList = listOf(Pair(11, 10) to 1, Pair(10, 20) to 0)
        val hapticFeedback = mockk<HapticFeedback>(relaxed = true)
        setTimetable(hapticFeedback = hapticFeedback)

        caseList.forEach { (time, count) ->
            performLongPress(timePosition(hour = 10, minute = 10))
            clearMocks(hapticFeedback, answers = false)

            performMoveTo(timePosition(hour = time.first, minute = time.second))

            verify(exactly = count) { hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick) }
            performUp()
        }
    }

    @Test
    fun `TC-9c94f435-750e-40dd-b910-62e293bbce7d 선택 중 화면이 재생성되면 선택이 취소되고 기간이 전달되지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                Timetable(
                    modifier = Modifier.fillMaxSize(),
                    state = rememberTimetableState(type = TimetableType.DAY, initialDate = september(day = 23)),
                    onEvent = { eventList += it },
                ) {}
            }
        }
        composeRule.waitForIdle()

        performLongPress(timePosition(hour = 10, minute = 10))
        performMoveTo(timePosition(hour = 11, minute = 10))
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
    }

    @Test
    fun `TC-fce1ec99-239c-4b2f-8f01-f7d9a04527cd 선택을 완료하면 선택된 기간이 없는 상태가 된다`() {
        val state = TimetableState(type = TimetableType.DAY, initialDate = september(day = 23))
        setTimetable(state = state)

        performLongPress(timePosition(hour = 10, minute = 10))
        performMoveTo(timePosition(hour = 11, minute = 10))
        performUp()

        eventList shouldBe listOf(selectTime(start = september(day = 23, hour = 10), endInclusive = september(day = 23, hour = 11, minute = 30)))
        state.selectState.selection.shouldBeNull()
    }

    @Test
    fun `선택을 완료하면 다음 길게 누르기는 새 칸에서 시작한다`() {
        setTimetable()

        performLongPress(timePosition(hour = 10, minute = 10))
        performMoveTo(timePosition(hour = 11, minute = 10))
        performUp()
        performLongPress(timePosition(hour = 13, minute = 10))
        performUp()

        eventList shouldBe
            listOf(
                selectTime(start = september(day = 23, hour = 10), endInclusive = september(day = 23, hour = 11, minute = 30)),
                selectTime(start = september(day = 23, hour = 13), endInclusive = september(day = 23, hour = 13, minute = 30)),
            )
    }

    @Test
    fun `TC-dbe54acb-fdb4-4d80-b9fe-df49ebb0325e 선택하는 동안 위아래로 끌어도 시간대가 스크롤되지 않는다`() {
        setTimetable()
        val start = timePosition(hour = 10, minute = 10)
        val tenY = hourLabelCenterY(label = TEN_AM_LABEL)
        val hourHeight = hourLabelCenterY(label = ELEVEN_AM_LABEL) - tenY

        val labelList = listOf(NINE_AM_LABEL, TEN_AM_LABEL, ELEVEN_AM_LABEL, NOON_LABEL)
        val initialLabelYList = labelList.map { label -> hourLabelCenterY(label = label) }

        performLongPress(start)
        performMoveTo(start.copy(y = start.y + hourHeight * 2))

        labelList.map { label -> hourLabelCenterY(label = label) } shouldBe initialLabelYList

        performUp()

        labelList.map { label -> hourLabelCenterY(label = label) } shouldBe initialLabelYList
    }

    private fun setTimetable(
        type: TimetableType = TimetableType.DAY,
        date: LocalDate = september(day = 23),
        state: TimetableState = TimetableState(type = type, initialDate = date),
        isSelectEnabled: Boolean = true,
        hapticFeedback: HapticFeedback? = null,
        content: TimetableScope.() -> Unit = {},
    ) {
        val onEvent: ((TimetableEvent) -> Unit)? = if (isSelectEnabled) eventList::add else null

        composeRule.setContent {
            DiaryTheme {
                CompositionLocalProvider(LocalHapticFeedback provides (hapticFeedback ?: LocalHapticFeedback.current)) {
                    Timetable(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        onEvent = onEvent,
                        content = content,
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun dayCenter(day: Int): Offset =
        composeRule
            .onNodeWithText(day.toString())
            .fetchSemanticsNode()
            .boundsInRoot.center

    private fun timePosition(
        hour: Int,
        minute: Int,
        day: Int? = null,
    ): Offset {
        val tenY = hourLabelCenterY(label = TEN_AM_LABEL)
        val hourHeight = hourLabelCenterY(label = ELEVEN_AM_LABEL) - tenY
        val x =
            day?.let { dayCenter(day = it).x }
                ?: composeRule
                    .onRoot()
                    .fetchSemanticsNode()
                    .size.width * 3F / 4F

        return Offset(x = x, y = tenY + (hour - 10 + minute / 60F) * hourHeight)
    }

    private fun hourLabelCenterY(label: String): Float =
        composeRule
            .onNodeWithText(label)
            .fetchSemanticsNode()
            .boundsInRoot.center.y

    private fun performLongPress(position: Offset) {
        composeRule.onRoot().performTouchInput {
            down(position)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_MARGIN_MILLIS)
            moveBy(Offset.Zero)
        }
        composeRule.waitForIdle()
    }

    private fun performMoveTo(position: Offset) {
        composeRule.onRoot().performTouchInput { moveTo(position) }
        composeRule.waitForIdle()
    }

    private fun performUp() {
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitForIdle()
    }
}

private fun TimetableScope.meetingItem(onClick: () -> Unit) {
    timeItem(
        start = LocalDateTime(date = september(day = 23), time = LocalTime(hour = 10, minute = 0)),
        endInclusive = LocalDateTime(date = september(day = 23), time = LocalTime(hour = 11, minute = 0)),
        key = MEETING,
    ) {
        Text(text = MEETING, modifier = Modifier.fillMaxSize().clickable(onClick = onClick))
    }
}

private fun selectTime(
    start: LocalDateTime,
    endInclusive: LocalDateTime,
): TimetableEvent.SelectTime = TimetableEvent.SelectTime(start = start, endInclusive = endInclusive)

private fun september(day: Int): LocalDate = LocalDate(year = 2026, month = 9, day = day)

private fun september(
    day: Int,
    hour: Int,
    minute: Int = 0,
): LocalDateTime = LocalDateTime(date = september(day = day), time = LocalTime(hour = hour, minute = minute))

private const val MEETING = "Meeting"
private const val NINE_AM_LABEL = "9 AM"
private const val TEN_AM_LABEL = "10 AM"
private const val ELEVEN_AM_LABEL = "11 AM"
private const val NOON_LABEL = "12 PM"
private const val VACATION = "Vacation"
private const val LONG_PRESS_MARGIN_MILLIS = 100L
private const val OUTSIDE_DRAG_DISTANCE_PX = 200F
