package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.MoveMemoUseCase
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.toDateRange
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.toDateRange
import io.github.taetae98coding.diary.feature.calendar.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
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

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScreenMoveTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val movedList = mutableListOf<Triple<Uuid, MemoDateTime, LocalDateRange>>()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

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
        val movedDateRange = july(day = 21)..july(day = 23)
        val storedMemoFlow = MutableStateFlow(Result.success(listOf(memo)))
        val moveMemoUseCase = mockk<MoveMemoUseCase>()
        coEvery { moveMemoUseCase(parameter = any()) } answers {
            val parameter = firstArg<MoveMemoUseCase.Parameter>()
            storedMemoFlow.value = Result.success(listOf(memo.copy(dateTime = MemoDateTime.AllDay(dateRange = parameter.toDateRange))))
            Result.success(Unit)
        }
        val getCalendarMemoUseCase = mockk<GetCalendarMemoUseCase>()
        every { getCalendarMemoUseCase(parameter = any()) } returns storedMemoFlow
        val getCalendarFilterUseCase = mockk<GetCalendarFilterUseCase>()
        every { getCalendarFilterUseCase(parameter = Unit) } returns flowOf(Result.success(emptyList()))
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(emptyList()),
            memoViewModel =
                CalendarHomeMemoViewModel(
                    getCalendarFilterUseCase = getCalendarFilterUseCase,
                    getCalendarMemoUseCase = getCalendarMemoUseCase,
                    moveMemoUseCase = moveMemoUseCase,
                ),
        )
        composeRule.onAllNodesWithText(MEETING_TITLE).assertCountEquals(1)

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))
        performMoveTo(dayCenter(day = 22))
        performUp()

        coVerify(exactly = 1) {
            moveMemoUseCase(
                parameter = MoveMemoUseCase.Parameter(id = memo.id, fromDateTime = memo.dateTime, toDateRange = movedDateRange),
            )
        }
        composeRule.onAllNodesWithText(MEETING_TITLE).assertCountEquals(1)
        val bounds =
            composeRule
                .onNodeWithText(MEETING_TITLE)
                .fetchSemanticsNode()
                .boundsInRoot
        // 바뀐 기간의 가운데인 7월 22일 칸에 걸쳐 있고, 원래 기간이 있던 7월 14일의 주 줄보다 아래에 놓인다.
        val movedCenter = dayCenter(day = 22)
        (bounds.left < movedCenter.x && movedCenter.x < bounds.right) shouldBe true
        (dayCenter(day = 14).y < movedCenter.y && movedCenter.y < bounds.center.y) shouldBe true
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
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
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-009 앱이 백그라운드로 이동해도 이동 중 표시가 남지 않고 그 시점의 기간이 반영된다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        val registry = SaveableStateRegistry(restoredValues = null) { true }
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            saveableStateRegistry = registry,
            onState = { capturedState = it },
        )

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))
        performMoveTo(dayCenter(day = 22))
        composeRule.runOnIdle { registry.performSave() }
        performCancel()

        movedList shouldBe listOf(Triple(memo.id, memo.dateTime, july(day = 21)..july(day = 23)))
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            .shouldBeNull()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-014 화면을 벗어나면 이동 중 표시가 남지 않고 그 시점의 기간이 반영된다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var isShown by mutableStateOf(true)
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            isShown = { isShown },
        )

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))
        performMoveTo(dayCenter(day = 22))
        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()

        movedList shouldBe listOf(Triple(memo.id, memo.dateTime, july(day = 21)..july(day = 23)))
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-015 이동 중 달 이동으로 옮겨질 기간이 달라지면 추가 촉각 피드백을 받는다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        val hapticFeedback = mockk<HapticFeedback>(relaxed = true)
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            hapticFeedback = hapticFeedback,
            onState = { capturedState = it },
        )

        val pressPosition = memoPressPosition(title = MEETING_TITLE, day = 15)
        performLongPress(pressPosition)
        clearMocks(hapticFeedback, answers = false)

        performSecondaryDown(secondaryPosition(pressPosition))
        performSecondaryMoveBy(monthSwipeDelta(direction = -1))

        capturedState?.calendarState?.currentYearMonth shouldBe AUGUST_2026
        verify(exactly = 1) { hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick) }
        performSecondaryUp()
        performUp()
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
        // 2026년 8월 화면에서 포인터 위치(셋째 주 토요일)의 날짜는 8월 15일이므로 7월 15일에서 31일을 옮긴 기간이 된다.
        val expectedDateRange = august(day = 14)..august(day = 16)
        advanceTimeUntil {
            capturedState
                ?.calendarState
                ?.moveState
                ?.moving
                ?.toDateRange == expectedDateRange
        }

        capturedState?.calendarState?.currentYearMonth shouldBe AUGUST_2026
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.key shouldBe memo.id

        performUpAndSettle()
        movedList shouldBe listOf(Triple(memo.id, memo.dateTime, expectedDateRange))
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-016 이동 중 가장자리 영역에 머무는 동안 달 이동이 반복된다`() {
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
        // 2026년 9월 화면에서 포인터 위치(셋째 주 토요일)의 날짜는 9월 19일이므로 7월 15일에서 66일을 옮긴 기간이 된다.
        val expectedDateRange = september(day = 18)..september(day = 20)
        advanceTimeUntil {
            capturedState
                ?.calendarState
                ?.moveState
                ?.moving
                ?.toDateRange == expectedDateRange
        }

        capturedState?.calendarState?.currentYearMonth shouldBe SEPTEMBER_2026
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.key shouldBe memo.id
        performUpAndSettle()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-016 가장자리 영역 밖에서는 이동을 위한 드래그로 달이 이동하지 않는다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))
        performMoveTo(dayCenter(day = 17))
        composeRule.mainClock.advanceTimeBy(IDLE_WAIT_MILLIS)

        capturedState?.calendarState?.currentYearMonth shouldBe JULY_2026
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.key shouldBe memo.id
        performUp()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-017 이동 중 왼쪽 가장자리 영역에 머무르면 이전 달로 이동하고 메모 이동이 유지된다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo)),
            onState = { capturedState = it },
        )

        val pressPosition = memoPressPosition(title = MEETING_TITLE, day = 15)
        performLongPress(pressPosition)
        composeRule.mainClock.autoAdvance = false
        performMove(Offset(x = 1F, y = pressPosition.y))
        // 2026년 6월 화면에서 포인터 위치(셋째 주 일요일)의 날짜는 6월 14일이므로 7월 15일에서 31일을 앞당긴 기간이 된다.
        val expectedDateRange = june(day = 13)..june(day = 15)
        advanceTimeUntil {
            capturedState
                ?.calendarState
                ?.moveState
                ?.moving
                ?.toDateRange == expectedDateRange
        }

        capturedState?.calendarState?.currentYearMonth shouldBe JUNE_2026
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            ?.key shouldBe memo.id
        performUpAndSettle()
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
        composeRule.onAllNodes(CalendarHomeTestFixture.dateCell(day = 2)).assertCountEquals(2)

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 23))
        performMoveTo(dayCenter(day = 2, index = 1))
        performUp()

        movedList shouldBe listOf(Triple(memo.id, memo.dateTime, july(day = 2)..july(day = 3)))
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DOMAIN-015 캘린더 밖으로 드래그해 완료하면 가장 가까운 날짜 칸을 기준으로 반영된다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        setCalendarHomeScreen(memoListFlow = MutableStateFlow(listOf(memo)))
        val start = memoPressPosition(title = MEETING_TITLE, day = 15)

        performLongPress(start)
        performMoveTo(Offset(x = start.x, y = rootHeight() + BELOW_CALENDAR_DISTANCE))
        performUp()

        movedList shouldBe listOf(Triple(memo.id, memo.dateTime, august(day = 4)..august(day = 6)))
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

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-014 공휴일 이름 위에서 길게 누르면 날짜 기간 선택이 시작되고 검색 결과는 열리지 않는다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        val memoAddList = mutableListOf<LocalDateRange>()
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16)))),
            holidayList = listOf(Holiday(name = CONSTITUTION_DAY_NAME, isHoliday = true, dateRange = july(day = 17)..july(day = 17))),
            uriHandler = uriHandler,
            navigateToMemoAdd = memoAddList::add,
            onState = { capturedState = it },
        )

        performLongPress(itemPressPosition(text = CONSTITUTION_DAY_NAME, day = 17))

        capturedState?.calendarSelectState?.dateRange shouldBe july(day = 17)..july(day = 17)
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            .shouldBeNull()
        performUp()
        memoAddList shouldBe listOf(july(day = 17)..july(day = 17))
        verify(exactly = 0) { uriHandler.openUri(any()) }
        movedList.shouldBeEmpty()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-014 생일 위에서 길게 누르면 날짜 기간 선택이 시작되고 ContactDetail 화면으로 이동하지 않는다`() {
        val contactDetailList = mutableListOf<Uuid>()
        val memoAddList = mutableListOf<LocalDateRange>()
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16)))),
            birthdayList = listOf(CalendarContactBirthday(contactId = fixtureMonkey.giveMeOne<Uuid>(), name = BIRTHDAY_NAME, date = july(day = 8))),
            navigateToContactDetail = contactDetailList::add,
            navigateToMemoAdd = memoAddList::add,
            onState = { capturedState = it },
        )

        performLongPress(itemPressPosition(text = BIRTHDAY_TEXT, day = 8))

        capturedState?.calendarSelectState?.dateRange shouldBe july(day = 8)..july(day = 8)
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            .shouldBeNull()
        performUp()
        memoAddList shouldBe listOf(july(day = 8)..july(day = 8))
        contactDetailList.shouldBeEmpty()
        movedList.shouldBeEmpty()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h891dp")
    fun `TC-CALENDAR-MEMO-MOVE-FEATURE-014 날씨 위에서 길게 누르면 날짜 기간 선택이 시작되고 검색 결과는 열리지 않는다`() {
        val uriHandler = mockk<UriHandler>(relaxed = true)
        val memoAddList = mutableListOf<LocalDateRange>()
        var capturedState: CalendarHomeScaffoldState? = null
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(listOf(memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16)))),
            weatherList =
                listOf(
                    calendarWeather(
                        date = july(day = 21),
                        temperature = CalendarWeatherTemperature.MinMax(min = 17.8, max = 28.2),
                        descriptionList = listOf(SUNNY_DESCRIPTION),
                    ),
                ),
            uriHandler = uriHandler,
            navigateToMemoAdd = memoAddList::add,
            onState = { capturedState = it },
        )

        performLongPress(itemPressPosition(text = WEATHER_TEMPERATURE_TEXT, day = 21))

        capturedState?.calendarSelectState?.dateRange shouldBe july(day = 21)..july(day = 21)
        capturedState
            ?.calendarState
            ?.moveState
            ?.moving
            .shouldBeNull()
        performUp()
        memoAddList shouldBe listOf(july(day = 21)..july(day = 21))
        verify(exactly = 0) { uriHandler.openUri(any()) }
        movedList.shouldBeEmpty()
    }

    @Test
    fun `TC-CALENDAR-MEMO-MOVE-DATA-002 저장에 실패하면 원래 기간으로 표시되고 오류 안내가 표시되지 않는다`() {
        val memo = memo(title = MEETING_TITLE, start = july(day = 14), endInclusive = july(day = 16))
        val moveMemoUseCase = mockk<MoveMemoUseCase>()
        coEvery { moveMemoUseCase(parameter = any()) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
        val getCalendarMemoUseCase = mockk<GetCalendarMemoUseCase>()
        every { getCalendarMemoUseCase(parameter = any()) } returns flowOf(Result.success(listOf(memo)))
        val getCalendarFilterUseCase = mockk<GetCalendarFilterUseCase>()
        every { getCalendarFilterUseCase(parameter = Unit) } returns flowOf(Result.success(emptyList()))
        setCalendarHomeScreen(
            memoListFlow = MutableStateFlow(emptyList()),
            memoViewModel =
                CalendarHomeMemoViewModel(
                    getCalendarFilterUseCase = getCalendarFilterUseCase,
                    getCalendarMemoUseCase = getCalendarMemoUseCase,
                    moveMemoUseCase = moveMemoUseCase,
                ),
        )
        composeRule.onAllNodesWithText(MEETING_TITLE).assertCountEquals(1)

        performLongPress(memoPressPosition(title = MEETING_TITLE, day = 15))
        performMoveTo(dayCenter(day = 22))
        performUp()

        coVerify(exactly = 1) { moveMemoUseCase(parameter = any()) }
        composeRule.onAllNodesWithText(MEETING_TITLE).assertCountEquals(1)
        val bounds =
            composeRule
                .onNodeWithText(MEETING_TITLE)
                .fetchSemanticsNode()
                .boundsInRoot
        // 원래 기간의 첫날인 7월 14일 칸에 걸쳐 있고, 옮기려던 7월 21일이 있는 다음 주 줄보다 위에 남아 있다.
        val originalStart = dayCenter(day = 14)
        (bounds.left < originalStart.x && originalStart.x < bounds.right) shouldBe true
        (originalStart.y < bounds.center.y && bounds.center.y < dayCenter(day = 21).y) shouldBe true
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
    }

    private fun setCalendarHomeScreen(
        memoListFlow: StateFlow<List<CalendarMemo>>,
        initialYearMonth: YearMonth = JULY_2026,
        hapticFeedback: HapticFeedback? = null,
        restorationTester: StateRestorationTester? = null,
        holidayList: List<Holiday> = emptyList(),
        birthdayList: List<CalendarContactBirthday> = emptyList(),
        weatherList: List<CalendarWeather> = emptyList(),
        uriHandler: UriHandler? = null,
        navigateToMemoAdd: (LocalDateRange) -> Unit = {},
        navigateToContactDetail: (Uuid) -> Unit = {},
        memoViewModel: CalendarHomeMemoViewModel? = null,
        saveableStateRegistry: SaveableStateRegistry? = null,
        isShown: () -> Boolean = { true },
        onState: (CalendarHomeScaffoldState) -> Unit = {},
    ) {
        val holidayViewModel = holidayViewModel(holidayListFlow = MutableStateFlow(holidayList))
        val birthdayViewModel = birthdayViewModel(birthdayListFlow = MutableStateFlow(birthdayList))
        val weatherViewModel =
            weatherViewModel(weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = weatherList)))
        val mockMemoViewModel =
            memoViewModel ?: mockk<CalendarHomeMemoViewModel>().also { viewModel ->
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
            CompositionLocalProvider(LocalSaveableStateRegistry provides (saveableStateRegistry ?: LocalSaveableStateRegistry.current)) {
                CalendarHomeScreenContent(
                    initialYearMonth = initialYearMonth,
                    hapticFeedback = hapticFeedback,
                    uriHandler = uriHandler,
                    isShown = isShown,
                    onState = onState,
                ) { state ->
                    CalendarHomeScreen(
                        navigateToMemoDetail = {},
                        navigateToMemoAdd = navigateToMemoAdd,
                        navigateToContactDetail = navigateToContactDetail,
                        birthdayViewModel = birthdayViewModel,
                        navigateToFilter = {},
                        navigateToTimetable = {},
                        state = state,
                        holidayViewModel = holidayViewModel,
                        memoViewModel = mockMemoViewModel,
                        weatherViewModel = weatherViewModel,
                        syncViewModel = syncViewModel(),
                        permissionManager = rememberPermissionManager(),
                    )
                }
            }
        }
    }

    @Composable
    private fun CalendarHomeScreenContent(
        initialYearMonth: YearMonth,
        hapticFeedback: HapticFeedback?,
        uriHandler: UriHandler?,
        isShown: () -> Boolean,
        onState: (CalendarHomeScaffoldState) -> Unit,
        content: @Composable (CalendarHomeScaffoldState) -> Unit,
    ) {
        val state =
            rememberCalendarHomeScaffoldState(
                calendarState = rememberCalendarState(initialYearMonth = initialYearMonth),
            )
        onState(state)

        DiaryTheme {
            CompositionLocalProvider(
                LocalHapticFeedback provides (hapticFeedback ?: LocalHapticFeedback.current),
                LocalUriHandler provides (uriHandler ?: LocalUriHandler.current),
            ) {
                if (isShown()) {
                    content(state)
                }
            }
        }
    }

    private fun dayCenter(
        day: Int,
        index: Int = 0,
    ): Offset =
        composeRule
            .onAllNodes(CalendarHomeTestFixture.dateCell(day = day))[index]
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

    // 아이템 가운데는 날짜 경계에 걸릴 수 있어 x는 지정한 날짜 칸의 중심, y는 아이템의 세로 중심을 사용한다.
    private fun itemPressPosition(
        text: String,
        day: Int,
    ): Offset {
        val bounds =
            composeRule
                .onNodeWithText(text, useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot

        return Offset(x = dayCenter(day = day).x, y = bounds.center.y)
    }

    private fun rootWidth(): Float =
        composeRule
            .onRoot()
            .fetchSemanticsNode()
            .size.width
            .toFloat()

    private fun rootHeight(): Float =
        composeRule
            .onRoot()
            .fetchSemanticsNode()
            .size.height
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
            id = fixtureMonkey.giveMeOne<Uuid>(),
            title = title,
            color = fixtureMonkey.giveMeOne(),
            dateTime = MemoDateTime.AllDay(dateRange = start..endInclusive),
        )

    private fun june(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JUNE, day = day)

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    private fun august(day: Int): LocalDate = LocalDate(year = 2026, month = Month.AUGUST, day = day)

    private fun september(day: Int): LocalDate = LocalDate(year = 2026, month = Month.SEPTEMBER, day = day)

    private fun firstYearJanuary(day: Int): LocalDate = LocalDate(year = 1, month = Month.JANUARY, day = day)

    companion object {
        private const val MEETING_TITLE = "회의"
        private const val TRIP_TITLE = "여행"
        private const val CONSTITUTION_DAY_NAME = "제헌절"
        private const val BIRTHDAY_NAME = "홍길동"
        private const val BIRTHDAY_TEXT = "🎂 홍길동"
        private const val SUNNY_DESCRIPTION = "맑음"
        private const val WEATHER_TEMPERATURE_TEXT = "17.8°/28.2°"

        // 두 주에 걸친 메모는 원본 조각 2개와 같은 수의 고스트 아이템이 표시된다.
        private const val PIECE_COUNT = 2
        private const val GHOST_COUNT = 2
        private const val LONG_PRESS_MARGIN_MILLIS = 100L
        private const val WAIT_TIMEOUT_MILLIS = 10_000L
        private const val IDLE_WAIT_MILLIS = 5_000L
        private const val FRAME_MILLIS = 16L
        private const val SECONDARY_POINTER_ID = 1
        private const val BELOW_CALENDAR_DISTANCE = 200F
        private val MONTH_SWIPE_DISTANCE = 64.dp

        // 페이저 스와이프로 처리되면 달이 넘어가는 거리여야 달 이동이 일어나지 않음을 확인할 수 있다.
        private const val PAGE_CROSSING_RATIO = 0.7F
        private val JUNE_2026 = YearMonth(year = 2026, month = Month.JUNE)
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
        private val AUGUST_2026 = YearMonth(year = 2026, month = Month.AUGUST)
        private val SEPTEMBER_2026 = YearMonth(year = 2026, month = Month.SEPTEMBER)
        private val FIRST_YEAR_MONTH = YearMonth(year = 1, month = Month.JANUARY)
    }
}
