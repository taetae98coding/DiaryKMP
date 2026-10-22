package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.text.TextLayoutResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.timetable.TIMETABLE_NOW_INDICATOR_TEST_TAG
import io.github.taetae98coding.diary.compose.timetable.rememberTimetableState
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.testing.memo.calendarMemo
import io.github.taetae98coding.diary.feature.calendar.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeDefaults
import io.github.taetae98coding.diary.feature.calendar.ui.home.mapState
import io.github.taetae98coding.diary.feature.calendar.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Clock

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarTimetableScreenDisplayTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var holidayFetchCount = 0

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-79408c77-76dd-4e92-a890-654a7e6d1e91 오늘에 해당하는 날짜는 주요 날짜로 강조된다`() {
        var onPrimary = Color.Unspecified

        setTimetableScreen(type = CalendarTimetableNavKey.Type.WEEK, clock = clockAt(TEN_O_CLOCK_ON_23)) {
            onPrimary = DiaryTheme.colorScheme.onPrimary
        }

        WEEK_DAY_LIST.forEach { day ->
            coloredTextCount(text = day.toString(), color = onPrimary) shouldBe if (day == 23) 1 else 0
        }
    }

    @Test
    fun `TC-824b4117-0ef6-4c54-bc17-3af23db5e94f 현재 시각 위치는 오늘의 열에만 표시된다`() {
        setTimetableScreen(type = CalendarTimetableNavKey.Type.WEEK, clock = clockAt(TEN_O_CLOCK_ON_23))

        composeRule.onAllNodesWithTag(TIMETABLE_NOW_INDICATOR_TEST_TAG, useUnmergedTree = true).assertCountEquals(1)
        assertNowIndicatorOnlyInColumnOf(day = 23)
        abs(minuteFrom(hourLabel = TEN_AM_LABEL, y = nowIndicatorCenterY())) shouldBeLessThan PLACEMENT_TOLERANCE_MINUTES
    }

    @Test
    fun `TC-0c0fbfc7-8c12-40e1-a33e-4527163145e9 1분이 지나면 현재 시각 위치가 그 시점의 시각으로 옮겨 간다`() {
        val clock = clockAt(TEN_O_CLOCK_ON_23)
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23), clock = clock)
        abs(minuteFrom(hourLabel = TEN_AM_LABEL, y = nowIndicatorCenterY())) shouldBeLessThan PLACEMENT_TOLERANCE_MINUTES

        composeRule.runOnIdle { every { clock.now() } returns TWO_O_CLOCK_PM_ON_23.toInstant(TimeZone.currentSystemDefault()) }
        composeRule.mainClock.advanceTimeBy(NOW_UPDATE_INTERVAL_MILLIS)
        composeRule.waitForIdle()

        abs(minuteFrom(hourLabel = TWO_PM_LABEL, y = nowIndicatorCenterY())) shouldBeLessThan PLACEMENT_TOLERANCE_MINUTES
    }

    @Test
    fun `TC-0c0fbfc7-8c12-40e1-a33e-4527163145e9 시간표를 보는 동안 자정이 지나면 현재 시각 위치와 날짜 강조가 바뀐 오늘을 따른다`() {
        val clock = clockAt(LAST_MINUTE_ON_23)
        var onPrimary = Color.Unspecified
        setTimetableScreen(type = CalendarTimetableNavKey.Type.WEEK, clock = clock) { onPrimary = DiaryTheme.colorScheme.onPrimary }
        assertNowIndicatorOnlyInColumnOf(day = 23)
        coloredTextCount(text = "23", color = onPrimary) shouldBe 1

        composeRule.runOnIdle { every { clock.now() } returns MIDNIGHT_ON_24.toInstant(TimeZone.currentSystemDefault()) }
        composeRule.mainClock.advanceTimeBy(NOW_UPDATE_INTERVAL_MILLIS)
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag(TIMETABLE_NOW_INDICATOR_TEST_TAG, useUnmergedTree = true).assertCountEquals(1)
        assertNowIndicatorOnlyInColumnOf(day = 24)
        abs(minuteFrom(hourLabel = ONE_AM_LABEL, y = nowIndicatorCenterY()) + MINUTES_PER_HOUR) shouldBeLessThan PLACEMENT_TOLERANCE_MINUTES
        coloredTextCount(text = "23", color = onPrimary) shouldBe 0
        coloredTextCount(text = "24", color = onPrimary) shouldBe 1
    }

    @Test
    fun `TC-0c0fbfc7-8c12-40e1-a33e-4527163145e9 다른 앱에서 돌아오면 현재 시각 위치가 그 시점의 시각으로 옮겨 간다`() {
        val clock = clockAt(TEN_O_CLOCK_ON_23)
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23), clock = clock, lifecycleOwner = lifecycleOwner)
        abs(minuteFrom(hourLabel = TEN_AM_LABEL, y = nowIndicatorCenterY())) shouldBeLessThan PLACEMENT_TOLERANCE_MINUTES

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { every { clock.now() } returns TWO_O_CLOCK_PM_ON_23.toInstant(TimeZone.currentSystemDefault()) }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        abs(minuteFrom(hourLabel = TWO_PM_LABEL, y = nowIndicatorCenterY())) shouldBeLessThan PLACEMENT_TOLERANCE_MINUTES
    }

    @Test
    fun `TC-0c0fbfc7-8c12-40e1-a33e-4527163145e9 다른 화면에서 돌아오면 현재 시각 위치가 그 시점의 시각으로 옮겨 간다`() {
        val clock = clockAt(TEN_O_CLOCK_ON_23)
        val isShown = mutableStateOf(true)
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 23), clock = clock, isShown = isShown)
        abs(minuteFrom(hourLabel = TEN_AM_LABEL, y = nowIndicatorCenterY())) shouldBeLessThan PLACEMENT_TOLERANCE_MINUTES

        composeRule.runOnIdle { isShown.value = false }
        composeRule.runOnIdle { every { clock.now() } returns TWO_O_CLOCK_PM_ON_23.toInstant(TimeZone.currentSystemDefault()) }
        composeRule.runOnIdle { isShown.value = true }
        composeRule.waitForIdle()

        abs(minuteFrom(hourLabel = TWO_PM_LABEL, y = nowIndicatorCenterY())) shouldBeLessThan PLACEMENT_TOLERANCE_MINUTES
    }

    @Test
    fun `공휴일 여부가 참인 공휴일의 날짜만 강조된다`() {
        var holidayColor = Color.Unspecified

        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.WEEK,
            date = september(day = 20),
            holidayList =
                listOf(
                    holiday(name = CHUSEOK, isHoliday = true, dateRange = september(day = 24)..september(day = 26)),
                    holiday(name = ANNIVERSARY, isHoliday = false, dateRange = september(day = 21)..september(day = 21)),
                ),
        ) { holidayColor = CalendarDefaults.colors().sundayAndHolidayColor }

        listOf(24, 25, 26).forEach { day -> coloredTextCount(text = day.toString(), color = holidayColor) shouldBe 1 }
        coloredTextCount(text = "21", color = holidayColor) shouldBe 0
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `공휴일 이름은 쉬는 날 여부에 따라 서로 다르게 표현된다`() {
        var holidayColor = Color.Unspecified
        var nonHolidayNameColor = Color.Unspecified

        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.WEEK,
            date = september(day = 20),
            holidayList =
                listOf(
                    holiday(name = CHUSEOK, isHoliday = true, dateRange = september(day = 24)..september(day = 24)),
                    holiday(name = ANNIVERSARY, isHoliday = false, dateRange = september(day = 21)..september(day = 21)),
                ),
        ) {
            holidayColor = CalendarDefaults.colors().sundayAndHolidayColor
            nonHolidayNameColor = CalendarHomeDefaults.nonHolidayNameColor()
        }

        holidayColor shouldNotBe nonHolidayNameColor
        itemContainsColor(text = CHUSEOK, color = holidayColor) shouldBe true
        itemContainsColor(text = ANNIVERSARY, color = nonHolidayNameColor) shouldBe true
        itemContainsColor(text = ANNIVERSARY, color = holidayColor) shouldBe false
    }

    @Test
    fun `TC-c6c91f24-dcf2-472d-87be-23ca46d8a72f 차지하는 시간대가 같은 시각 메모는 조회 결과 순서대로 놓인다`() {
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            memoList =
                listOf(
                    memo(title = MEETING_TITLE, dateTime = timed(day = 23, startHour = 9, endHour = 10)),
                    memo(title = TRIP_TITLE, dateTime = timed(day = 23, startHour = 9, endHour = 10)),
                ),
        )

        boundsOf(text = MEETING_TITLE).left shouldBeLessThan boundsOf(text = TRIP_TITLE).left
    }

    @Test
    fun `TC-810e9329-0378-42f1-b85a-4a9aa9257f31 차지하는 날짜가 같은 메모는 공휴일 이름보다 위쪽 줄에 놓인다`() {
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 24),
            memoList = listOf(memo(title = VACATION_TITLE, dateTime = MemoDateTime.AllDay(dateRange = september(day = 24)..september(day = 24)))),
            holidayList = listOf(holiday(name = CHUSEOK, isHoliday = true, dateRange = september(day = 24)..september(day = 24))),
        )

        boundsOf(text = VACATION_TITLE).top shouldBeLessThan boundsOf(text = CHUSEOK).top
    }

    @Test
    fun `TC-b41ad833-886a-4ee1-805c-4cad46fae7e8 시스템이 앱을 정리했다가 다시 만들어도 보던 형식과 날짜와 시간대가 유지된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            restorationTester = restorationTester,
        )
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        scrollToLastHour()

        // 시스템이 앱을 정리했다가 다시 만들면 저장해 둔 화면 상태로 화면을 다시 구성한다.
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Thu").assertIsDisplayed()
        composeRule.onNodeWithText("24").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertDoesNotExist()
        composeRule.onNodeWithText(LAST_HOUR_LABEL).assertIsDisplayed()
    }

    @Test
    fun `TC-a23f6d5c-b06e-42d7-b681-fec0479047bb 앱을 다시 실행해 시간표를 새로 열면 오전 8시부터 보인다`() {
        val launchCount = mutableStateOf(0)
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            launchCount = launchCount,
        )
        scrollToLastHour()
        composeRule.onNodeWithText(INITIAL_HOUR_LABEL).assertIsNotDisplayed()

        composeRule.runOnIdle { launchCount.value += 1 }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(INITIAL_HOUR_LABEL).assertIsDisplayed()
        composeRule.onNodeWithText(BEFORE_INITIAL_HOUR_LABEL).assertIsNotDisplayed()
    }

    @Test
    fun `TC-7488fa90-e540-41e9-904a-a0e5beb8532e 다른 앱에 다녀오는 것만으로는 공휴일을 다시 동기화하지 않는다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            lifecycleOwner = lifecycleOwner,
        )
        val fetchCountBeforeLeave = composeRule.runOnIdle { holidayFetchCount }
        (fetchCountBeforeLeave > 0) shouldBe true

        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP) }
        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START) }
        composeRule.runOnIdle { lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME) }
        composeRule.waitForIdle()

        composeRule.runOnIdle { holidayFetchCount } shouldBe fetchCountBeforeLeave
    }

    private fun setTimetableScreen(
        type: CalendarTimetableNavKey.Type,
        date: LocalDate? = null,
        clock: Clock = Clock.System,
        memoList: List<CalendarMemo> = emptyList(),
        holidayList: List<Holiday> = emptyList(),
        restorationTester: StateRestorationTester? = null,
        launchCount: MutableState<Int>? = null,
        lifecycleOwner: LifecycleOwner? = null,
        isShown: State<Boolean>? = null,
        onTheme: @Composable () -> Unit = {},
    ) {
        val memoViewModel =
            mockk<CalendarTimetableViewModel>().also { viewModel ->
                every { viewModel.select(any()) } returns Unit
                every { viewModel.uiState } returns MutableStateFlow(memoList).mapState { list -> CalendarTimetableMemoUiState(memoList = list) }
            }
        val holidayViewModel =
            mockk<CalendarTimetableHolidayViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } answers { holidayFetchCount += 1 }
                every { viewModel.uiState } returns
                    MutableStateFlow(holidayList).mapState { list -> CalendarTimetableHolidayUiState(holidayList = list) }
            }
        val setContent: (@Composable () -> Unit) -> Unit =
            restorationTester?.let { tester -> { content -> tester.setContent(content) } } ?: composeRule::setContent

        setContent {
            // launchCount가 바뀌면 저장된 상태 없이 시간표를 새로 구성해, 앱을 다시 실행하고 새로 연 것처럼 만든다.
            key(launchCount?.value) {
                val timetableState =
                    rememberTimetableState(
                        type = type.toTimetableType(),
                        initialDate = date ?: clock.todayIn(TimeZone.currentSystemDefault()),
                    )
                val state =
                    remember(timetableState) {
                        CalendarTimetableScaffoldState(timetableState = timetableState, snackbarHostState = SnackbarHostState(), clock = clock)
                    }

                CompositionLocalProvider(LocalLifecycleOwner provides (lifecycleOwner ?: LocalLifecycleOwner.current)) {
                    DiaryTheme {
                        onTheme()

                        // isShown이 거짓이 되면 시간표가 구성에서 빠지고, 다시 참이 되면 보던 상태 그대로 다시 구성되어 다른 화면에서 돌아온 것처럼 만든다.
                        if (isShown?.value != false) {
                            CalendarTimetableScreen(
                                navigateUp = {},
                                navigateToMemoDetail = {},
                                navigateToMemoAdd = {},
                                state = state,
                                memoViewModel = memoViewModel,
                                holidayViewModel = holidayViewModel,
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun scrollToLastHour() {
        composeRule.onNodeWithText(LAST_HOUR_LABEL).assertIsNotDisplayed()
        repeat(SCROLL_SWIPE_COUNT) { composeRule.onRoot().performTouchInput { swipeUp() } }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(LAST_HOUR_LABEL).assertIsDisplayed()
    }

    private fun boundsOf(text: String) = composeRule.onNodeWithText(text).fetchSemanticsNode().boundsInRoot

    // 현재 시각 표시는 그 날짜 열의 폭을 채우므로, 표시의 가로 범위 안에 그 날짜의 일 숫자만 들어 있는지로 어느 열에 놓였는지 판정한다.
    private fun assertNowIndicatorOnlyInColumnOf(day: Int) {
        val indicatorBounds = composeRule.onNodeWithTag(TIMETABLE_NOW_INDICATOR_TEST_TAG, useUnmergedTree = true).getUnclippedBoundsInRoot()

        WEEK_DAY_LIST.forEach { weekDay ->
            val dayBounds = composeRule.onNodeWithText(weekDay.toString()).getUnclippedBoundsInRoot()
            val dayCenterX = (dayBounds.left + dayBounds.right) / 2

            (dayCenterX > indicatorBounds.left && dayCenterX < indicatorBounds.right) shouldBe (weekDay == day)
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

    private fun clockAt(dateTime: LocalDateTime): Clock = mockk<Clock>().also { clock -> every { clock.now() } returns dateTime.toInstant(TimeZone.currentSystemDefault()) }

    // 날짜 숫자와 같은 글을 표시하는 다른 노드가 있어도, 같은 글을 표시하는 노드 중 기대 색상을 쓰는 노드 수로 판정한다.
    private fun coloredTextCount(
        text: String,
        color: Color,
    ): Int {
        val nodes = composeRule.onAllNodesWithText(text, useUnmergedTree = true)

        return List(nodes.fetchSemanticsNodes().size) { index -> nodes[index].textColor() }
            .count { textColor -> textColor == color }
    }

    private fun itemContainsColor(
        text: String,
        color: Color,
    ): Boolean {
        val bounds = boundsOf(text = text)
        val pixelMap = composeRule.onRoot().captureToImage().toPixelMap()

        return (bounds.left.roundToInt() until bounds.right.roundToInt()).any { x ->
            (bounds.top.roundToInt() until bounds.bottom.roundToInt()).any { y -> pixelMap[x, y] == color }
        }
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

    private fun holiday(
        name: String,
        isHoliday: Boolean,
        dateRange: LocalDateRange,
    ): Holiday = Holiday(name = name, isHoliday = isHoliday, dateRange = dateRange)

    private companion object {
        const val MINUTES_PER_HOUR = 60F
        const val PLACEMENT_TOLERANCE_MINUTES = 3F
        const val NOW_UPDATE_INTERVAL_MILLIS = 60_000L
        const val ONE_AM_LABEL = "1 AM"
        const val TEN_AM_LABEL = "10 AM"
        const val ELEVEN_AM_LABEL = "11 AM"
        const val TWO_PM_LABEL = "2 PM"
        val WEEK_DAY_LIST = (20..26).toList()
        val TEN_O_CLOCK_ON_23 = LocalDateTime(year = 2026, month = 9, day = 23, hour = 10, minute = 0)
        val TWO_O_CLOCK_PM_ON_23 = LocalDateTime(year = 2026, month = 9, day = 23, hour = 14, minute = 0)
        val LAST_MINUTE_ON_23 = LocalDateTime(year = 2026, month = 9, day = 23, hour = 23, minute = 59)
        val MIDNIGHT_ON_24 = LocalDateTime(year = 2026, month = 9, day = 24, hour = 0, minute = 0)
        const val SCROLL_SWIPE_COUNT = 5
        const val LAST_HOUR_LABEL = "11 PM"
        const val INITIAL_HOUR_LABEL = "8 AM"
        const val BEFORE_INITIAL_HOUR_LABEL = "7 AM"
        const val CHUSEOK = "추석"
        const val ANNIVERSARY = "기념일"
        const val MEETING_TITLE = "Meeting"
        const val TRIP_TITLE = "Trip"
        const val VACATION_TITLE = "Vacation"
    }
}
