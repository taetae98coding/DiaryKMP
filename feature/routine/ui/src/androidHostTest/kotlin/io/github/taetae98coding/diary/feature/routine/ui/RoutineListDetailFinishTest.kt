package io.github.taetae98coding.diary.feature.routine.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.api.isRoutineListDetailPane
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.CYCLE_WEEKLY
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.FINISHED_LIST_TITLE
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.FINISH_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.RECORD_UNDO_ACTION
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.RESTART_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.SHOW_CALENDAR_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.RoutineListDetailRobot.Companion.SHOW_LIST_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagRoutineFinishedListNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 루틴의 완료와 다시 시작이 목록·상세 배치의 목록, 달력과 상세 영역에 미치는 결과를 제품 routineEntry로 구성한 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class RoutineListDetailFinishTest {
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
    fun `TC-c81e1e79-8df0-4798-87c1-14a791884a79 상세 영역에서 완료하면 목록에서 사라지지만 상세가 남는다`() {
        val (ga, na, da) = listOf("가", "나", "다").map { title -> routine(title = title) }
        robot.setNavDisplay(routineList = listOf(ga, na, da))
        robot.selectRoutine(na)
        robot.assertDetailOffersFinish()

        robot.clickDetailAction(description = FINISH_DESCRIPTION)

        robot.waitUntilGone(robot.card("나"))
        assertCardOrder("가", "다")
        backStack.last() shouldBe RoutineDetailNavKey(id = na.id)
        robot.assertDetailOnDetailPane(na)
        robot.assertDetailOffersRestart()
    }

    @Test
    fun `TC-7f809f9e-ce40-4680-9fc9-e9e53a8138dd 상세 영역에 남은 상세에서 다시 시작하면 목록의 순서에 맞는 자리에 다시 나타난다`() {
        val (ga, na, da) = listOf("가", "나", "다").map { title -> routine(title = title) }
        robot.setNavDisplay(routineList = listOf(ga, na, da))
        robot.selectRoutine(na)
        robot.clickDetailAction(description = FINISH_DESCRIPTION)
        robot.waitUntilGone(robot.card("나"))
        assertCardOrder("가", "다")

        robot.clickDetailAction(description = RESTART_DESCRIPTION)

        robot.waitUntilExists(robot.card("나"))
        assertCardOrder("가", "나", "다")
        robot.assertDetailOnDetailPane(na)
        robot.assertDetailOffersFinish()
    }

    @Test
    fun `TC-60590f7c-0ae5-49c3-be2e-fd5627720fe8 상세 영역에서 완료하거나 다시 시작해도 달력의 기록은 바뀌지 않는다`() {
        val routine = routine(title = WATER_TITLE, unit = "ml")
        robot.setNavDisplay(
            routineList = listOf(routine),
            recordList = listOf(routineListDetailRecord(routine = routine, date = TODAY, amount = 1500)),
        )
        robot.showCalendar()
        robot.selectCalendarRecord(title = WATER_TITLE, amountText = AMOUNT_1500_ML_TEXT)
        robot.assertDetailOnDetailPane(routine)

        listOf(FINISH_DESCRIPTION, RESTART_DESCRIPTION).forEach { description ->
            robot.clickDetailAction(description = description)

            robot.assertCalendarView()
            composeRule.onNode(robot.calendarRecord(title = WATER_TITLE, amountText = AMOUNT_1500_ML_TEXT)).assertExists()
            composeRule.onAllNodes(hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG)).fetchSemanticsNodes() shouldHaveSize 1
            robot.assertDetailOnDetailPane(routine)
        }
        robot.assertDetailOffersFinish()
    }

    @Test
    fun `TC-b3889f1b-6fa8-4206-bbd3-4267d241093f 목록 카드에서 완료해도 상세 영역은 그대로이고 제공하는 동작만 바뀐다`() {
        val routine = routine(title = FIRST_TITLE)
        val other = routine(title = SECOND_TITLE)
        robot.setNavDisplay(routineList = listOf(routine, other))
        robot.selectRoutine(routine)
        robot.assertDetailOffersFinish()
        robot.titleInput().performTextReplacement(EDITED_TITLE)
        composeRule.waitForIdle()

        robot.swipeFinish(routine)

        robot.waitUntilGone(robot.card(FIRST_TITLE))
        backStack.last() shouldBe RoutineDetailNavKey(id = routine.id)
        robot.assertDetailOnDetailPane(title = EDITED_TITLE)
        robot.assertDetailOffersRestart()

        composeRule.onNodeWithText(RECORD_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        robot.waitUntilExists(robot.card(FIRST_TITLE))
        robot.assertDetailOnDetailPane(title = EDITED_TITLE)
        robot.assertDetailOffersFinish()
    }

    @Test
    fun `TC-a59b3c3b-cfeb-4431-ae6e-0bf68fa2ed36 달력 보기에서 완료된 루틴의 기록을 선택해도 상세 영역이 그 루틴의 상세로 바뀐다`() {
        val finished = routine(title = EXERCISE_TITLE).copy(isFinished = true)
        robot.setNavDisplay(
            routineList = listOf(routine(title = FIRST_TITLE), finished),
            recordList = listOf(routineListDetailRecord(routine = finished, date = TODAY, amount = 1)),
        )
        robot.showCalendar()
        robot.assertAddOnDetailPane()

        robot.selectCalendarRecord(title = EXERCISE_TITLE, amountText = AMOUNT_1_TIME_TEXT)

        robot.assertCalendarView()
        composeRule.onNode(robot.calendarRecord(title = EXERCISE_TITLE, amountText = AMOUNT_1_TIME_TEXT)).assertExists()
        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = finished.id))
        robot.assertDetailOnDetailPane(finished)
        robot.assertDetailOffersRestart()
    }

    @Test
    fun `TC-2d07d2df-ff06-40e5-b488-bdd92abca85c RoutineFinishedList에서 진입한 루틴 상세는 루틴 목록과 함께 표시하지 않는다`() {
        val unfinished = routine(title = FIRST_TITLE)
        val finished = routine(title = SECOND_TITLE).copy(isFinished = true)
        robot.setNavDisplay(routineList = listOf(unfinished, finished))
        robot.clickFinishedList()
        robot.waitUntilExists(robot.card(SECOND_TITLE))

        composeRule.onNode(robot.card(SECOND_TITLE)).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineFinishedListNavKey, RoutineDetailNavKey(id = finished.id))
        backStack.isRoutineListDetailPane(backStack.last()) shouldBe false
        robot.assertDetailOnDetailPane(finished)
        composeRule.onNodeWithText(FINISHED_LIST_TITLE).assertExists()
        composeRule.onNode(robot.card(FIRST_TITLE)).assertDoesNotExist()
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).fetchSemanticsNodes() shouldHaveSize 1
        composeRule.onAllNodes(hasContentDescription(SHOW_CALENDAR_DESCRIPTION)).fetchSemanticsNodes() shouldHaveSize 0
        composeRule.onAllNodes(hasContentDescription(SHOW_LIST_DESCRIPTION)).fetchSemanticsNodes() shouldHaveSize 0
    }

    @Test
    fun `TagRoutineFinishedList에서 진입한 루틴 상세는 루틴 목록의 상세 영역에 놓이지 않는다`() {
        val listKey = TagRoutineFinishedListNavKey(tagId = Arb.kotlinUuid().next())
        val detailKey = RoutineDetailNavKey(id = Arb.kotlinUuid().next())
        val keyList = listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, TagDetailNavKey(id = listKey.tagId), listKey, detailKey)

        keyList.isRoutineListDetailPane(detailKey) shouldBe false
        keyList.routineDetailPaneSceneKey(detailKey) shouldBe listKey
    }

    @Test
    fun `TC-936f429c-544a-4bb9-9aa6-aa38fbff930b 완료된 루틴 목록에 다녀와도 선택 전 루틴 추가의 작성 내용이 그대로다`() {
        val tag = Arb.tag(title = ROUTINE_LIST_DETAIL_TAG_TITLE).next()
        environment.tagList = listOf(tag)
        robot.setNavDisplay(routineList = listOf(routine(title = FIRST_TITLE), finishedRoutine()))
        robot.titleInput().performTextInput(TYPED_TITLE)
        robot.goalAmountInput().performTextInput(TYPED_GOAL_AMOUNT)
        robot.selectCycle(option = CYCLE_WEEKLY)
        composeRule.runOnIdle { environment.addTagViewModelList.last().add(tagId = tag.id) }
        composeRule.waitForIdle()

        visitFinishedListDetail()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey)
        composeRule.onNode(robot.card(FIRST_TITLE)).assertExists()
        robot.assertAddOnDetailPane()
        robot.titleInput().routineListDetailEditableText() shouldBe TYPED_TITLE
        robot.goalAmountInput().routineListDetailEditableText() shouldBe TYPED_GOAL_AMOUNT
        robot.assertCycleSelected(option = CYCLE_WEEKLY)
        composeRule.onNodeWithText(ROUTINE_LIST_DETAIL_TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-936f429c-544a-4bb9-9aa6-aa38fbff930b 완료된 루틴 목록에 다녀와도 루틴 상세가 그대로이고 뒤로가면 루틴 추가다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine, finishedRoutine()))
        robot.selectRoutine(routine)
        robot.assertDetailOnDetailPane(routine)

        visitFinishedListDetail()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id))
        composeRule.onNode(robot.card(FIRST_TITLE)).assertExists()
        robot.assertDetailOnDetailPane(routine)
        robot.pressBack()
        robot.assertAddOnDetailPane()
    }

    @Test
    fun `TC-936f429c-544a-4bb9-9aa6-aa38fbff930b 완료된 루틴 목록에 다녀와도 상세 위의 루틴 추가가 그대로이고 뒤로가면 루틴 상세다`() {
        val routine = routine(title = FIRST_TITLE)
        robot.setNavDisplay(routineList = listOf(routine, finishedRoutine()))
        robot.selectRoutine(routine)
        robot.assertDetailOnDetailPane(routine)
        robot.clickHomeAdd()
        backStack.last() shouldBe RoutineAddNavKey()

        visitFinishedListDetail()

        backStack.toList() shouldBe listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey, RoutineDetailNavKey(id = routine.id), RoutineAddNavKey())
        composeRule.onNode(robot.card(FIRST_TITLE)).assertExists()
        robot.assertAddOnDetailPane()
        robot.pressBack()
        robot.assertDetailOnDetailPane(routine)
    }

    // 완료된 루틴 확인으로 이동해 완료된 루틴의 상세를 하나 연 뒤, 완료된 루틴 목록이 제공하는 뒤로가기로 돌아온다.
    private fun visitFinishedListDetail() {
        robot.clickFinishedList()
        robot.waitUntilExists(robot.card(FINISHED_TITLE))
        composeRule.onNode(robot.card(FINISHED_TITLE)).performClick()
        composeRule.waitForIdle()
        robot.assertDetailOnDetailPane(title = FINISHED_TITLE)
        robot.clickNavigateUp()
        robot.waitUntilExists(robot.card(FIRST_TITLE))
    }

    private fun assertCardOrder(vararg title: String) {
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).fetchSemanticsNodes() shouldHaveSize title.size
        title
            .map { value ->
                composeRule
                    .onNode(robot.card(value))
                    .fetchSemanticsNode()
                    .boundsInRoot.top
            }.zipWithNext()
            .forEach { (before, after) -> (before < after) shouldBe true }
    }

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        const val FIRST_TITLE = "A-list-routine"
        const val SECOND_TITLE = "B-list-routine"
        const val FINISHED_TITLE = "C-finished-routine"
        const val WATER_TITLE = "물 마시기"
        const val EXERCISE_TITLE = "운동"
        const val EDITED_TITLE = "edited-title"
        const val TYPED_TITLE = "typed-add-title"
        const val TYPED_GOAL_AMOUNT = "2000"
        const val AMOUNT_1500_ML_TEXT = "1500ml"
        const val AMOUNT_1_TIME_TEXT = "1회"

        fun routine(
            title: String,
            unit: String = "회",
        ): Routine = routineListDetailRoutine(title = title, today = TODAY, unit = unit)

        fun finishedRoutine(): Routine = routine(title = FINISHED_TITLE).copy(isFinished = true)
    }
}
