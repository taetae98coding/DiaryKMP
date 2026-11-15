package io.github.taetae98coding.diary.feature.timetable.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.timetable.TIMETABLE_ALL_DAY_TEST_TAG
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.core.model.dday.CalendarDDayLabel
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.testing.contact.calendarContactBirthday
import io.github.taetae98coding.diary.core.testing.dday.calendarDDay
import io.github.taetae98coding.diary.core.testing.memo.calendarMemo
import io.github.taetae98coding.diary.domain.dday.usecase.GetCalendarDDayUseCase
import io.github.taetae98coding.diary.feature.timetable.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarTimetableScreenDDayTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-b52c5c92-a52e-47e1-9ff3-feba59727d99 디데이가 그 날짜의 종일 영역에 제목과 문구로 표시된다`() {
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.WEEK,
            date = september(day = 20),
            ddayListFlow =
                MutableStateFlow(
                    listOf(
                        dday(title = EXAM_TITLE, day = 24),
                        dday(title = US_TITLE, day = 25, label = CalendarDDayLabel.Days(days = 100)),
                    ),
                ),
        )

        assertInAllDayColumn(text = EXAM_TEXT, day = 24)
        assertInAllDayColumn(text = US_HUNDRED_DAYS_TEXT, day = 25)
    }

    @Test
    fun `TC-f663ffaf-88ad-4e5e-a1cb-e87d38eaa4dd 표시할 디데이가 없다가 준비되면 별도 조작 없이 표시된다`() {
        val ddayListFlow = MutableStateFlow(emptyList<CalendarDDay>())
        setTimetableScreen(type = CalendarTimetableNavKey.Type.DAY, date = september(day = 24), ddayListFlow = ddayListFlow)
        composeRule.onNodeWithText("24").assertIsDisplayed()
        composeRule.onAllNodesWithText(DDAY_TEXT_PREFIX, substring = true).assertCountEquals(0)

        composeRule.runOnIdle { ddayListFlow.value = listOf(dday(title = EXAM_TITLE, day = 24)) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(EXAM_TEXT).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `TC-f73a5325-fab1-467c-85fe-192db513bc6f 다른 날짜로 이동하면 이동한 날짜의 디데이를 보여 준다`() {
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 23),
            ddayListFlow = MutableStateFlow(listOf(dday(title = EXAM_TITLE, day = 23), dday(title = TOEIC_TITLE, day = 24))),
        )
        composeRule.onNodeWithText(EXAM_TEXT).performScrollTo().assertIsDisplayed()

        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TOEIC_TEXT).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(EXAM_TEXT).assertIsNotDisplayed()
    }

    @Test
    fun `TC-63fa5f86-1d37-48df-9f49-b612f8a2f6ee 디데이를 선택하면 그 디데이의 DDayDetail 화면으로 이동을 요청한다`() {
        val dday = dday(title = EXAM_TITLE, day = 24)
        val uriHandler = mockk<UriHandler>(relaxed = true)
        val ddayDetailList = mutableListOf<Uuid>()
        val memoAddList = mutableListOf<MemoDateTime>()
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 24),
            ddayListFlow = MutableStateFlow(listOf(dday)),
            navigateToDDayDetail = ddayDetailList::add,
            navigateToMemoAdd = memoAddList::add,
            uriHandler = uriHandler,
        )

        composeRule.onNodeWithText(EXAM_TEXT).performScrollTo().performClick()
        composeRule.waitForIdle()

        ddayDetailList shouldBe listOf(dday.ddayId)
        memoAddList shouldBe emptyList()
        verify(exactly = 0) { uriHandler.openUri(any()) }
    }

    @Test
    fun `TC-48116262-e2d8-40ca-8ce4-d21684f179b2 디데이를 길게 누르면 DDayDetail로 가지 않고 MemoAdd 이동도 요청하지 않는다`() {
        val ddayDetailList = mutableListOf<Uuid>()
        val memoAddList = mutableListOf<MemoDateTime>()
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 24),
            ddayListFlow = MutableStateFlow(listOf(dday(title = EXAM_TITLE, day = 24))),
            navigateToDDayDetail = ddayDetailList::add,
            navigateToMemoAdd = memoAddList::add,
        )

        longPressAndRelease(position = examCenter())

        ddayDetailList shouldBe emptyList()
        memoAddList shouldBe emptyList()
    }

    // 선택된 기간은 칸의 배경으로만 드러나므로, 누르기 전과 같은 화면인지로 남은 선택이 없음을 확인한다.
    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `디데이를 길게 눌렀다 떼면 누르기 전과 같은 화면이다`() {
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 24),
            ddayListFlow = MutableStateFlow(listOf(dday(title = EXAM_TITLE, day = 24))),
        )
        val position = examCenter()
        val pixelMapBeforePress = composeRule.onRoot().captureToImage().toPixelMap()

        longPressAndRelease(position = position)

        val pixelMapAfterRelease = composeRule.onRoot().captureToImage().toPixelMap()
        (pixelMapAfterRelease.width to pixelMapAfterRelease.height) shouldBe (pixelMapBeforePress.width to pixelMapBeforePress.height)
        val changedPixelCount =
            (0 until pixelMapBeforePress.width).sumOf { x ->
                (0 until pixelMapBeforePress.height).count { y -> pixelMapBeforePress[x, y] != pixelMapAfterRelease[x, y] }
            }
        changedPixelCount shouldBe 0
    }

    @Test
    fun `TC-fa4a8936-1f05-4b55-a804-e4a177a20814 디데이가 다른 날짜로 바뀌면 조작 없이 바뀐 날짜 열에 표시된다`() {
        val exam = dday(title = EXAM_TITLE, day = 24)
        val ddayListFlow = MutableStateFlow(listOf(exam))
        setTimetableScreen(type = CalendarTimetableNavKey.Type.WEEK, date = september(day = 20), ddayListFlow = ddayListFlow)
        assertInAllDayColumn(text = EXAM_TEXT, day = 24)

        composeRule.runOnIdle { ddayListFlow.value = listOf(exam.copy(date = september(day = 25))) }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText(EXAM_TEXT).assertCountEquals(1)
        assertInAllDayColumn(text = EXAM_TEXT, day = 25)
    }

    @Test
    fun `TC-fa4a8936-1f05-4b55-a804-e4a177a20814 디데이의 결과가 빠지면 조작 없이 보이지 않는다`() {
        val ddayListFlow = MutableStateFlow(listOf(dday(title = EXAM_TITLE, day = 24)))
        setTimetableScreen(type = CalendarTimetableNavKey.Type.WEEK, date = september(day = 20), ddayListFlow = ddayListFlow)
        composeRule.onNodeWithText(EXAM_TEXT).performScrollTo().assertIsDisplayed()

        composeRule.runOnIdle { ddayListFlow.value = emptyList() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(EXAM_TITLE, substring = true).assertDoesNotExist()
    }

    @Test
    fun `TC-bbf93916-0c01-4b20-8cd0-f508823c2b89 디데이 조회에 실패하면 디데이 없이 다른 표시를 그대로 보여 준다`() {
        val errorMessage = Arb.nonBlankString().next()
        val getCalendarDDayUseCase = mockk<GetCalendarDDayUseCase>()
        every { getCalendarDDayUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(errorMessage)))
        setTimetableScreen(
            type = CalendarTimetableNavKey.Type.DAY,
            date = september(day = 24),
            ddayViewModel = CalendarTimetableDDayViewModel(getCalendarDDayUseCase = getCalendarDDayUseCase),
            memoList =
                listOf(
                    Arb
                        .calendarMemo(
                            dateTime =
                                MemoDateTime.DateTime(
                                    start = LocalDateTime(year = 2026, month = 9, day = 24, hour = 10, minute = 0),
                                    endInclusive = LocalDateTime(year = 2026, month = 9, day = 24, hour = 11, minute = 0),
                                ),
                            title = MEETING_TITLE,
                        ).next(),
                ),
            holidayList = listOf(Holiday(name = CHUSEOK, isHoliday = true, dateRange = september(day = 24)..september(day = 24))),
            birthdayList = listOf(Arb.calendarContactBirthday().next().copy(name = BIRTHDAY_NAME, nameQualifier = "", date = september(day = 24))),
        )

        composeRule.onNodeWithText(MEETING_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(CHUSEOK).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(BIRTHDAY_TEXT).performScrollTo().assertIsDisplayed()
        composeRule.onAllNodesWithText(DDAY_TEXT_PREFIX, substring = true).assertCountEquals(0)
        composeRule.onNodeWithText(errorMessage, substring = true).assertDoesNotExist()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
    }

    private fun examCenter(): Offset =
        composeRule
            .onNodeWithText(EXAM_TEXT)
            .performScrollTo()
            .fetchSemanticsNode()
            .boundsInRoot.center

    private fun longPressAndRelease(position: Offset) {
        composeRule.onRoot().performTouchInput {
            down(position)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_MARGIN_MILLIS)
            moveBy(Offset.Zero)
            up()
        }
        composeRule.waitForIdle()
    }

    private fun setTimetableScreen(
        type: CalendarTimetableNavKey.Type,
        date: LocalDate,
        ddayListFlow: MutableStateFlow<List<CalendarDDay>> = MutableStateFlow(emptyList()),
        ddayViewModel: CalendarTimetableDDayViewModel = timetableDDayViewModel(ddayListFlow = ddayListFlow),
        memoList: List<CalendarMemo> = emptyList(),
        holidayList: List<Holiday> = emptyList(),
        birthdayList: List<CalendarContactBirthday> = emptyList(),
        navigateToDDayDetail: (Uuid) -> Unit = {},
        navigateToMemoAdd: (MemoDateTime) -> Unit = {},
        uriHandler: UriHandler? = null,
    ) {
        val memoViewModel =
            mockk<CalendarTimetableViewModel>().also { viewModel ->
                every { viewModel.select(any()) } returns Unit
                every { viewModel.uiState } returns MutableStateFlow(memoList).mapState { list -> CalendarTimetableMemoUiState(memoList = list) }
            }
        val holidayViewModel =
            mockk<CalendarTimetableHolidayViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.uiState } returns MutableStateFlow(holidayList).mapState { list -> CalendarTimetableHolidayUiState(holidayList = list) }
            }

        composeRule.setContent {
            val state = rememberCalendarTimetableScaffoldState(type = type, initialDate = date)

            CompositionLocalProvider(LocalUriHandler provides (uriHandler ?: LocalUriHandler.current)) {
                DiaryTheme {
                    CalendarTimetableScreen(
                        navigateUp = {},
                        navigateToMemoDetail = {},
                        navigateToDDayDetail = navigateToDDayDetail,
                        navigateToMemoAdd = navigateToMemoAdd,
                        navigateToContactDetail = {},
                        state = state,
                        memoViewModel = memoViewModel,
                        holidayViewModel = holidayViewModel,
                        weatherViewModel = timetableWeatherViewModel(),
                        ddayViewModel = ddayViewModel,
                        birthdayViewModel = timetableBirthdayViewModel(birthdayListFlow = MutableStateFlow(birthdayList)),
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun assertInAllDayColumn(
        text: String,
        day: Int,
    ) {
        val bounds =
            composeRule
                .onNodeWithText(text)
                .performScrollTo()
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .boundsInRoot
        val allDayBounds = composeRule.onNodeWithTag(TIMETABLE_ALL_DAY_TEST_TAG).fetchSemanticsNode().boundsInRoot
        val dayX =
            composeRule
                .onNodeWithText(day.toString())
                .fetchSemanticsNode()
                .boundsInRoot.center.x

        (bounds.top >= allDayBounds.top && bounds.bottom <= allDayBounds.bottom) shouldBe true
        abs(bounds.center.x - dayX) shouldBeLessThan COLUMN_ALIGNMENT_TOLERANCE_PX
    }

    private fun dday(
        title: String,
        day: Int,
        label: CalendarDDayLabel = CalendarDDayLabel.BaseDay,
    ): CalendarDDay = Arb.calendarDDay().next().copy(title = title, date = september(day = day), label = label)

    private fun september(day: Int): LocalDate = LocalDate(year = 2026, month = 9, day = day)

    private companion object {
        const val DDAY_TEXT_PREFIX = "🎉"
        const val EXAM_TITLE = "수능"
        const val EXAM_TEXT = "🎉 수능 D-Day"
        const val US_TITLE = "우리"
        const val US_HUNDRED_DAYS_TEXT = "🎉 우리 100일"
        const val TOEIC_TITLE = "토익"
        const val TOEIC_TEXT = "🎉 토익 D-Day"
        const val MEETING_TITLE = "Meeting"
        const val CHUSEOK = "추석"
        const val BIRTHDAY_NAME = "홍길동"
        const val BIRTHDAY_TEXT = "🎂 홍길동"
        const val LONG_PRESS_MARGIN_MILLIS = 100L
        const val COLUMN_ALIGNMENT_TOLERANCE_PX = 1F
    }
}
