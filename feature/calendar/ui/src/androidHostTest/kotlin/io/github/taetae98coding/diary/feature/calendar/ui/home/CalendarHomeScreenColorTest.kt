package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.roundToInt
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScreenColorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `공휴일 여부가 참인 공휴일의 날짜만 강조된다`() {
        var holidayColor = Color.Unspecified
        setCalendarHomeScreen(
            holidayList =
                listOf(
                    holiday(name = CONSTITUTION_DAY_NAME, isHoliday = true, start = july(day = 17)),
                    holiday(name = LONG_HOLIDAY_NAME, isHoliday = true, start = july(day = 22), endInclusive = july(day = 23)),
                    holiday(name = MIDSUMMER_DAY_NAME, isHoliday = false, start = july(day = 20)),
                ),
        ) { holidayColor = CalendarDefaults.colors().sundayAndHolidayColor }

        listOf(17, 22, 23).forEach { day -> holidayColoredDateCount(day = day, holidayColor = holidayColor) shouldBe 1 }
        holidayColoredDateCount(day = 20, holidayColor = holidayColor) shouldBe 0
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `공휴일 이름은 쉬는 날 여부에 따라 서로 다르게 표현된다`() {
        var holidayColor = Color.Unspecified
        var nonHolidayNameColor = Color.Unspecified
        setCalendarHomeScreen(
            holidayList =
                listOf(
                    holiday(name = CONSTITUTION_DAY_NAME, isHoliday = true, start = july(day = 17)),
                    holiday(name = MIDSUMMER_DAY_NAME, isHoliday = false, start = july(day = 20)),
                ),
        ) {
            holidayColor = CalendarDefaults.colors().sundayAndHolidayColor
            nonHolidayNameColor = CalendarHomeDefaults.nonHolidayNameColor()
        }

        holidayColor shouldNotBe nonHolidayNameColor
        itemContainsColor(text = CONSTITUTION_DAY_NAME, color = holidayColor) shouldBe true
        itemContainsColor(text = MIDSUMMER_DAY_NAME, color = nonHolidayNameColor) shouldBe true
        itemContainsColor(text = MIDSUMMER_DAY_NAME, color = holidayColor) shouldBe false
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `메모는 정해진 표시 색상으로 드러난다`() {
        setCalendarHomeScreen(
            memoListFlow =
                MutableStateFlow(
                    listOf(
                        singleDayMemo(title = MEETING_TITLE, color = RED, date = july(day = 7)),
                        allDayMemo(title = TRIP_TITLE, color = BLUE, start = july(day = 14), endInclusive = july(day = 16)),
                    ),
                ),
        )

        itemContainsColor(text = MEETING_TITLE, color = Color(color = RED.toInt())) shouldBe true
        itemContainsColor(text = TRIP_TITLE, color = Color(color = BLUE.toInt())) shouldBe true
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `하루 일정과 그 밖의 메모는 서로 다르게 표현된다`() {
        setCalendarHomeScreen(
            memoListFlow =
                MutableStateFlow(
                    listOf(
                        singleDayMemo(title = MEETING_TITLE, color = RED, date = july(day = 7)),
                        allDayMemo(title = TRIP_TITLE, color = RED, start = july(day = 14), endInclusive = july(day = 16)),
                    ),
                ),
        )

        val memoColor = Color(color = RED.toInt())
        itemContainsColor(text = MEETING_TITLE, color = memoColor) shouldBe true
        trailingHalfContainsColor(text = MEETING_TITLE, color = memoColor) shouldBe false
        trailingHalfContainsColor(text = TRIP_TITLE, color = memoColor) shouldBe true
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `메모의 기간이 바뀌어 갈래가 달라지면 조작 없이 표현이 갱신된다`() {
        val memo = allDayMemo(title = TRIP_TITLE, color = RED, start = july(day = 14), endInclusive = july(day = 14))
        val memoListFlow = MutableStateFlow(listOf(memo))
        setCalendarHomeScreen(memoListFlow = memoListFlow)
        val memoColor = Color(color = RED.toInt())
        trailingHalfContainsColor(text = TRIP_TITLE, color = memoColor) shouldBe true

        composeRule.runOnIdle { memoListFlow.value = listOf(memo.copy(dateTime = singleDayDateTime(date = july(day = 14)))) }
        composeRule.waitForIdle()

        itemContainsColor(text = TRIP_TITLE, color = memoColor) shouldBe true
        trailingHalfContainsColor(text = TRIP_TITLE, color = memoColor) shouldBe false
    }

    private fun setCalendarHomeScreen(
        holidayList: List<Holiday> = emptyList(),
        memoListFlow: StateFlow<List<CalendarMemo>> = MutableStateFlow(emptyList()),
        onTheme: @Composable () -> Unit = {},
    ) {
        val memoViewModel =
            mockk<CalendarHomeMemoViewModel>().also { viewModel ->
                every { viewModel.select(any()) } returns Unit
                every { viewModel.uiState } returns memoListFlow.mapState { memoList -> CalendarHomeMemoUiState(memoList = memoList) }
                every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
            }

        composeRule.setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = JULY_2026),
                )

            DiaryTheme {
                onTheme()

                CalendarHomeScreen(
                    navigateToMemoDetail = {},
                    navigateToMemoAdd = {},
                    navigateToContactDetail = {},
                    birthdayViewModel = birthdayViewModel(),
                    navigateToFilter = {},
                    navigateToTimetable = {},
                    state = state,
                    holidayViewModel = holidayViewModel(holidayListFlow = MutableStateFlow(holidayList)),
                    memoViewModel = memoViewModel,
                    weatherViewModel = weatherViewModel(),
                    syncViewModel = syncViewModel(),
                    permissionManager = rememberPermissionManager(),
                )
            }
        }
        composeRule.waitForIdle()
    }

    // 오늘 버튼도 오늘의 일 숫자를 표시하므로, 같은 숫자를 표시하는 노드 중 공휴일 강조 색상을 쓰는 노드 수로 판정한다.
    private fun holidayColoredDateCount(
        day: Int,
        holidayColor: Color,
    ): Int {
        val nodes = composeRule.onAllNodesWithText(day.toString(), useUnmergedTree = true)

        return List(nodes.fetchSemanticsNodes().size) { index -> nodes[index].textColor() }
            .count { color -> color == holidayColor }
    }

    private fun itemContainsColor(
        text: String,
        color: Color,
    ): Boolean {
        val bounds = composeRule.onNodeWithText(text).fetchSemanticsNode().boundsInRoot

        return capture().containsColor(bounds = bounds, color = color)
    }

    // 제목은 아이템 가운데에 놓이므로, 세로 가운데 줄의 뒤쪽 절반에 메모 색상이 채워져 있는지로 배경이 채워졌는지 판정한다.
    private fun trailingHalfContainsColor(
        text: String,
        color: Color,
    ): Boolean {
        val bounds = composeRule.onNodeWithText(text).fetchSemanticsNode().boundsInRoot
        val y = bounds.center.y.roundToInt()
        val pixelMap = capture()

        return (bounds.center.x.roundToInt() until bounds.right.roundToInt() - 1).any { x -> pixelMap[x, y] == color }
    }

    private fun capture(): PixelMap = composeRule.onRoot().captureToImage().toPixelMap()

    private fun PixelMap.containsColor(
        bounds: Rect,
        color: Color,
    ): Boolean =
        (bounds.left.roundToInt() until bounds.right.roundToInt()).any { x ->
            (bounds.top.roundToInt() until bounds.bottom.roundToInt()).any { y -> this[x, y] == color }
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

    private fun holiday(
        name: String,
        isHoliday: Boolean,
        start: LocalDate,
        endInclusive: LocalDate = start,
    ): Holiday =
        Holiday(
            name = name,
            isHoliday = isHoliday,
            dateRange = start..endInclusive,
        )

    private fun allDayMemo(
        title: String,
        color: Long,
        start: LocalDate,
        endInclusive: LocalDate,
    ): CalendarMemo =
        CalendarMemo(
            id = fixtureMonkey.giveMeOne<Uuid>(),
            title = title,
            color = color,
            dateTime = MemoDateTime.AllDay(dateRange = start..endInclusive),
        )

    private fun singleDayMemo(
        title: String,
        color: Long,
        date: LocalDate,
    ): CalendarMemo =
        CalendarMemo(
            id = fixtureMonkey.giveMeOne<Uuid>(),
            title = title,
            color = color,
            dateTime = singleDayDateTime(date = date),
        )

    private fun singleDayDateTime(date: LocalDate): MemoDateTime =
        MemoDateTime.DateTime(
            start = LocalDateTime(date = date, time = LocalTime(hour = 9, minute = 0)),
            endInclusive = LocalDateTime(date = date, time = LocalTime(hour = 10, minute = 0)),
        )

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    companion object {
        private const val CONSTITUTION_DAY_NAME = "제헌절"
        private const val MIDSUMMER_DAY_NAME = "초복"
        private const val LONG_HOLIDAY_NAME = "연휴"
        private const val MEETING_TITLE = "회의"
        private const val TRIP_TITLE = "여행"
        private const val RED: Long = 0xFFE53935
        private const val BLUE: Long = 0xFF1E88E5
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
    }
}
