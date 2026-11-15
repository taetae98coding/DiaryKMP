package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.domain.routine.usecase.GetCalendarRoutineRecordUseCase
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.JULY_15_2026
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.JULY_2026
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.holiday
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.july
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.koreanTitle
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.record
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h2000dp")
class RoutineHomeCalendarRecordTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val navigatedDetailIdList = mutableListOf<Uuid>()
    private var navigateToAddCount = 0
    private var navigateToFilterCount = 0
    private lateinit var scaffoldState: RoutineHomeScaffoldState

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-e79569cd-abb5-4a3e-813c-7d1c0aa3de41 달력에는 루틴 기록과 공휴일만 표시한다`() {
        setRoutineHomeScreen(
            recordList = listOf(record(title = WORKOUT_TITLE, date = july(day = 10))),
            holidayList = listOf(holiday(name = CONSTITUTION_DAY_NAME, date = july(day = 17))),
        )

        // 화면이 받는 달력 내용은 루틴 기록과 공휴일뿐이므로, 날짜 숫자와 요일, 표시 중인 달 안내를 뺀 글이 그 둘뿐인지 본다.
        calendarItemTextList() shouldBe listOf(WORKOUT_ONCE_TEXT, CONSTITUTION_DAY_NAME)
        composeRule.routineHomeCalendarItemBoundsOn(text = WORKOUT_ONCE_TEXT, date = july(day = 10), yearMonth = JULY_2026) shouldHaveSize 1
        composeRule.routineHomeCalendarItemBoundsOn(text = CONSTITUTION_DAY_NAME, date = july(day = 17), yearMonth = JULY_2026) shouldHaveSize 1
    }

    @Test
    fun `TC-941ad955-4ce3-4467-95a1-711364585dff 기록이 있는 날짜에 루틴의 제목과 그 날짜 합계의 양 문구가 표시된다`() {
        val workoutId = Arb.kotlinUuid().next()
        setRoutineHomeScreen(
            recordList =
                listOf(
                    record(title = WATER_TITLE, date = july(day = 10), amount = 1500, unit = "ml"),
                    record(title = WORKOUT_TITLE, date = july(day = 10), amount = 1, unit = "회", routineId = workoutId),
                    record(title = WORKOUT_TITLE, date = july(day = 12), amount = 1, unit = "회", routineId = workoutId),
                    record(title = RUNNING_TITLE, date = july(day = 31), amount = 5000, unit = "m"),
                    record(title = STRETCHING_TITLE, date = july(day = 15), amount = 1, unit = ""),
                ),
        )

        composeRule.onAllNodesWithText(WATER_1500_TEXT).assertCountEquals(1)
        assertItemOn(text = WATER_1500_TEXT, date = july(day = 10))
        composeRule.onAllNodesWithText(WORKOUT_ONCE_TEXT).assertCountEquals(2)
        assertItemOn(text = WORKOUT_ONCE_TEXT, date = july(day = 10))
        assertItemOn(text = WORKOUT_ONCE_TEXT, date = july(day = 12))
        composeRule.onAllNodesWithText(RUNNING_5000_TEXT).assertCountEquals(1)
        assertItemOn(text = RUNNING_5000_TEXT, date = july(day = 31))
        composeRule.onAllNodesWithText(STRETCHING_ONE_TEXT).assertCountEquals(1)
        assertItemOn(text = STRETCHING_ONE_TEXT, date = july(day = 15))
        composeRule.onAllNodesWithTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG).assertCountEquals(5)
    }

    @Test
    fun `TC-9e6657c6-73af-4f3c-b2ce-a237714cf3fd 진행 상태나 날짜 범위와 관계없이 기록은 그 날짜에 표시된다`() {
        val dateByTitle =
            mapOf(
                "시작 전" to july(day = 22),
                "시작 날짜 변경" to july(day = 3),
                "진행 중" to july(day = 25),
            )
        setRoutineHomeScreen(recordList = dateByTitle.map { (title, date) -> record(title = title, date = date) })

        dateByTitle.forEach { (title, date) ->
            withClue(title) {
                composeRule.onAllNodesWithText("$title 1회").assertCountEquals(1)
                assertItemOn(text = "$title 1회", date = date)
            }
        }
    }

    @Test
    fun `TC-2e137351-7d14-401d-87b3-aeaae08bb0f5 표시할 기록이 준비되거나 바뀌면 조작 없이 달력에 반영된다`() {
        val water = record(title = WATER_TITLE, date = july(day = 10), amount = 1000, unit = "ml")
        val calendarUiState = MutableStateFlow(RoutineHomeCalendarUiState(recordList = listOf(water)))
        setRoutineHomeScreen(calendarUiState = calendarUiState)
        assertItemOn(text = WATER_1000_ML_ITEM_TEXT, date = july(day = 10))

        showRecordList(calendarUiState, water.copy(amount = 1500))
        composeRule.onAllNodesWithText(WATER_1000_ML_ITEM_TEXT).assertCountEquals(0)
        assertItemOn(text = WATER_1500_TEXT, date = july(day = 10))

        showRecordList(calendarUiState, water, water.copy(date = july(day = 12), amount = 250))
        assertItemOn(text = WATER_1000_ML_ITEM_TEXT, date = july(day = 10))
        assertItemOn(text = "물 마시기 250ml", date = july(day = 12))

        showRecordList(calendarUiState)
        composeRule.onAllNodesWithTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG).assertCountEquals(0)

        showRecordList(calendarUiState, water.copy(title = "수분 보충"))
        composeRule.onAllNodesWithText(WATER_TITLE, substring = true).assertCountEquals(0)
        assertItemOn(text = "수분 보충 1000ml", date = july(day = 10))

        showRecordList(calendarUiState, water.copy(unit = "회"))
        assertItemOn(text = "물 마시기 1000회", date = july(day = 10))

        showRecordList(calendarUiState, water.copy(unit = ""))
        composeRule.onAllNodesWithText("물 마시기 1000회").assertCountEquals(0)
        assertItemOn(text = "물 마시기 1000", date = july(day = 10))

        showRecordList(calendarUiState, water, water.copy(date = july(day = 20), amount = 300))
        assertItemOn(text = "물 마시기 300ml", date = july(day = 20))
    }

    @Test
    fun `TC-33bb3eab-49e9-4407-94ef-de35b458b516 다른 달로 이동하면 이동한 달의 기록을 표시한다`() {
        val september = YearMonth(year = 2026, month = Month.SEPTEMBER)
        val recordList =
            listOf(
                record(title = WORKOUT_TITLE, date = july(day = 10)),
                record(title = WORKOUT_TITLE, date = LocalDate(year = 2026, month = Month.SEPTEMBER, day = 10)),
            )
        val getCalendarRoutineRecordUseCase = mockk<GetCalendarRoutineRecordUseCase>()
        every { getCalendarRoutineRecordUseCase(parameter = any()) } answers {
            flowOf(Result.success(recordList.filter { record -> record.date in firstArg<LocalDateRange>() }))
        }
        setRoutineHomeScreen(calendarViewModel = RoutineHomeCalendarViewModel(getCalendarRoutineRecordUseCase = getCalendarRoutineRecordUseCase))
        assertItemOn(text = WORKOUT_ONCE_TEXT, date = july(day = 10))

        repeat(2) { composeRule.routineHomeCalendarMoveToNextMonth() }

        composeRule.onNodeWithText(koreanTitle(september)).assertIsDisplayed()
        composeRule.onAllNodesWithText(WORKOUT_ONCE_TEXT).assertCountEquals(1)
        composeRule.routineHomeCalendarItemBoundsOn(text = WORKOUT_ONCE_TEXT, date = LocalDate(year = 2026, month = Month.SEPTEMBER, day = 10), yearMonth = september) shouldHaveSize 1
    }

    @Test
    fun `TC-0dc1de98-26a9-401d-b6e7-2336d684a226 달력의 루틴 기록을 선택하면 어느 날짜의 아이템이든 그 루틴의 상세로 한 번 이동한다`() {
        val workoutId = Arb.kotlinUuid().next()
        setRoutineHomeScreen(
            recordList =
                listOf(
                    record(title = WORKOUT_TITLE, date = july(day = 10), routineId = workoutId),
                    record(title = WORKOUT_TITLE, date = july(day = 12), routineId = workoutId),
                    record(title = WATER_TITLE, date = july(day = 10), amount = 1500, unit = "ml"),
                ),
        )

        listOf(july(day = 10), july(day = 12)).forEach { date ->
            navigatedDetailIdList.clear()
            val bounds = composeRule.routineHomeCalendarItemBoundsOn(text = WORKOUT_ONCE_TEXT, date = date, yearMonth = JULY_2026).single()

            composeRule.onRoot().performTouchInput { click(position = bounds.center) }
            composeRule.waitForIdle()

            withClue(date) { navigatedDetailIdList shouldBe listOf(workoutId) }
        }
    }

    @Test
    fun `TC-69fff189-b21a-4fac-bf57-e42550960953 루틴 기록을 길게 눌러도 아무 동작도 하지 않는다`() {
        setRoutineHomeScreen(recordList = listOf(record(title = WORKOUT_TITLE, date = july(day = 10))))

        composeRule.onNodeWithText(WORKOUT_ONCE_TEXT).performTouchInput { longClick() }
        composeRule.waitForIdle()

        assertNothingOpened()
        composeRule.onAllNodesWithText(WORKOUT_ONCE_TEXT).assertCountEquals(1)
        assertItemOn(text = WORKOUT_ONCE_TEXT, date = july(day = 10))
    }

    @Test
    fun `TC-69fff189-b21a-4fac-bf57-e42550960953 길게 누르기 인식 시간 직후에 떼어도 루틴 기록 선택을 실행하지 않는다`() {
        setRoutineHomeScreen(recordList = listOf(record(title = WORKOUT_TITLE, date = july(day = 10))))

        composeRule.onNodeWithText(WORKOUT_ONCE_TEXT).performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + AFTER_LONG_PRESS_MILLIS)
            up()
        }
        composeRule.waitForIdle()

        assertNothingOpened()
    }

    @Test
    fun `길게 누르기 인식 시간 전에 떼면 루틴 기록 선택을 실행한다`() {
        val record = record(title = WORKOUT_TITLE, date = july(day = 10))
        setRoutineHomeScreen(recordList = listOf(record))

        composeRule.onNodeWithText(WORKOUT_ONCE_TEXT).performTouchInput {
            down(center)
            advanceEventTime(viewConfiguration.longPressTimeoutMillis - BEFORE_LONG_PRESS_MILLIS)
            up()
        }
        composeRule.waitForIdle()

        navigatedDetailIdList shouldBe listOf(record.routineId)
    }

    @Test
    fun `길게 누른 뒤에도 루틴 기록을 누르면 상세로 이동한다`() {
        val record = record(title = WORKOUT_TITLE, date = july(day = 10))
        setRoutineHomeScreen(recordList = listOf(record))
        composeRule.onNodeWithText(WORKOUT_ONCE_TEXT).performTouchInput { longClick() }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WORKOUT_ONCE_TEXT).performClick()
        composeRule.waitForIdle()

        navigatedDetailIdList shouldBe listOf(record.routineId)
    }

    @Test
    fun `TC-7765452e-7f65-48ef-9be6-e1a134da425a 달력의 날짜나 주를 누르거나 길게 눌러도 아무 일도 일어나지 않는다`() {
        setRoutineHomeScreen()
        val dateTextList = composeRule.routineHomeCalendarDateTexts()
        // 6월 28일부터 놓인 칸 가운데 7월 14일과 그 아랫주의 7월 21일을 쓴다.
        val dateBounds = composeRule.routineHomeCalendarDateNodes()[JULY_14_INDEX].boundsInRoot
        val nextWeekDateBounds = composeRule.routineHomeCalendarDateNodes()[JULY_21_INDEX].boundsInRoot
        val weekPosition = Offset(x = dateBounds.center.x, y = (dateBounds.bottom + nextWeekDateBounds.top) / 2)
        val gestureByName =
            mapOf(
                "날짜를 누른다" to { composeRule.onRoot().performTouchInput { click(position = dateBounds.center) } },
                "주를 누른다" to { composeRule.onRoot().performTouchInput { click(position = weekPosition) } },
                "날짜를 길게 누른다" to { composeRule.onRoot().performTouchInput { longClick(position = dateBounds.center) } },
                "날짜를 길게 누른 채 다른 날짜로 끈다" to {
                    composeRule.onRoot().performTouchInput {
                        down(nextWeekDateBounds.center)
                        advanceEventTime(durationMillis = viewConfiguration.longPressTimeoutMillis * 2)
                        moveTo(dateBounds.center)
                        up()
                    }
                },
            )

        gestureByName.forEach { (name, gesture) ->
            gesture()
            composeRule.waitForIdle()

            withClue(name) {
                assertNothingOpened()
                composeRule.runOnIdle { scaffoldState.calendarState.selectState.dateRange shouldBe null }
                composeRule.onNodeWithText(koreanTitle(JULY_2026)).assertIsDisplayed()
                composeRule.routineHomeCalendarDateTexts() shouldBe dateTextList
            }
        }
    }

    @Test
    fun `TC-d0483b27-35dc-49ae-a8e1-2fd510e42118 루틴 기록을 조회하지 못하면 기록 없이 달력을 표시하고 알리지 않는다`() {
        val errorMessage = Arb.nonBlankString().next()
        val getCalendarRoutineRecordUseCase = mockk<GetCalendarRoutineRecordUseCase>()
        every { getCalendarRoutineRecordUseCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(errorMessage)))
        setRoutineHomeScreen(
            calendarViewModel = RoutineHomeCalendarViewModel(getCalendarRoutineRecordUseCase = getCalendarRoutineRecordUseCase),
            holidayList =
                listOf(
                    holiday(name = CONSTITUTION_DAY_NAME, date = july(day = 17)),
                    holiday(name = LIBERATION_DAY_NAME, date = LocalDate(year = 2026, month = Month.AUGUST, day = 15)),
                ),
        )

        composeRule.onNodeWithText(koreanTitle(JULY_2026)).assertIsDisplayed()
        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).assertIsDisplayed()
        assertNoRecordAndNoNotice(errorMessage = errorMessage)

        composeRule.routineHomeCalendarMoveToNextMonth()

        composeRule.onNodeWithText(koreanTitle(YearMonth(year = 2026, month = Month.AUGUST))).assertIsDisplayed()
        composeRule.onNodeWithText(LIBERATION_DAY_NAME).assertIsDisplayed()
        assertNoRecordAndNoNotice(errorMessage = errorMessage)
    }

    @Test
    fun `TC-b18b2091-6103-4ed5-930a-c428ef600a49 같은 날짜의 루틴 기록과 공휴일 이름은 루틴 기록, 공휴일 이름 순으로 놓인다`() {
        setRoutineHomeScreen(
            recordList = listOf(record(title = WORKOUT_TITLE, date = july(day = 17))),
            holidayList = listOf(holiday(name = CONSTITUTION_DAY_NAME, date = july(day = 17))),
        )

        val recordBounds = composeRule.routineHomeCalendarItemBoundsOn(text = WORKOUT_ONCE_TEXT, date = july(day = 17), yearMonth = JULY_2026).single()
        val holidayBounds = composeRule.routineHomeCalendarItemBoundsOn(text = CONSTITUTION_DAY_NAME, date = july(day = 17), yearMonth = JULY_2026).single()

        (recordBounds.bottom <= holidayBounds.top) shouldBe true
    }

    private fun showRecordList(
        calendarUiState: MutableStateFlow<RoutineHomeCalendarUiState>,
        vararg record: CalendarRoutineRecord,
    ) {
        composeRule.runOnIdle { calendarUiState.value = RoutineHomeCalendarUiState(recordList = record.toList()) }
        composeRule.waitForIdle()
    }

    private fun assertItemOn(
        text: String,
        date: LocalDate,
    ) {
        withClue("$text on $date") {
            composeRule.routineHomeCalendarItemBoundsOn(text = text, date = date, yearMonth = JULY_2026) shouldHaveSize 1
        }
    }

    private fun assertNothingOpened() {
        navigatedDetailIdList.shouldBeEmpty()
        navigateToAddCount shouldBe 0
        navigateToFilterCount shouldBe 0
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        composeRule.onAllNodes(isPopup()).assertCountEquals(0)
    }

    private fun assertNoRecordAndNoNotice(errorMessage: String) {
        composeRule.onAllNodesWithTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG).assertCountEquals(0)
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        composeRule.onAllNodes(isPopup()).assertCountEquals(0)
        composeRule.onAllNodes(hasText(text = errorMessage, substring = true)).assertCountEquals(0)
        scaffoldState.snackbarHostState.currentSnackbarData shouldBe null
    }

    private fun calendarItemTextList(): List<String> =
        composeRule
            .onAllNodes(hasText(text = "", substring = true))
            .fetchSemanticsNodes()
            .map { node ->
                node.config
                    .getOrNull(SemanticsProperties.Text)
                    .orEmpty()
                    .joinToString(separator = "") { text -> text.text }
            }.filterNot { text -> text.all(Char::isDigit) || text in DAY_OF_WEEK_TITLE_LIST || text == koreanTitle(JULY_2026) }

    private fun setRoutineHomeScreen(
        recordList: List<CalendarRoutineRecord> = emptyList(),
        calendarUiState: MutableStateFlow<RoutineHomeCalendarUiState> = MutableStateFlow(RoutineHomeCalendarUiState(recordList = recordList)),
        calendarViewModel: RoutineHomeCalendarViewModel = RoutineHomeCalendarTestFixture.calendarViewModel(uiState = calendarUiState),
        holidayList: List<Holiday> = emptyList(),
    ) {
        val holidayViewModel = RoutineHomeCalendarTestFixture.holidayViewModel(uiState = MutableStateFlow(RoutineHomeHolidayUiState(holidayList = holidayList)))

        composeRule.setContent {
            RoutineHomeCalendarTestScreen(
                today = JULY_15_2026,
                calendarViewModel = calendarViewModel,
                holidayViewModel = holidayViewModel,
                navigateToAdd = { navigateToAddCount += 1 },
                navigateToDetail = navigatedDetailIdList::add,
                navigateToFilter = { navigateToFilterCount += 1 },
                onState = { state -> scaffoldState = state },
            )
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val WORKOUT_TITLE = "운동"
        const val WORKOUT_ONCE_TEXT = "운동 1회"
        const val WATER_TITLE = "물 마시기"
        const val WATER_1500_TEXT = "물 마시기 1500ml"
        const val RUNNING_TITLE = "달리기"
        const val RUNNING_5000_TEXT = "달리기 5000m"
        const val STRETCHING_TITLE = "스트레칭"
        const val STRETCHING_ONE_TEXT = "스트레칭 1"
        const val CONSTITUTION_DAY_NAME = "제헌절"
        const val LIBERATION_DAY_NAME = "광복절"
        const val JULY_14_INDEX = 16
        const val JULY_21_INDEX = 23
        const val AFTER_LONG_PRESS_MILLIS = 1L
        const val BEFORE_LONG_PRESS_MILLIS = 50L
        val DAY_OF_WEEK_TITLE_LIST = listOf("일", "월", "화", "수", "목", "금", "토")
    }
}

private const val WATER_1000_ML_ITEM_TEXT = "물 마시기 1000ml"
