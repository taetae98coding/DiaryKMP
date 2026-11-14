package io.github.taetae98coding.diary.feature.routine.ui.finished

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.routine.ui.form.routineInputUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineSelectCycle
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

// 완료된 루틴 목록을 제품 entry로 배치한 테스트가 함께 쓰는 화면 조작과 확인을 모은다. 문구는 기본 언어(영어) 리소스의 값이다.
internal class RoutineFinishedListRobot(
    private val composeRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>,
    private val environment: RoutineFinishedListTestEnvironment,
) {
    private val backStack: List<ScreenNavKey>
        get() = environment.backStack

    fun setNavDisplay(
        routineList: List<Routine>,
        recordList: List<RoutineRecord> = emptyList(),
    ) {
        environment.setRoutineList(routineList = routineList, recordList = recordList)
        composeRule.setContent { environment.Content() }
        composeRule.waitForIdle()
    }

    // RoutineHome의 목록 보기에서 완료된 루틴 확인을 선택한다.
    fun enterFinishedList() {
        composeRule.waitUntilRoutineFinishedListExists(hasText(HOME_FINISHED_LIST_LABEL))
        composeRule.onNodeWithText(HOME_FINISHED_LIST_LABEL).performClick()
        composeRule.waitForIdle()
        backStack.last() shouldBe RoutineFinishedListNavKey
        composeRule.waitUntilRoutineFinishedListExists(hasText(FINISHED_LIST_TITLE))
    }

    fun selectRoutine(title: String) {
        composeRule.waitUntilRoutineFinishedListExists(routineFinishedListCard(title))
        composeRule.onNode(routineFinishedListCard(title)).performClick()
        composeRule.waitForIdle()
    }

    fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    fun clickNavigateUp() {
        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    // RoutineHome의 목록 보기에는 달력 보기로 바꾸는 동작과 완료된 루틴 진입이 놓인다.
    fun assertHomeListView() {
        composeRule.waitUntilRoutineFinishedListExists(hasContentDescription(SHOW_CALENDAR_DESCRIPTION))
        composeRule.onNodeWithText(HOME_FINISHED_LIST_LABEL).assertExists()
        composeRule.onAllNodesWithText(FINISHED_LIST_TITLE).assertCountEquals(0)
    }

    fun assertFinishedListShown() {
        composeRule.waitUntilRoutineFinishedListExists(hasText(FINISHED_LIST_TITLE))
        composeRule.onAllNodesWithContentDescription(SHOW_CALENDAR_DESCRIPTION).assertCountEquals(0)
    }

    fun assertFinishedListNotShown() {
        composeRule.onAllNodesWithText(FINISHED_LIST_TITLE).assertCountEquals(0)
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertCountEquals(0)
    }

    fun assertPlaceholder() {
        composeRule.waitUntilRoutineFinishedListExists(hasText(PLACEHOLDER_MESSAGE))
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
    }

    fun assertDetail(title: String) {
        composeRule.waitUntil(timeoutMillis = ROUTINE_FINISHED_LIST_TIMEOUT_MILLIS) { composeRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onAllNodesWithText(PLACEHOLDER_MESSAGE).assertCountEquals(0)
        titleInput().fetchSemanticsNode().config[SemanticsProperties.EditableText].text shouldBe title
    }

    fun titleInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[TITLE_INDEX]

    fun goalAmountInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[GOAL_AMOUNT_INDEX]

    // 목록 카드의 스와이프 표시도 같은 이름을 쓰므로 상세 영역에 있는 버튼을 누른다.
    fun clickDetailAction(description: String) {
        detailAction(description).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
    }

    fun detailAction(description: String): SemanticsNodeInteraction {
        val interactions = composeRule.onAllNodes(hasContentDescription(description) and hasClickAction())
        val detailLeft = titleInput().fetchSemanticsNode().boundsInRoot.left
        val index = interactions.fetchSemanticsNodes().indexOfFirst { node -> node.boundsInRoot.left >= detailLeft }

        return interactions[index]
    }

    fun detailActionCount(description: String): Int {
        val detailLeft = titleInput().fetchSemanticsNode().boundsInRoot.left

        return composeRule
            .onAllNodes(hasContentDescription(description) and hasClickAction())
            .fetchSemanticsNodes()
            .count { node -> node.boundsInRoot.left >= detailLeft }
    }

    fun selectCycle(option: String) {
        composeRule.routineSelectCycle(option = option)
    }

    fun inputUnit(unit: String) {
        composeRule.routineInputUnit(unit = unit)
    }

    // 날짜 선택 창을 글자 입력으로 바꿔 날짜를 적고 확인한다.
    fun pickDate(
        currentText: String,
        date: LocalDate,
    ) {
        composeRule.onNodeWithText(currentText).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(SWITCH_TO_INPUT_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextClearance()
        composeRule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextInput(date.toInputText())
        composeRule.waitForIdle()
        composeRule.onNodeWithText(CONFIRM).performClick()
        composeRule.waitForIdle()
    }

    companion object {
        const val HOME_FINISHED_LIST_LABEL = "Finished routines"
        const val FINISHED_LIST_TITLE = "Finished Routines"
        const val FINISHED_TEXT = "Finished"
        const val PLACEHOLDER_MESSAGE = "Choose a routine"
        const val NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val SHOW_CALENDAR_DESCRIPTION = "Show calendar"
        const val ADD_DESCRIPTION = "Add routine"
        const val ADD_TITLE = "Add Routine"
        const val UPDATE_DESCRIPTION = "Update routine"
        const val DELETE_DESCRIPTION = "Delete routine"
        const val FINISH_DESCRIPTION = "Finish routine"
        const val RESTART_DESCRIPTION = "Restart routine"
        const val RECORD_ADD_DESCRIPTION = "Add record"
        const val CYCLE_WEEKLY = "Weekly"
        const val UNIT_COUNT = "회"
        const val SWITCH_TO_INPUT_DESCRIPTION = "Switch to text input mode"
        const val CONFIRM = "OK"

        private const val TITLE_INDEX = 0
        private const val GOAL_AMOUNT_INDEX = 3
    }
}

private fun LocalDate.toInputText(): String = "${month.ordinal.plus(1).toString().padStart(2, '0')}${day.toString().padStart(2, '0')}$year"
