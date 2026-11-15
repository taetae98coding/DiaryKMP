package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.record.ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 보기 전환은 제품 RoutineHomeScreen에서 실행하고, 달력에 놓이는 기록은 달력 ViewModel 대역이 돌려준다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class RoutineHomeViewTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineHomeQuickRecordTestEnvironment()

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineHomeQuickRecordToday(TODAY)
    }

    @After
    fun tearDown() {
        releaseRoutineHomeQuickRecordToday()
    }

    @Test
    fun `TC-2d2e308a-e22c-4c4d-a9e3-77ccf9a7b55e RoutineHome에 진입하면 목록 보기로 시작한다`() {
        setCalendarRecord()

        setRoutineHome(waterSummary())

        assertListView()
    }

    @Test
    fun `TC-62584258-fa41-4786-bd31-63fdb8274aff 목록 보기에는 루틴 목록을 두고 달력과 달 안내, 달 선택, 오늘로 이동은 두지 않는다`() {
        setRoutineHome(waterSummary())

        assertCommonActions()
        assertListView()
    }

    @Test
    fun `TC-62584258-fa41-4786-bd31-63fdb8274aff 달력 보기에는 달력과 달 안내, 달 선택, 오늘로 이동을 두고 루틴 목록은 두지 않는다`() {
        setRoutineHome(waterSummary())

        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION)

        assertCommonActions()
        assertCalendarView(title = JULY_TITLE)
        composeRule.onNodeWithText(JULY_TITLE).assertHasClickAction()
    }

    @Test
    fun `TC-f31da5cf-f257-4492-a505-648d6782ddd8 보기 전환을 실행하면 목록 보기와 달력 보기가 서로 바뀐다`() {
        setRoutineHome(waterSummary())
        assertListView()

        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION)

        assertCalendarView(title = JULY_TITLE)

        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_LIST_VIEW_DESCRIPTION)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE))
        assertListView()
    }

    @Test
    fun `TC-f9000c81-6ac1-4558-add1-53bbfc9b46fd 루틴이 표시되어 있어도 보기 전환을 실행할 수 있다`() {
        setRoutineHome(waterSummary())

        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION)

        assertCalendarView(title = JULY_TITLE)
    }

    @Test
    fun `TC-f9000c81-6ac1-4558-add1-53bbfc9b46fd 목록을 준비하는 중에도 보기 전환을 실행할 수 있다`() {
        environment.isPageLoading = true
        setRoutineHome()

        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION)

        assertCalendarView(title = JULY_TITLE)
    }

    @Test
    fun `TC-f9000c81-6ac1-4558-add1-53bbfc9b46fd 빈 상태 안내가 표시되어 있어도 보기 전환을 실행할 수 있다`() {
        setRoutineHome()
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(EMPTY_TITLE))

        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION)

        assertCalendarView(title = JULY_TITLE)
    }

    @Test
    fun `TC-f9000c81-6ac1-4558-add1-53bbfc9b46fd 태그 필터를 적용해 두어도 보기 전환을 실행할 수 있다`() {
        environment.filterTagList = listOf(Arb.tag().next())
        setRoutineHome(waterSummary())

        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION)

        assertCalendarView(title = JULY_TITLE)
    }

    @Test
    fun `TC-f9000c81-6ac1-4558-add1-53bbfc9b46fd 새로고침이 진행 중이어도 보기 전환을 실행할 수 있다`() {
        environment.syncUiState.value = SyncRefreshUiState(isRefreshing = true)
        setRoutineHome(waterSummary())

        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION)

        assertCalendarView(title = JULY_TITLE)
    }

    @Test
    fun `TC-e17b89a4-fb7e-47ef-9c84-149ceb9fa9ba 달력 보기에서 목록 보기에 다녀와도 보던 달을 그대로 본다`() {
        setRoutineHome(waterSummary())
        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION)
        repeat(2) {
            composeRule.onRoot().performTouchInput { swipeLeft() }
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithText(SEPTEMBER_TITLE).assertExists()

        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_LIST_VIEW_DESCRIPTION)
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE))
        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION)

        assertCalendarView(title = SEPTEMBER_TITLE)
        // 2026년 9월 달력은 8월 30일 일요일부터 10월 10일 토요일까지 보여 주므로 30일은 8월과 9월에 한 번씩 놓인다.
        composeRule.onAllNodesWithText("30").assertCountEquals(2)
    }

    @Test
    fun `TC-6d65a330-d99a-4e14-9c7e-425b1009346b 달력 보기에서는 빠른 기록을 제공하지 않는다`() {
        setCalendarRecord()
        setRoutineHome(waterSummary())

        toggleViewMode(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION)

        composeRule.onAllNodes(hasTestTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG)).assertCountEquals(0)
        composeRule.onNode(hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG)).performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        environment.recordParameterList.shouldBeEmpty()
    }

    private fun assertCommonActions() {
        composeRule.onNodeWithContentDescription(FILTER_DESCRIPTION).assertHasClickAction()
        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).assertHasClickAction()
    }

    private fun assertListView() {
        composeRule.onNode(routineHomeQuickRecordCard(WATER_TITLE)).assertExists()
        composeRule.onNodeWithContentDescription(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION).assertHasClickAction()
        composeRule.onNodeWithContentDescription(ROUTINE_HOME_QUICK_RECORD_LIST_VIEW_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(JULY_TITLE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(TODAY_DESCRIPTION).assertDoesNotExist()
        // 달력의 날짜는 일 숫자로 놓이므로, 어느 달에나 놓이는 일 숫자가 없으면 달력이 없다.
        composeRule.onAllNodesWithText(CALENDAR_DAY_TEXT).assertCountEquals(0)
    }

    private fun assertCalendarView(title: String) {
        composeRule.onNodeWithText(title).assertExists()
        composeRule.onNodeWithContentDescription(TODAY_DESCRIPTION).assertHasClickAction()
        composeRule.onNodeWithContentDescription(ROUTINE_HOME_QUICK_RECORD_LIST_VIEW_DESCRIPTION).assertHasClickAction()
        composeRule.onNodeWithContentDescription(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertCountEquals(0)
        (composeRule.onAllNodesWithText(CALENDAR_DAY_TEXT).fetchSemanticsNodes().size > 0) shouldBe true
    }

    private fun toggleViewMode(description: String) {
        composeRule.onNodeWithContentDescription(description).performClick()
        composeRule.waitForIdle()
    }

    private fun setCalendarRecord() {
        val record = CalendarRoutineRecord(routineId = waterSummary().routine.id, title = WATER_TITLE, color = 0L, date = TODAY, amount = 1_500L, unit = "ml")
        every { environment.calendarViewModel.uiState } returns MutableStateFlow(RoutineHomeCalendarUiState(recordList = listOf(record)))
    }

    private fun setRoutineHome(vararg summary: RoutineSummary) {
        environment.setSummaryList(summary.toList())
        composeRule.setContent { environment.Content() }
        composeRule.waitForIdle()
        if (!environment.isPageLoading) {
            summary.forEach { value -> composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(value.routine.detail.title)) }
        }
    }

    private fun waterSummary(): RoutineSummary =
        routineHomeQuickRecordSummary(
            title = WATER_TITLE,
            startDate = START_DATE,
            unit = "ml",
            goalAmount = 2_000,
            amountPerRecord = 250,
            cycleAmount = 1_500L,
        )

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val START_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 1)
        const val WATER_TITLE = "물 마시기"
        const val JULY_TITLE = "2026년 7월"
        const val SEPTEMBER_TITLE = "2026년 9월"
        const val FILTER_DESCRIPTION = "필터"
        const val ADD_DESCRIPTION = "루틴 추가"
        const val TODAY_DESCRIPTION = "오늘로 이동"
        const val EMPTY_TITLE = "아직 루틴이 없습니다"
        const val CALENDAR_DAY_TEXT = "28"
    }
}
