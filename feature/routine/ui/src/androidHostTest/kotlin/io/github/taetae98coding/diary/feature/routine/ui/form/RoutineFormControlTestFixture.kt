package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement

internal const val ROUTINE_DEFAULT_UNIT_INPUT_LABEL = "Unit"
internal const val ROUTINE_EMPTY_UNIT = ""

// 주기 버튼은 지금 고른 주기를 글자로 보여 주고 메뉴를 여는 컨트롤로 알린다.
internal fun SemanticsNodeInteractionsProvider.routineCycleButton(): SemanticsNodeInteraction = onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.DropdownList))

internal fun SemanticsNodeInteractionsProvider.routineSelectedCycle(): String =
    routineCycleButton()
        .fetchSemanticsNode()
        .config[SemanticsProperties.Text]
        .joinToString(separator = "") { text -> text.text }

// 주기 버튼은 본문 아래쪽에 놓여 화면 밖에 있을 수 있으므로, 누르는 위치와 무관한 클릭 동작으로 메뉴를 펼치고 항목을 고른다.
internal fun ComposeContentTestRule.routineSelectCycle(option: String) {
    routineCycleButton().performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
    onNode(routineCycleMenuItem(option = option)).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

// 주기 메뉴를 펼쳐 고를 수 있는 항목의 글자를 위에서 아래 순서로 읽고, 고르지 않은 채 메뉴를 닫는다.
internal fun ComposeContentTestRule.routineCycleMenuOptionList(): List<String> {
    routineCycleButton().performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
    val nodeList =
        onAllNodes(hasClickAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected) and hasAnyAncestor(isPopup()))
            .fetchSemanticsNodes()
            .sortedBy { node -> node.boundsInRoot.top }
    val optionList = nodeList.map { node -> node.config[SemanticsProperties.Text].joinToString(separator = "") { text -> text.text } }
    onNode(routineCycleMenuItem(option = routineSelectedCycle())).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()

    return optionList
}

internal fun routineCycleMenuItem(option: String): SemanticsMatcher = hasText(option) and hasClickAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)

internal fun SemanticsNodeInteractionsProvider.routineUnitInput(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasText(ROUTINE_DEFAULT_UNIT_INPUT_LABEL))

internal fun SemanticsNodeInteractionsProvider.routineInputtedUnit(): String = routineUnitInput().fetchSemanticsNode().config[SemanticsProperties.EditableText].text

internal fun ComposeContentTestRule.routineInputUnit(unit: String) {
    routineUnitInput().performTextReplacement(unit)
    waitForIdle()
}
