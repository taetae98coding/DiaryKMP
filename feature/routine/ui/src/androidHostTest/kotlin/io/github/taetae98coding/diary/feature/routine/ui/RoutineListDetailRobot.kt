package io.github.taetae98coding.diary.feature.routine.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeFilterNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.form.routineInputUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineInputtedUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineSelectCycle
import io.github.taetae98coding.diary.feature.routine.ui.form.routineSelectedCycle
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.home.record.ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate

// 목록·상세 배치 테스트가 함께 쓰는 화면 조작과 확인을 모은다. 문구는 기본 언어(영어) 리소스의 값이다.
internal class RoutineListDetailRobot(
    private val composeRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>,
    private val environment: RoutineListDetailTestEnvironment,
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
        routineList.firstOrNull()?.takeIf { backStack.contains(RoutineHomeNavKey) }?.let(::waitUntilCard)
    }

    fun card(title: String): SemanticsMatcher = hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(title)

    fun card(
        title: String,
        cardText: String,
    ): SemanticsMatcher = card(title) and hasText(cardText)

    fun calendarRecord(
        title: String,
        amountText: String,
    ): SemanticsMatcher = hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG) and hasContentDescription("$title $amountText, routine")

    fun recordRow(
        dateText: String,
        amountText: String,
    ): SemanticsMatcher = hasText(dateText) and hasText(amountText)

    fun waitUntilCard(routine: Routine) {
        waitUntilExists(card(routine.detail.title))
    }

    fun waitUntilExists(matcher: SemanticsMatcher) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    }

    fun waitUntilGone(matcher: SemanticsMatcher) {
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isEmpty() }
    }

    fun selectRoutine(routine: Routine) {
        waitUntilCard(routine)
        composeRule.onNode(card(routine.detail.title)).performClick()
        composeRule.waitForIdle()
    }

    fun selectCalendarRecord(
        title: String,
        amountText: String,
    ) {
        waitUntilExists(calendarRecord(title = title, amountText = amountText))
        composeRule.onNode(calendarRecord(title = title, amountText = amountText)).performClick()
        composeRule.waitForIdle()
    }

    fun showCalendar() {
        composeRule.onNodeWithContentDescription(SHOW_CALENDAR_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertCalendarView()
    }

    fun showList() {
        composeRule.onNodeWithContentDescription(SHOW_LIST_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        assertListView()
    }

    // 달력 보기에는 목록 보기로 바꾸는 동작이, 목록 보기에는 달력 보기로 바꾸는 동작이 놓인다.
    fun assertCalendarView() {
        composeRule.onNodeWithContentDescription(SHOW_LIST_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(SHOW_CALENDAR_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG)).fetchSemanticsNodes().size shouldBe 0
    }

    fun assertListView() {
        composeRule.onNodeWithContentDescription(SHOW_CALENDAR_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(SHOW_LIST_DESCRIPTION).assertDoesNotExist()
        composeRule.onAllNodes(hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG)).fetchSemanticsNodes().size shouldBe 0
    }

    fun quickRecord(routine: Routine) {
        composeRule.onNode(quickRecordButton(routine)).performClick()
        composeRule.waitForIdle()
    }

    // 빠른 기록을 길게 눌러 양 입력 창을 열고, 양을 적어 확인한다.
    fun amountRecord(
        routine: Routine,
        amount: String,
    ) {
        composeRule.onNode(quickRecordButton(routine)).performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextReplacement(amount)
        composeRule.waitForIdle()
        composeRule.onNode(hasText(CONFIRM) and hasAnyAncestor(isDialog())).performClick()
        composeRule.waitForIdle()
    }

    fun clickHomeAdd() {
        homeAddAction().performClick()
        composeRule.waitForIdle()
    }

    // 루틴 추가의 추가 동작과 이름이 같으므로, 상세 영역의 첫 입력보다 앞에서 끝나는 목록 쪽 동작을 찾는다.
    fun homeAddAction(): SemanticsNodeInteraction {
        val interactions = composeRule.onAllNodesWithContentDescription(ADD_DESCRIPTION)
        val detailLeft = detailPaneLeft()
        val index = interactions.fetchSemanticsNodes().indexOfFirst { node -> node.boundsInRoot.right < detailLeft }

        return interactions[index]
    }

    fun homeAddActionCount(): Int {
        val detailLeft = detailPaneLeft()

        return composeRule
            .onAllNodesWithContentDescription(ADD_DESCRIPTION)
            .fetchSemanticsNodes()
            .count { node -> node.boundsInRoot.right < detailLeft }
    }

    fun clickDetailAdd() {
        val interactions = composeRule.onAllNodesWithContentDescription(ADD_DESCRIPTION)
        val detailLeft = detailPaneLeft()
        val index = interactions.fetchSemanticsNodes().indexOfFirst { node -> node.boundsInRoot.left >= detailLeft }
        interactions[index].performClick()
        composeRule.waitForIdle()
    }

    // 목록 카드의 삭제 동작도 같은 이름이므로 상세 영역에 있는 삭제 버튼을 누른다.
    fun clickDetailDelete() {
        val interactions = composeRule.onAllNodes(hasContentDescription(DELETE_DESCRIPTION) and hasClickAction())
        val detailLeft = detailPaneLeft()
        val index = interactions.fetchSemanticsNodes().indexOfFirst { node -> node.boundsInRoot.left >= detailLeft }
        interactions[index].performClick()
        composeRule.waitForIdle()
    }

    // 목록 카드의 스와이프 배경에도 같은 이름이 있으므로 상세 영역에 있는 완료 상태 전환 버튼만 센다.
    fun detailActionCount(description: String): Int {
        val detailLeft = detailPaneLeft()

        return composeRule
            .onAllNodes(hasContentDescription(description))
            .fetchSemanticsNodes()
            .count { node -> node.boundsInRoot.left >= detailLeft }
    }

    fun clickDetailAction(description: String) {
        val interactions = composeRule.onAllNodes(hasContentDescription(description))
        val detailLeft = detailPaneLeft()
        val index = interactions.fetchSemanticsNodes().indexOfFirst { node -> node.boundsInRoot.left >= detailLeft }
        interactions[index].performClick()
        composeRule.waitForIdle()
    }

    fun assertDetailOffersFinish() {
        detailActionCount(description = FINISH_DESCRIPTION) shouldBe 1
        detailActionCount(description = RESTART_DESCRIPTION) shouldBe 0
    }

    fun assertDetailOffersRestart() {
        detailActionCount(description = RESTART_DESCRIPTION) shouldBe 1
        detailActionCount(description = FINISH_DESCRIPTION) shouldBe 0
    }

    fun swipeFinish(routine: Routine) {
        waitUntilCard(routine)
        composeRule.onNode(card(routine.detail.title)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
    }

    fun clickFinishedList() {
        composeRule.onNodeWithText(FINISHED_LIST_LABEL).performClick()
        composeRule.waitForIdle()
    }

    fun clickNavigateUp() {
        composeRule.onNodeWithContentDescription(NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    fun clickDetailUpdate() {
        composeRule.onNodeWithContentDescription(UPDATE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    fun clickRecordAdd() {
        composeRule.onNode(hasContentDescription(RECORD_ADD_DESCRIPTION) and hasClickAction()).performClick()
        composeRule.waitForIdle()
    }

    fun clickRecordRemove(
        dateText: String,
        amountText: String,
    ) {
        composeRule.onNode(hasContentDescription("Remove record, $dateText $amountText") and hasClickAction()).performClick()
        composeRule.waitForIdle()
    }

    fun selectCycle(option: String) {
        composeRule.routineSelectCycle(option = option)
    }

    fun inputUnit(unit: String) {
        composeRule.routineInputUnit(unit = unit)
    }

    fun assertCycleSelected(option: String) {
        composeRule.routineSelectedCycle() shouldBe option
    }

    fun assertUnit(unit: String) {
        composeRule.routineInputtedUnit() shouldBe unit
    }

    fun titleInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[TITLE_INDEX]

    fun descriptionInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[DESCRIPTION_INDEX]

    fun goalAmountInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[GOAL_AMOUNT_INDEX]

    fun amountPerRecordInput(): SemanticsNodeInteraction = composeRule.onAllNodes(hasSetTextAction())[AMOUNT_PER_RECORD_INDEX]

    // 상세 영역의 입력은 그 영역의 왼쪽 끝 가까이에서 시작하므로, 첫 입력의 왼쪽 끝을 두 영역의 경계로 본다.
    fun detailPaneLeft(): Float = titleInput().fetchSemanticsNode().boundsInRoot.left

    fun assertAddOnDetailPane() {
        composeRule.onNodeWithText(ADD_TITLE).assertExists()
    }

    fun assertDetailOnDetailPane(routine: Routine) {
        assertDetailOnDetailPane(title = routine.detail.title)
    }

    fun assertDetailOnDetailPane(title: String) {
        composeRule.onNodeWithText(ADD_TITLE).assertDoesNotExist()
        titleInput().routineListDetailEditableText() shouldBe title
    }

    fun assertAddInitial(today: LocalDate) {
        titleInput().routineListDetailEditableText() shouldBe ""
        descriptionInput().routineListDetailEditableText() shouldBe ""
        composeRule.onNodeWithText(today.toRoutineListDetailText()).assertExists()
        assertCycleSelected(option = CYCLE_DAILY)
        assertUnit(unit = UNIT_EMPTY)
        goalAmountInput().routineListDetailEditableText() shouldBe ""
        amountPerRecordInput().routineListDetailEditableText() shouldBe "1"
        composeRule.onNodeWithText(ROUTINE_LIST_DETAIL_TAG_TITLE).assertDoesNotExist()
    }

    // 날짜 선택 창을 글자 입력으로 바꿔 날짜를 적고 확인한다.
    fun pickDate(
        currentText: String,
        date: LocalDate,
    ) {
        composeRule.onNodeWithText(currentText).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(SWITCH_TO_INPUT_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextClearance()
        composeRule.onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextInput(date.toInputText())
        composeRule.waitForIdle()
        composeRule.onNodeWithText(CONFIRM).performClick()
        composeRule.waitForIdle()
    }

    fun toggleFilterTag(tag: Tag) {
        if (backStack.last() != RoutineHomeFilterNavKey) {
            composeRule.onNodeWithContentDescription(FILTER_DESCRIPTION).performClick()
            composeRule.waitForIdle()
            composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
                composeRule.onAllNodesWithText(tag.detail.title).fetchSemanticsNodes().isNotEmpty()
            }
        }
        composeRule.onNodeWithText(tag.detail.title).performClick()
        composeRule.waitForIdle()
    }

    fun openFilter() {
        if (backStack.last() == RoutineHomeFilterNavKey) return
        composeRule.onNodeWithContentDescription(FILTER_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        waitUntilExists(viewScopeOption(label = VIEW_SCOPE_TODAY))
    }

    // 필터 Bottom Sheet는 별도 창이라 화면의 뒤로가기 대신 필터 화면을 전환 이력에서 빼서 닫는다.
    fun closeFilter() {
        composeRule.runOnIdle { environment.backStack.remove(RoutineHomeFilterNavKey) shouldBe true }
        composeRule.waitForIdle()
        waitUntilGone(viewScopeOption(label = VIEW_SCOPE_TODAY))
    }

    // 필터를 열어 보기 범위를 고른다. 필터는 열린 채로 남는다.
    fun selectViewScope(label: String) {
        openFilter()
        composeRule.onNode(viewScopeOption(label = label)).performClick()
        composeRule.waitForIdle()
    }

    fun assertViewScopeSelected(label: String) {
        openFilter()
        listOf(VIEW_SCOPE_TODAY, VIEW_SCOPE_ALL).forEach { option ->
            if (option == label) composeRule.onNode(viewScopeOption(label = option)).assertIsSelected() else composeRule.onNode(viewScopeOption(label = option)).assertIsNotSelected()
        }
    }

    fun viewScopeOption(label: String): SemanticsMatcher = hasText(label) and hasClickAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)

    // 격자는 행 우선으로 놓이므로 위에서 아래, 같은 줄에서는 시작에서 끝 순서로 읽은 카드의 제목이다.
    fun cardTitleList(titleList: Collection<String>): List<String> =
        composeRule
            .onAllNodes(hasTestTag(ROUTINE_CARD_TEST_TAG))
            .fetchSemanticsNodes()
            .sortedWith(compareBy({ node -> node.boundsInRoot.top }, { node -> node.boundsInRoot.left }))
            .mapNotNull { node ->
                node.config
                    .getOrNull(SemanticsProperties.Text)
                    .orEmpty()
                    .map { text -> text.text }
                    .firstOrNull { text -> text in titleList }
            }

    fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    // 공통 내비게이션에서 다른 주요 목적지를 고르면 전환 이력이 그 목적지의 것으로 바뀐다.
    fun navigateToOtherTopLevel() {
        composeRule.runOnIdle {
            environment.backStack.clear()
            environment.backStack.addAll(listOf(RoutineListDetailDefaultNavKey, RoutineListDetailOtherNavKey))
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTINE_LIST_DETAIL_OTHER_CONTENT).assertExists()
    }

    fun navigateToRoutineTopLevel() {
        composeRule.runOnIdle {
            environment.backStack.clear()
            environment.backStack.addAll(listOf(RoutineListDetailDefaultNavKey, RoutineHomeNavKey))
        }
        composeRule.waitForIdle()
    }

    private fun quickRecordButton(routine: Routine): SemanticsMatcher =
        hasTestTag(ROUTINE_QUICK_RECORD_BUTTON_TEST_TAG) and
            SemanticsMatcher("${routine.detail.title}의 빠른 기록") { node ->
                node.config
                    .getOrNull(SemanticsProperties.ContentDescription)
                    .orEmpty()
                    .any { description -> description.endsWith(" for ${routine.detail.title}") }
            }

    companion object {
        const val ADD_TITLE = "Add Routine"
        const val ADD_DESCRIPTION = "Add routine"
        const val UPDATE_DESCRIPTION = "Update routine"
        const val DELETE_DESCRIPTION = "Delete routine"
        const val FINISH_DESCRIPTION = "Finish routine"
        const val RESTART_DESCRIPTION = "Restart routine"
        const val FINISHED_LIST_LABEL = "Finished routines"
        const val FINISHED_LIST_TITLE = "Finished Routines"
        const val NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val FILTER_DESCRIPTION = "Filter"
        const val VIEW_SCOPE_TODAY = "Today's routines"
        const val VIEW_SCOPE_ALL = "All routines"
        const val EMPTY_TITLE = "No routines yet"
        const val TODAY_EMPTY_TITLE = "No routines for today"
        const val TODAY_EMPTY_DESCRIPTION = "Use the filter to see all routines."
        const val FILTERED_EMPTY_TITLE = "No routines match the filter"
        const val CYCLE_WEEKDAYS = "Weekdays"
        const val CYCLE_WEEKENDS = "Weekends"
        const val SHOW_CALENDAR_DESCRIPTION = "Show calendar"
        const val SHOW_LIST_DESCRIPTION = "Show list"
        const val RECORD_ADD_DESCRIPTION = "Add record"
        const val RECORD_EMPTY_TITLE = "No records yet"
        const val RECORD_UNDO_ACTION = "Undo"
        const val CYCLE_DAILY = "Daily"
        const val CYCLE_WEEKLY = "Weekly"
        const val UNIT_EMPTY = ""
        const val UNIT_COUNT = "회"
        const val UNIT_MILLILITER = "ml"
        const val SWITCH_TO_INPUT_DESCRIPTION = "Switch to text input mode"
        const val CONFIRM = "OK"
        const val TIMEOUT_MILLIS = 5_000L

        private const val TITLE_INDEX = 0
        private const val DESCRIPTION_INDEX = 1
        private const val GOAL_AMOUNT_INDEX = 3
        private const val AMOUNT_PER_RECORD_INDEX = 4
    }
}

internal const val ROUTINE_LIST_DETAIL_TAG_TITLE: String = "tag-a"

internal fun SemanticsNodeInteraction.routineListDetailEditableText(): String = fetchSemanticsNode().config[SemanticsProperties.EditableText].text

// 기본 언어에서 날짜는 `Jul 15, 2026`처럼 표시된다.
internal fun LocalDate.toRoutineListDetailText(): String = "${ROUTINE_LIST_DETAIL_MONTH_TEXT_LIST[month.ordinal]} $day, $year"

private val ROUTINE_LIST_DETAIL_MONTH_TEXT_LIST: List<String> = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private fun LocalDate.toInputText(): String = "${month.ordinal.plus(1).toString().padStart(2, '0')}${day.toString().padStart(2, '0')}$year"
