@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.routine.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.ADD_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.ADD_TITLE
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.CYCLE_WEEKLY
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.NAVIGATE_UP_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.SHOW_CALENDAR_DESCRIPTION
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 제품 entry의 배치 정보와 entry별 화면 상태를 그대로 쓰도록, 두 영역을 함께 표시하는 너비에서 제품 routineEntry로 NavDisplay를 구성한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class RoutineListDetailSelectionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineListDetailTestEnvironment(today = TODAY)
    private val robot = RoutineListDetailRobot(composeRule = composeRule, environment = environment)
    private val backStack: NavBackStack<ScreenNavKey>
        get() = environment.backStack

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineListDetailToday(TODAY)
    }

    @After
    fun tearDown() {
        stopKoin()
        releaseRoutineListDetailToday()
    }

    @Test
    fun `TC-411ef686-22d2-4e3e-842c-b27f4635f248 목록 보기에서 상세 영역에 루틴 상세가 놓이면 추가 수단을 제공한다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))
        robot.selectRoutine(routine)
        robot.assertDetailOnDetailPane(routine)

        robot.homeAddActionCount() shouldBe 1
        robot.clickHomeAdd()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id), RoutineAddNavKey())
    }

    @Test
    fun `TC-411ef686-22d2-4e3e-842c-b27f4635f248 달력 보기에서 상세 영역에 루틴 상세가 놓이면 추가 수단을 제공한다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))
        robot.selectRoutine(routine)
        robot.showCalendar()
        robot.assertDetailOnDetailPane(routine)

        robot.homeAddActionCount() shouldBe 1
        robot.clickHomeAdd()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id), RoutineAddNavKey())
        robot.assertCalendarView()
    }

    @Test
    fun `TC-5836128a-c6c3-4258-9b5e-b9f9c4a5802b 목록과 함께 표시된 루틴 상세에는 뒤로가기 수단이 없다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))

        robot.selectRoutine(routine)

        robot.assertDetailOnDetailPane(routine)
        composeRule.onNode(robot.card(routine.detail.title)).assertExists()
        composeRule.onAllNodesWithContentDescription(NAVIGATE_UP_DESCRIPTION).assertCountEquals(0)
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-5836128a-c6c3-4258-9b5e-b9f9c4a5802b 단독으로 표시된 루틴 상세에는 뒤로가기 수단이 있다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))

        robot.selectRoutine(routine)

        robot.assertDetailOnDetailPane(routine)
        composeRule.onNode(robot.card(routine.detail.title)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
    }

    @Test
    fun `TC-17b45a39-cad9-4d26-af1e-c3f176188942 선택한 루틴이 없을 때의 루틴 추가는 처음 상태다`() {
        robot.setNavDisplay(routineList = listOf(routine(title = FIRST_TITLE)))

        robot.assertAddOnDetailPane()
        robot.assertAddInitial(today = TODAY)
    }

    @Test
    fun `TC-94615b53-5a58-47c2-b57b-7498b33f0c11 목록에서 루틴을 선택하면 목록을 유지한 채 상세 영역이 그 상세로 바뀐다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))
        robot.assertAddOnDetailPane()

        robot.selectRoutine(routine)

        backStack.last() shouldBe RoutineDetailNavKey(id = routine.id)
        composeRule.onNode(robot.card(routine.detail.title)).assertExists()
        robot.assertDetailOnDetailPane(routine)
    }

    @Test
    fun `TC-d47754cd-0469-4277-be82-5af13ca64780 달력에서 앞 날짜의 기록을 선택하면 상세 영역이 그 루틴의 상세로 바뀐다`() {
        assertCalendarRecordSelectsDetail(amountText = EARLIER_AMOUNT_TEXT)
    }

    @Test
    fun `TC-d47754cd-0469-4277-be82-5af13ca64780 달력에서 뒤 날짜의 기록을 선택하면 상세 영역이 그 루틴의 상세로 바뀐다`() {
        assertCalendarRecordSelectsDetail(amountText = LATER_AMOUNT_TEXT)
    }

    @Test
    fun `TC-2b81800c-7443-4a5f-840f-ddbe383da697 목록에서 고른 뒤 달력에서 고르면 교체하고 뒤로가면 루틴 추가로 돌아간다`() {
        val first = routine(title = FIRST_TITLE)
        val second = routine(title = SECOND_TITLE)
        robot.setNavDisplay(
            routineList = listOf(first, second),
            recordList = listOf(routineListDetailRecord(routine = second, date = TODAY, amount = LATER_AMOUNT)),
        )

        robot.selectRoutine(first)
        robot.showCalendar()
        robot.selectCalendarRecord(title = SECOND_TITLE, amountText = LATER_AMOUNT_TEXT)

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = second.id))
        robot.assertDetailOnDetailPane(second)

        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        robot.assertAddOnDetailPane()
        robot.assertCalendarView()
    }

    @Test
    fun `TC-fb696550-5cf8-4c1a-97bb-00fb3b090fb6 상세가 놓인 동안 다른 루틴을 고르면 교체하고 뒤로가면 루틴 추가로 돌아간다`() {
        val first = routine(title = FIRST_TITLE)
        val second = routine(title = SECOND_TITLE)
        robot.setNavDisplay(routineList = listOf(first, second))

        robot.selectRoutine(first)
        robot.selectRoutine(second)

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = second.id))
        robot.assertDetailOnDetailPane(second)

        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        composeRule.onNode(robot.card(first.detail.title)).assertExists()
        composeRule.onNode(robot.card(second.detail.title)).assertExists()
        robot.assertAddOnDetailPane()
    }

    @Test
    fun `TC-20a6a6e3-e77a-4b01-bbae-e6de39d999c7 목록 보기의 상세 영역에서 뒤로가면 루틴 추가로 되돌아간다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))
        robot.selectRoutine(routine)

        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        robot.assertListView()
        composeRule.onNode(robot.card(routine.detail.title)).assertExists()
        robot.assertAddOnDetailPane()
        robot.assertAddInitial(today = TODAY)
    }

    @Test
    fun `TC-20a6a6e3-e77a-4b01-bbae-e6de39d999c7 달력 보기의 상세 영역에서 뒤로가면 루틴 추가로 되돌아간다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(
            routineList = listOf(routine),
            recordList = listOf(routineListDetailRecord(routine = routine, date = TODAY, amount = LATER_AMOUNT)),
        )
        robot.showCalendar()
        robot.selectCalendarRecord(title = FIRST_TITLE, amountText = LATER_AMOUNT_TEXT)
        robot.assertDetailOnDetailPane(routine)

        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        robot.assertCalendarView()
        composeRule.onNode(robot.calendarRecord(title = FIRST_TITLE, amountText = LATER_AMOUNT_TEXT)).assertExists()
        robot.assertAddOnDetailPane()
        robot.assertAddInitial(today = TODAY)
    }

    @Test
    fun `TC-96f56848-40da-4100-9e7b-ad07c751b203 상세에서 루틴을 삭제하면 목록에서 사라지고 상세 영역이 루틴 추가로 되돌아간다`() {
        val deleted = routine(title = FIRST_TITLE)
        val other = routine(title = SECOND_TITLE)
        robot.setNavDisplay(routineList = listOf(deleted, other))
        robot.selectRoutine(deleted)

        robot.clickDetailDelete()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        robot.waitUntilGone(robot.card(deleted.detail.title))
        composeRule.onNode(robot.card(other.detail.title)).assertExists()
        robot.assertAddOnDetailPane()
    }

    @Test
    fun `TC-ff826665-c13f-4602-9374-e01a487c2e01 목록 보기에서 상세가 놓인 동안 루틴 추가를 실행하면 처음 상태의 루틴 추가로 바뀐다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))
        fillPlaceholder()
        robot.selectRoutine(routine)
        robot.assertDetailOnDetailPane(routine)

        robot.clickHomeAdd()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id), RoutineAddNavKey())
        robot.assertListView()
        composeRule.onNode(robot.card(routine.detail.title)).assertExists()
        robot.assertAddOnDetailPane()
        robot.assertAddInitial(today = TODAY)
    }

    @Test
    fun `TC-ff826665-c13f-4602-9374-e01a487c2e01 달력 보기에서 상세가 놓인 동안 루틴 추가를 실행하면 처음 상태의 루틴 추가로 바뀐다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))
        fillPlaceholder()
        robot.selectRoutine(routine)
        robot.showCalendar()
        robot.assertDetailOnDetailPane(routine)

        robot.clickHomeAdd()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id), RoutineAddNavKey())
        robot.assertCalendarView()
        robot.assertAddOnDetailPane()
        robot.assertAddInitial(today = TODAY)
    }

    @Test
    fun `TC-1dd1074d-3091-447e-a8db-f74176efa750 상세 위에서 연 루틴 추가에서 뒤로가면 이전 루틴 상세로 돌아간다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))
        openAddOnDetail(routine)

        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id))
        composeRule.onNode(robot.card(routine.detail.title)).assertExists()
        robot.assertDetailOnDetailPane(routine)
    }

    @Test
    fun `TC-80127a90-7c34-49f6-b302-7eb992d4c38f 상세 위에서 연 루틴 추가가 놓인 동안 루틴을 고르면 추가를 걷어낸다`() {
        val first = routine(title = FIRST_TITLE)
        val second = routine(title = SECOND_TITLE)
        robot.setNavDisplay(routineList = listOf(first, second))
        openAddOnDetail(first)
        robot.titleInput().performTextInput(TYPED_ADD_TITLE)
        composeRule.waitForIdle()

        robot.selectRoutine(second)

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = second.id))
        robot.assertDetailOnDetailPane(second)

        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        robot.assertAddOnDetailPane()
        composeRule.onNodeWithText(TYPED_ADD_TITLE).assertDoesNotExist()
        robot.assertAddInitial(today = TODAY)
    }

    @Test
    fun `TC-80127a90-7c34-49f6-b302-7eb992d4c38f 좁은 창에서 연 루틴 추가가 창이 넓어져 놓인 동안 루틴을 고르면 추가를 걷어낸다`() {
        val routine = routine(title = FIRST_TITLE)
        environment.directiveOverride = PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 1)
        robot.setNavDisplay(routineList = listOf(routine))
        composeRule.onNodeWithContentDescription(ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(robot.card(routine.detail.title)).assertDoesNotExist()
        robot.titleInput().performTextInput(TYPED_ADD_TITLE)
        composeRule.waitForIdle()

        composeRule.runOnIdle { environment.directiveOverride = null }
        composeRule.waitForIdle()
        robot.selectRoutine(routine)

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id))
        robot.assertDetailOnDetailPane(routine)

        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        robot.assertAddOnDetailPane()
        composeRule.onNodeWithText(TYPED_ADD_TITLE).assertDoesNotExist()
        robot.assertAddInitial(today = TODAY)
    }

    @Test
    fun `TC-2863ca89-7cea-476a-b415-e61ac342f04c 달력 보기에서 루틴 추가가 놓인 상태로 뒤로가면 기본 목적지로 돌아간다`() {
        robot.setNavDisplay(routineList = listOf(routine(title = FIRST_TITLE)))
        robot.showCalendar()
        robot.assertAddOnDetailPane()

        robot.pressBack()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey)
        composeRule.onNodeWithText(ROUTINE_LIST_DETAIL_DEFAULT_CONTENT).assertExists()
        composeRule.onNodeWithContentDescription(SHOW_CALENDAR_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertCountEquals(0)
        composeRule.onNodeWithText(ADD_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-12247261-c406-4b46-bb2c-329f4b536428 다른 화면에서 진입한 루틴 상세는 목록 없이 단독으로 뒤로가기와 함께 표시한다`() {
        val routine = routine(title = FIRST_TITLE)
        setBackStack(RoutineListDetailDefaultNavKey, RoutineListDetailOtherNavKey, RoutineDetailNavKey(id = routine.id))
        robot.setNavDisplay(routineList = listOf(routine))

        assertStandalone(title = routine.detail.title)

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineListDetailOtherNavKey)
    }

    @Test
    fun `TC-12247261-c406-4b46-bb2c-329f4b536428 목록과 상세를 함께 보던 중 Spotlight로 진입한 루틴 상세는 단독으로 표시한다`() {
        val opened = routine(title = FIRST_TITLE)
        val spotlight = routine(title = SECOND_TITLE)
        robot.setNavDisplay(routineList = listOf(opened, spotlight))
        robot.selectRoutine(opened)

        // Spotlight로 여는 루틴 상세는 배치에 참여하지 않는 상세로 전환 이력 위에 쌓인다.
        composeRule.runOnIdle { backStack.add(RoutineDetailNavKey(id = spotlight.id, isStandalone = true)) }
        composeRule.waitForIdle()

        assertStandalone(title = spotlight.detail.title)

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = opened.id))
    }

    @Test
    fun `TC-12247261-c406-4b46-bb2c-329f4b536428 TagDetail 루틴 탭에서 진입한 루틴 추가는 목록 없이 단독으로 뒤로가기와 함께 표시한다`() {
        val tag = Arb.tag(title = ROUTINE_LIST_DETAIL_TAG_TITLE).next()
        environment.tagList = listOf(tag)
        setBackStack(RoutineListDetailDefaultNavKey, RoutineListDetailOtherNavKey, RoutineAddNavKey(initialTagId = tag.id))
        robot.setNavDisplay(routineList = listOf(routine(title = FIRST_TITLE)))

        composeRule.onNodeWithText(ADD_TITLE).assertExists()
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertCountEquals(0)
        composeRule.onNodeWithContentDescription(SHOW_CALENDAR_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineListDetailOtherNavKey)
    }

    @Test
    fun `TC-a2f32751-6f4b-4dfe-bdfa-a0c65b2cc024 목록 보기에서 고른 루틴 상세는 목록과 함께 상세 영역에 표시한다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine))

        robot.selectRoutine(routine)

        robot.assertDetailOnDetailPane(routine)
        val cardBounds = composeRule.onNode(robot.card(routine.detail.title)).fetchSemanticsNode().boundsInRoot
        (cardBounds.right <= robot.detailPaneLeft()) shouldBe true
    }

    @Test
    fun `TC-a2f32751-6f4b-4dfe-bdfa-a0c65b2cc024 달력 보기에서 고른 루틴 상세는 달력과 함께 상세 영역에 표시한다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(
            routineList = listOf(routine),
            recordList = listOf(routineListDetailRecord(routine = routine, date = TODAY, amount = LATER_AMOUNT)),
        )
        robot.showCalendar()

        robot.selectCalendarRecord(title = FIRST_TITLE, amountText = LATER_AMOUNT_TEXT)

        robot.assertDetailOnDetailPane(routine)
        val recordBounds = composeRule.onNode(robot.calendarRecord(title = FIRST_TITLE, amountText = LATER_AMOUNT_TEXT)).fetchSemanticsNode().boundsInRoot
        (recordBounds.right <= robot.detailPaneLeft()) shouldBe true
    }

    private fun assertCalendarRecordSelectsDetail(amountText: String) {
        val routine = routine(title = CALENDAR_TITLE)
        robot.setNavDisplay(
            routineList = listOf(routine),
            recordList =
                listOf(
                    routineListDetailRecord(routine = routine, date = TODAY.minus(1, DateTimeUnit.DAY), amount = EARLIER_AMOUNT),
                    routineListDetailRecord(routine = routine, date = TODAY, amount = LATER_AMOUNT),
                ),
        )
        robot.showCalendar()
        robot.assertAddOnDetailPane()

        robot.selectCalendarRecord(title = CALENDAR_TITLE, amountText = amountText)

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id))
        robot.assertCalendarView()
        composeRule.onNode(robot.calendarRecord(title = CALENDAR_TITLE, amountText = EARLIER_AMOUNT_TEXT)).assertExists()
        composeRule.onNode(robot.calendarRecord(title = CALENDAR_TITLE, amountText = LATER_AMOUNT_TEXT)).assertExists()
        robot.assertDetailOnDetailPane(routine)
    }

    private fun assertStandalone(title: String) {
        robot.assertDetailOnDetailPane(title = title)
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertCountEquals(0)
        composeRule.onNodeWithContentDescription(SHOW_CALENDAR_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun setBackStack(vararg key: ScreenNavKey) {
        composeRule.runOnIdle {
            backStack.clear()
            backStack.addAll(key)
        }
    }

    // 선택한 루틴이 없을 때의 루틴 추가에 제목과 목표량을 적고 주기를 매주로 바꾼다.
    private fun fillPlaceholder() {
        robot.titleInput().performTextInput(PLACEHOLDER_TITLE)
        robot.goalAmountInput().performTextInput(PLACEHOLDER_GOAL_AMOUNT)
        robot.selectCycle(option = CYCLE_WEEKLY)
        robot.titleInput().routineListDetailEditableText() shouldBe PLACEHOLDER_TITLE
        robot.goalAmountInput().routineListDetailEditableText() shouldBe PLACEHOLDER_GOAL_AMOUNT
        robot.assertCycleSelected(option = CYCLE_WEEKLY)
    }

    private fun openAddOnDetail(routine: Routine) {
        robot.selectRoutine(routine)
        robot.assertDetailOnDetailPane(routine)
        robot.clickHomeAdd()
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        const val FIRST_TITLE = "A-list-routine"
        const val SECOND_TITLE = "B-list-routine"
        const val CALENDAR_TITLE = "운동"
        const val EARLIER_AMOUNT = 10
        const val LATER_AMOUNT = 20
        const val EARLIER_AMOUNT_TEXT = "10회"
        const val LATER_AMOUNT_TEXT = "20회"
        const val PLACEHOLDER_TITLE = "placeholder-title"
        const val PLACEHOLDER_GOAL_AMOUNT = "3"
        const val TYPED_ADD_TITLE = "typed-add-title"

        fun routine(title: String): Routine = routineListDetailRoutine(title = title, today = TODAY)
    }
}
