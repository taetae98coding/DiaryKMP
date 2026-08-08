package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasNoClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.feature.calendar.ui.permission.rememberLocationPermissionRequester
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScreenMoveTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val movedList = mutableListOf<Triple<Uuid, MemoDateTime, LocalDateRange>>()

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-001 메모 제목을 길게 누르면 메모 이동이 시작되고 날짜 기간 선택은 시작되지 않는다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))

        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.key shouldBe memo.id
        capturedState?.calendarSelectState?.dateRange.shouldBeNull()
        performUp()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-002 아이템이 없는 날짜 칸에서 길게 누르면 기존과 같이 날짜 기간 선택이 시작된다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        performLongPress(dayCenter(day = 8))

        capturedState?.calendarSelectState?.dateRange shouldBe july(day = 8)..july(day = 8)
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            .shouldBeNull()
        performUp()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-003 여러 주에 나뉜 조각 어느 것에서 시작해도 같은 메모의 이동이 시작된다`() {
        val memo = memo(title = TRIP_TITLE, start = july(day = 15), endInclusive = july(day = 22))
        setCalendarHomeScreen(memoListFlow = MutableStateFlow(listOf(memo)))
        composeRule.onAllNodesWithText(TRIP_TITLE).assertCountEquals(2)

        performLongPress(memoPressPosition(title = TRIP_TITLE, day = 20, pieceIndex = 1))
        performMoveTo(dayCenter(day = 21))
        performUp()

        movedList shouldBe listOf(Triple(memo.id, memo.dateTime, july(day = 16)..july(day = 23)))
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-004 드래그로 가리키는 날짜를 바꾸면 옮겨질 기간이 즉시 갱신된다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))
        performMoveTo(dayCenter(day = 22))

        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.toDateRange shouldBe july(day = 21)..july(day = 23)
        performUp()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-005 이동을 완료하면 날짜 차이만큼 옮겨진 기간이 반영된다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        setCalendarHomeScreen(memoListFlow = MutableStateFlow(listOf(memo)))

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))
        performMoveTo(dayCenter(day = 22))
        performUp()

        movedList shouldBe listOf(Triple(memo.id, memo.dateTime, july(day = 21)..july(day = 23)))
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-006 이동을 시작한 날짜와 같은 날짜에서 완료하면 메모가 바뀌지 않는다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        setCalendarHomeScreen(memoListFlow = MutableStateFlow(listOf(memo)))

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))
        performUp()

        movedList shouldBe emptyList()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-011 손을 떼지 않고 이동이 끝나도 그 시점의 기간이 반영된다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))
        performMoveTo(dayCenter(day = 22))
        performCancel()

        movedList shouldBe listOf(Triple(memo.id, memo.dateTime, july(day = 21)..july(day = 23)))
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            .shouldBeNull()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-008 화면이 재생성되면 이동 중 표시가 남지 않고 그 시점의 기간이 반영된다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        val restorationTester = StateRestorationTester(composeRule)
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            restorationTester = restorationTester,
            onState = { capturedState = it },
        )

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))
        performMoveTo(dayCenter(day = 22))
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        movedList shouldBe listOf(Triple(memo.id, memo.dateTime, july(day = 21)..july(day = 23)))
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            .shouldBeNull()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-007 메모 이동을 시작하면 촉각 피드백을 받는다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        val hapticFeedback = mockk<HapticFeedback>(relaxed = true)
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            hapticFeedback = hapticFeedback,
        )

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))

        verify(exactly = 1) { hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress) }
        performUp()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-008 드래그로 옮겨질 기간이 달라지면 추가 촉각 피드백을 받는다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        val hapticFeedback = mockk<HapticFeedback>(relaxed = true)
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            hapticFeedback = hapticFeedback,
        )

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))
        clearMocks(hapticFeedback, answers = false)

        performMoveTo(dayCenter(day = 16))

        verify(exactly = 1) { hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick) }
        performUp()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-009 드래그해도 옮겨질 기간이 그대로이면 추가 촉각 피드백을 받지 않는다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        val hapticFeedback = mockk<HapticFeedback>(relaxed = true)
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            hapticFeedback = hapticFeedback,
        )

        val pressPosition = memoPressPosition(title = MEETING_TITLE, day = 15)
        performLongPress(pressPosition)
        clearMocks(hapticFeedback, answers = false)

        performMoveTo(pressPosition + Offset(x = 2F, y = 0F))

        verify(exactly = 0) { hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick) }
        performUp()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-010 이동 중 오른쪽 가장자리 영역에 머무르면 다음 달로 이동하고 메모 이동이 유지된다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        val pressPosition = memoPressPosition(title = MEETING_TITLE, day = 15)
        performLongPress(pressPosition)
        composeRule.mainClock.autoAdvance = false
        performMove(Offset(x = rootWidth() - 1F, y = pressPosition.y))
        advanceTimeUntil { capturedState?.calendarState?.currentYearMonth == AUGUST_2026 }

        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.key shouldBe memo.id
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.toDateRange shouldNotBe memo.dateTime.toDateRange()

        performUpAndSettle()
        movedList.size shouldBe 1
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-003 드래그하는 동안 다른 경로로 기간이 바뀌어도 이동 시작 시점의 기간을 기준으로 완료를 전달한다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        val memoListFlow = MutableStateFlow(listOf(memo))
        setCalendarHomeScreen(memoListFlow = memoListFlow)

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))

        memoListFlow.value = listOf(memo.copy(dateTime = MemoDateTime.AllDay(dateRange = july(day = 1)..july(day = 2))))
        composeRule.waitForIdle()

        performMoveTo(dayCenter(day = 22))
        performUp()

        movedList shouldBe listOf(Triple(memo.id, memo.dateTime, july(day = 21)..july(day = 23)))
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-006 이동 범위를 벗어나는 방향으로는 달이 이동하지 않고 메모 이동이 유지된다`() {
        val memo =
            memo(
                title = MEETING_TITLE,
                start = firstYearJanuary(day = 14),
                endInclusive = firstYearJanuary(day = 16),
            )
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            initialYearMonth = FIRST_YEAR_MONTH,
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        val pressPosition = memoPressPosition(title = MEETING_TITLE, day = 15)
        performLongPress(pressPosition)
        composeRule.mainClock.autoAdvance = false
        performMove(Offset(x = 1F, y = pressPosition.y))
        composeRule.mainClock.advanceTimeBy(IDLE_WAIT_MILLIS)

        capturedState?.calendarState?.currentYearMonth shouldBe FIRST_YEAR_MONTH
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.key shouldBe memo.id
        performUpAndSettle()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-007 이웃 달에 속한 날짜 칸으로 완료하면 실제 날짜 그대로 반영된다`() {
        val memo = memo(title = MEETING_TITLE, start = june(day = 23), endInclusive = june(day = 24))
        setCalendarHomeScreen(
            initialYearMonth = JUNE_2026,
            memoListFlow = MutableStateFlow(listOf(memo)),
        )
        composeRule.onAllNodes(hasText("2").and(hasNoClickAction())).assertCountEquals(2)

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 23))
        performMoveTo(dayCenter(day = 2, index = 1))
        performUp()

        movedList shouldBe listOf(Triple(memo.id, memo.dateTime, july(day = 2)..july(day = 3)))
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-012 이동 중 다른 손가락으로 왼쪽으로 스와이프하면 다음 달로 이동하고 메모 이동이 유지된다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        val pressPosition = memoPressPosition(title = MEETING_TITLE, day = 15)
        performLongPress(pressPosition)
        performSecondaryDown(secondaryPosition(pressPosition))
        performSecondaryMoveBy(monthSwipeDelta(direction = -1))

        capturedState?.calendarState?.currentYearMonth shouldBe AUGUST_2026
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.key shouldBe memo.id
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.toDateRange shouldBe august(day = 11)..august(day = 13)
        performSecondaryUp()
        performUp()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-013 스와이프한 손가락을 떼도 메모 이동이 이어지고 메모를 누른 손가락을 떼면 반영된다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        val pressPosition = memoPressPosition(title = MEETING_TITLE, day = 15)
        performLongPress(pressPosition)
        performSecondaryDown(secondaryPosition(pressPosition))
        performSecondaryMoveBy(monthSwipeDelta(direction = -1))
        performSecondaryUp()

        capturedState?.calendarState?.currentYearMonth shouldBe AUGUST_2026

        performMoveTo(dayCenter(day = 5))
        performUp()

        movedList shouldBe listOf(Triple(memo.id, memo.dateTime, august(day = 4)..august(day = 6)))
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-010 이동 중 다른 손가락으로 오른쪽으로 스와이프하면 이전 달로 이동한다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        val pressPosition = memoPressPosition(title = MEETING_TITLE, day = 15)
        performLongPress(pressPosition)
        performSecondaryDown(secondaryPosition(pressPosition))
        performSecondaryMoveBy(monthSwipeDelta(direction = 1))

        capturedState?.calendarState?.currentYearMonth shouldBe JUNE_2026
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.key shouldBe memo.id
        performSecondaryUp()
        performUp()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-011 한 번 누른 손가락의 스와이프로는 한 달만 이동한다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        val pressPosition = memoPressPosition(title = MEETING_TITLE, day = 15)
        performLongPress(pressPosition)
        performSecondaryDown(secondaryPosition(pressPosition))
        performSecondaryMoveBy(monthSwipeDelta(direction = -1))
        performSecondaryMoveBy(monthSwipeDelta(direction = -1))

        capturedState?.calendarState?.currentYearMonth shouldBe AUGUST_2026
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.key shouldBe memo.id
        performSecondaryUp()
        performUp()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-012 이동 중 다른 손가락으로 세로로 움직여도 달이 이동하지 않는다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        val pressPosition = memoPressPosition(title = MEETING_TITLE, day = 15)
        performLongPress(pressPosition)
        performSecondaryDown(secondaryPosition(pressPosition))
        performSecondaryMoveBy(monthSwipeDelta(direction = -1).let { delta -> Offset(x = 0F, y = delta.x) })

        capturedState?.calendarState?.currentYearMonth shouldBe JULY_2026
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.key shouldBe memo.id
        performSecondaryUp()
        performUp()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-013 날짜 기간 선택 중에는 다른 손가락의 스와이프로 달이 이동하지 않는다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        val pressPosition = dayCenter(day = 8)
        performLongPress(pressPosition)
        performSecondaryDown(secondaryPosition(pressPosition))
        performSecondaryMoveBy(Offset(x = -rootWidth() * PAGE_CROSSING_RATIO, y = 0F))

        capturedState?.calendarState?.currentYearMonth shouldBe JULY_2026
        capturedState?.calendarSelectState?.dateRange shouldBe july(day = 8)..july(day = 8)
        performSecondaryUp()
        performUp()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-006 이동 범위를 벗어나는 방향으로 스와이프해도 달이 이동하지 않고 메모 이동이 유지된다`() {
        val memo =
            memo(
                title = MEETING_TITLE,
                start = firstYearJanuary(day = 14),
                endInclusive = firstYearJanuary(day = 16),
            )
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            initialYearMonth = FIRST_YEAR_MONTH,
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        val pressPosition = memoPressPosition(title = MEETING_TITLE, day = 15)
        performLongPress(pressPosition)
        performSecondaryDown(secondaryPosition(pressPosition))
        performSecondaryMoveBy(monthSwipeDelta(direction = 1))

        capturedState?.calendarState?.currentYearMonth shouldBe FIRST_YEAR_MONTH
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.key shouldBe memo.id
        performSecondaryUp()
        performUp()
    }

    @Test
    fun `이동이 진행되는 동안 모든 조각의 고스트 아이템이 함께 표시되고 완료하면 사라진다`() {
        val memo = memo(title = TRIP_TITLE, start = july(day = 15), endInclusive = july(day = 22))
        setCalendarHomeScreen(memoListFlow = MutableStateFlow(listOf(memo)))
        composeRule.onAllNodesWithText(TRIP_TITLE).assertCountEquals(2)

        performLongPress(memoPressPosition(title = TRIP_TITLE, day = 16))
        composeRule.onAllNodesWithText(TRIP_TITLE).assertCountEquals(PIECE_COUNT + GHOST_COUNT)

        performUp()
        composeRule.onAllNodesWithText(TRIP_TITLE).assertCountEquals(2)
    }

    private fun setCalendarHomeScreen(
        memoListFlow: StateFlow<List<CalendarMemo>>,
        initialYearMonth: YearMonth = JULY_2026,
        hapticFeedback: HapticFeedback? = null,
        restorationTester: StateRestorationTester? = null,
        onState: (CalendarHomeScaffoldState) -> Unit = {},
    ) {
        val memoViewModel =
            mockk<CalendarHomeMemoViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.memoList } returns memoListFlow
                every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
                every { viewModel.move(id = any(), fromDateTime = any(), toDateRange = any()) } answers {
                    movedList += Triple(firstArg(), secondArg(), thirdArg())
                }
            }

        val setContent: (@Composable () -> Unit) -> Unit =
            restorationTester?.let { tester -> { content -> tester.setContent(content) } } ?: composeRule::setContent

        setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = initialYearMonth),
                )
            onState(state)

            DiaryTheme {
                CompositionLocalProvider(
                    LocalHapticFeedback provides (hapticFeedback ?: LocalHapticFeedback.current),
                ) {
                    CalendarHomeScreen(
                        navigateToMemoDetail = {},
                        navigateToMemoAdd = {},
                        navigateToContactDetail = {},
                        birthdayViewModel = birthdayViewModel(),
                        navigateToFilter = {},
                        state = state,
                        holidayViewModel = holidayViewModel(),
                        memoViewModel = memoViewModel,
                        weatherViewModel = weatherViewModel(),
                        syncViewModel = syncViewModel(),
                        locationPermissionRequester = rememberLocationPermissionRequester(),
                    )
                }
            }
        }
    }

    // 상단 바 오늘 버튼도 일 숫자를 표시하므로 클릭할 수 없는 날짜 숫자만 대상으로 삼는다.
    private fun dayCenter(
        day: Int,
        index: Int = 0,
    ): Offset =
        composeRule
            .onAllNodes(hasText(day.toString()).and(hasNoClickAction()))[index]
            .fetchSemanticsNode()
            .boundsInRoot
            .center

    // 조각 가운데는 날짜 경계에 걸릴 수 있어 x는 지정한 날짜 칸의 중심, y는 조각의 세로 중심을 사용한다.
    private fun memoPressPosition(
        title: String,
        day: Int,
        pieceIndex: Int = 0,
        dayIndex: Int = 0,
    ): Offset {
        val bounds =
            composeRule
                .onAllNodesWithText(title)[pieceIndex]
                .fetchSemanticsNode()
                .boundsInRoot

        return Offset(x = dayCenter(day = day, index = dayIndex).x, y = bounds.center.y)
    }

    private fun rootWidth(): Float =
        composeRule
            .onRoot()
            .fetchSemanticsNode()
            .size.width
            .toFloat()

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

    // 가장자리 머무름처럼 달 이동이 반복되는 동안에는 idle 상태가 되지 않으므로 클록 자동 진행을 끈 뒤 이동만 주입한다.
    private fun performMove(position: Offset) {
        composeRule.onRoot().performTouchInput { moveTo(position) }
    }

    private fun performCancel() {
        composeRule.onRoot().performTouchInput { cancel() }
        composeRule.waitForIdle()
    }

    private fun performUp() {
        composeRule.onRoot().performTouchInput { up() }
        composeRule.waitForIdle()
    }

    // 손을 뗀 뒤 클록 자동 진행을 복구해 진행 중인 달 전환이 끝나기를 기다린다.
    private fun performUpAndSettle() {
        composeRule.onRoot().performTouchInput { up() }
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
    }

    private fun performSecondaryDown(position: Offset) {
        composeRule.onRoot().performTouchInput { down(pointerId = SECONDARY_POINTER_ID, position = position) }
        composeRule.waitForIdle()
    }

    private fun performSecondaryMoveBy(delta: Offset) {
        composeRule.onRoot().performTouchInput { moveBy(pointerId = SECONDARY_POINTER_ID, delta = delta) }
        composeRule.waitForIdle()
    }

    private fun performSecondaryUp() {
        composeRule.onRoot().performTouchInput { up(pointerId = SECONDARY_POINTER_ID) }
        composeRule.waitForIdle()
    }

    // 메모를 누른 손가락과 겹치지 않고 가장자리 영역에도 닿지 않는 캘린더 안의 지점을 사용한다.
    private fun secondaryPosition(pressPosition: Offset): Offset = Offset(x = rootWidth() / 2F, y = pressPosition.y)

    // 스와이프 달 이동은 가로 48dp를 넘겨야 인식되므로 그보다 넉넉한 거리를 사용한다.
    private fun monthSwipeDelta(direction: Int): Offset =
        Offset(
            x = direction * with(composeRule.density) { MONTH_SWIPE_DISTANCE.toPx() },
            y = 0F,
        )

    private fun advanceTimeUntil(
        timeoutMillis: Long = WAIT_TIMEOUT_MILLIS,
        condition: () -> Boolean,
    ) {
        var elapsedMillis = 0L

        while (!condition()) {
            check(elapsedMillis < timeoutMillis) { "${timeoutMillis}ms 안에 조건이 충족되지 않았다." }

            composeRule.mainClock.advanceTimeByFrame()
            elapsedMillis += FRAME_MILLIS
        }
    }

    private fun memo(
        title: String,
        start: LocalDate,
        endInclusive: LocalDate,
    ): CalendarMemo =
        CalendarMemo(
            id = Uuid.random(),
            title = title,
            color = fixtureMonkey.giveMeOne(),
            dateTime = MemoDateTime.AllDay(dateRange = start..endInclusive),
        )

    private fun MemoDateTime.toDateRange(): LocalDateRange =
        when (this) {
            is MemoDateTime.AllDay -> dateRange
            is MemoDateTime.DateTime -> start.date..endInclusive.date
        }

    private fun june(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JUNE, day = day)

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    private fun august(day: Int): LocalDate = LocalDate(year = 2026, month = Month.AUGUST, day = day)

    private fun firstYearJanuary(day: Int): LocalDate = LocalDate(year = 1, month = Month.JANUARY, day = day)

    companion object {
        private const val MEETING_TITLE = "회의"
        private const val TRIP_TITLE = "여행"

        // 두 주에 걸친 메모는 원본 조각 2개와 같은 수의 고스트 아이템이 표시된다.
        private const val PIECE_COUNT = 2
        private const val GHOST_COUNT = 2
        private const val LONG_PRESS_MARGIN_MILLIS = 100L
        private const val WAIT_TIMEOUT_MILLIS = 10_000L
        private const val IDLE_WAIT_MILLIS = 5_000L
        private const val FRAME_MILLIS = 16L
        private const val SECONDARY_POINTER_ID = 1
        private val MONTH_SWIPE_DISTANCE = 64.dp

        // 페이저 스와이프로 처리되면 달이 넘어가는 거리여야 달 이동이 일어나지 않음을 확인할 수 있다.
        private const val PAGE_CROSSING_RATIO = 0.7F
        private val JUNE_2026 = YearMonth(year = 2026, month = Month.JUNE)
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
        private val AUGUST_2026 = YearMonth(year = 2026, month = Month.AUGUST)
        private val FIRST_YEAR_MONTH = YearMonth(year = 1, month = Month.JANUARY)
    }
}
